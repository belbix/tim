package pro.belbix.tim.strategies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.models.OrderSide;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.properties.StrategyPropertiesI;
import pro.belbix.tim.services.*;
import pro.belbix.tim.strategies.common.PositionStatus;
import pro.belbix.tim.strategies.common.TradeStatus;
import pro.belbix.tim.strategies.common.TradeStatusBuilder;
import pro.belbix.tim.utils.Indicators;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static pro.belbix.tim.models.OrderSide.*;


public abstract class Strategy implements Schedulable {
    static final Logger log = LoggerFactory.getLogger(Strategy.class);
    private final ICandleService candleService;
    private final StrategyProperties strategyProperties;
    private final IOrderService orderService;
    private final EmailService emailService;
    private final TickService tickService;
    TradeStatusBuilder tradeStatusBuilder;
    String globalPostfix;
    double lastDeltaPriceStopLose = 0.0;
    double lastDeltaPriceTakeProfit = 0.0;
    Double lastPrice;
    Double openPrice; //for stop lose and other case who need last open price (not from last trade)
    private ProcessingType processingType = ProcessingType.CANDLE;
    private boolean useMarketOrder;
    private PositionStatus positionStatus = new PositionStatus();
    private TradeStatus openDecisionTrade;
    private boolean stopped = false;
    private boolean checkedPositions = false;
    private boolean checkOrders = true;
    private boolean initedTradeStatus = false;
    private boolean withoutDecision = false;

    public Strategy(ICandleService candleService,
                    StrategyProperties strategyProperties,
                    IOrderService orderService,
                    EmailService emailService,
                    TickService tickService) {
        this.candleService = candleService;
        this.strategyProperties = strategyProperties;
        this.orderService = orderService;
        this.emailService = emailService;
        this.tickService = tickService;
        this.useMarketOrder = strategyProperties.isUseMarketOrder();
    }

    private void checkOpenPositions() {
        if (checkedPositions) return;
        orderService.init();
        log.info("Check positions and orders");
        if (orderService.checkOpenPositionsOrOrders(strategyProperties.getSymbol())) {
            log.error("We have opened positions");
            System.exit(0);
        }
        checkedPositions = true;
    }

    private Candle tickProcessing() {
        List<Tick> ticks;
        if (strategyProperties.isSkipLoad()) {
            ticks = new ArrayList<>();
        } else {
            ticks = loadTicks();
            if (ticks == null) return null;
            if (ticks.size() < strategyProperties.getCount()) {
                log.error("Too small ticks: " + ticks.size());
                return null;
            }
        }
        return processingTicks(ticks);
    }

    private Candle candleProcessing() {
        List<Candle> candlesLong = loadCandlesForProcessing(createDefaultCandleRequest(true));
        if (candlesLong == null) return null;
        List<Candle> candlesShort;
        if (strategyProperties.isTimeframeTheSame()) {
            candlesShort = new ArrayList<>(candlesLong);
        } else {
            candlesShort = loadCandlesForProcessing(createDefaultCandleRequest(true));
        }
        if (candlesShort == null) return null;
        return processingCandles(candlesLong, candlesShort);
    }

    @Override
    public void start() {
        if (stopped) {
            return;
        }
        initTradeStatus();
        checkOpenPositions();
        if (checkOrders && !orderService.getLimitOrders().isEmpty()) {
            log.info("Wait until we have open orders");
            return;
        }

        if (checkOrders && openPrice != null && orderService.getLastOrderPrice() != null) {
            openPrice = orderService.getLastOrderPrice();
        }

        try {
            Candle decisionCandle;
            if (processingType.equals(ProcessingType.CANDLE)) {
                decisionCandle = candleProcessing();
            } else if (processingType.equals(ProcessingType.TICK)) {
                decisionCandle = tickProcessing();
            } else {
                throw new IllegalStateException("Unknown processing type");
            }

            changePositionStatus(decisionCandle);
            if (decisionCandle == null || withoutDecision) return;

            log.info("Have decision : " + decisionCandle);
            Order order = null;
            try {
                if (useMarketOrder) {
                    order = createOrder(decisionCandle);
                } else {
                    order = createLimitOrder(decisionCandle);
                }
            } catch (Throwable e) {
                log.error("Error create order, clear all", e);
                emailService.sendError("Error create order", e);
                try {
                    positionStatus.clear();
                    orderService.closeAll(decisionCandle);
                } catch (Throwable e1) {
                    log.info("Error close all orders", e);
                    throw new RuntimeException("Error close all orders");
                }
            }

            log.info("ORDER: " + order);

            if (order != null) {
                decisionCandle.setClose(order.getPrice());
            }

            TradeStatus tradeStatus = tradeStatusBuilder.buildTrade(decisionCandle);
            log.warn(tradeStatus.toString());
            setDecisionTrade(tradeStatus);
            emailService.sendTradeStatus(tradeStatus);

        } catch (Exception e) {
            log.error("Main loop error", e);
            emailService.sendError("Strategy Main loop error " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    public void changePositionStatus(Candle candle) {
        if (candle == null) return;
        if (candle.getOrderSide().isLong()) {
            if (!candle.getOrderSide().isClose()) {
                positionStatus.setLongOpen(candle);
            } else {
                positionStatus.setLongClose();
            }
        }
        if (candle.getOrderSide().isShort()) {
            if (!candle.getOrderSide().isClose()) {
                positionStatus.setShortOpen(candle);
            } else {
                positionStatus.setShortClose();
            }
        }
    }

    public void setDecisionTrade(TradeStatus decisionTrade) {
        if (decisionTrade.getTrade().getCandle().getOrderSide().isClose()) {
            this.openDecisionTrade = null;
        } else {
            this.openDecisionTrade = decisionTrade;
        }
    }

    private void initTradeStatus() {
        if (initedTradeStatus) return;
        tradeStatusBuilder = new TradeStatusBuilder(1000, strategyProperties.getFee(), strategyProperties.getLeverage());
        initedTradeStatus = true;
    }

    private List<Candle> loadCandlesForProcessing(ICandleService.CandleRequest candleRequest) {
        List<Candle> candles;
        if (strategyProperties.isSkipLoad()) {
            candles = new ArrayList<>();
        } else {
            candles = loadCandles(candleRequest);
            if (candles == null) return null;
            if (candles.size() < strategyProperties.getCount()) {
                log.error("Too small candles: " + candles.size());
                return null;
            }
        }
        return candles;
    }

    private ICandleService.CandleRequest createDefaultCandleRequest(boolean l) {
        ICandleService.CandleRequest candleRequest = new ICandleService.CandleRequest();
        candleRequest.setServer(strategyProperties.getServer());
        candleRequest.setSymbol(strategyProperties.getSymbol());
        if (l) {
            candleRequest.setTimeFrame(strategyProperties.getTimeframeLong());
        } else {
            candleRequest.setTimeFrame(strategyProperties.getTimeframeShort());
        }
        candleRequest.setBeforeDate(LocalDateTime.now(ZoneOffset.UTC));
        candleRequest.setCount(strategyProperties.getCount());
        return candleRequest;
    }

    private List<Candle> loadCandles(ICandleService.CandleRequest candleRequest) {
        List<Candle> candles = candleService.loadCandles(candleRequest);
        if (candles.size() < Indicators.CANDLE_SIZE_MIN) {
            log.warn("Candles size too small: " + candles.size());
            return null;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        if (Duration.between(candles.get(0).getDate(), now).toMinutes() > (candleRequest.getTimeFrame() + 1)) {
            log.warn("Candle is too older: " + candles.get(0).getDate() + " for " + now);
            if (strategyProperties.isValidateCandleDate()) return null;
        }

        return candles;
    }

    private List<Tick> loadTicks() {
        if (strategyProperties.isUseDb()) {
            return loadTicksFromDb();
        } else {
            return loadTicksFromRest();
        }
    }

    private List<Tick> loadTicksFromRest() {
        try {
            return tickService.getLastTicksFromExchange(strategyProperties.getCount());
        } catch (TIMRetryException e) {
            log.error("Error load top ticks", e);
        }
        return null;
    }

    private List<Tick> loadTicksFromDb() {
        List<Tick> ticks = tickService.getTopTicks(
                strategyProperties.getServer(),
                strategyProperties.getSymbol(),
                strategyProperties.getCount());
        if (ticks == null || ticks.isEmpty()) {
            throw new TIMRuntimeException("Empty top ticks");
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        if (Duration.between(ticks.get(0).getDate(), now).toMinutes() > strategyProperties.getTimeframeLong() + 1) {
            log.warn("Ticks is too older: " + ticks.get(0).getDate() + " for " + now);
            if (strategyProperties.isValidateCandleDate()) return null;
        }
        return ticks;
    }

    public Candle processingCandles(List<Candle> longCandles, List<Candle> shortCandles) {
        return null;
    }

    public Candle processingTicks(List<Tick> ticks) {
        return null;
    }

    private double calcDeltaPrice() {
        return lastPrice - openPrice;
    }

    private boolean checkStopLose(OrderSide side) {
        if (openDecisionTrade == null) return false;
        if (lastPrice == null || openPrice == null || !strategyProperties.isDirectClose()) return false;
        double dPrice = calcDeltaPrice();
        lastDeltaPriceStopLose = (dPrice / openPrice) * 100;
        boolean r = false;
        double stopLoseProp;
        if (side.isLong()) {
            stopLoseProp = strategyProperties.getStopLoseLong();
            if (lastDeltaPriceStopLose < -stopLoseProp) {
                r = true;
            }
        } else {
            stopLoseProp = strategyProperties.getStopLoseShort();
            if (lastDeltaPriceStopLose > stopLoseProp) {
                r = true;
            }
        }
        if (r) {
            log.warn("Stop lose! " +
                    "\n dPricePerc: " + lastDeltaPriceStopLose + "" +
                    "\n last price: " + lastPrice + "" +
                    "\n openPrice: " + openPrice + "" +
                    "\n stop lose: " + stopLoseProp);
        }
        return r;
    }

    private boolean checkTakeProfit(OrderSide side) {
        if (openDecisionTrade == null || lastPrice == null || openPrice == null || !strategyProperties.isDirectClose())
            return false;
        double dPrice = calcDeltaPrice();
        lastDeltaPriceTakeProfit = (dPrice / openPrice) * 100;
        boolean r = false;
        double takeProfitProp;
        if (side.isLong()) {
            takeProfitProp = strategyProperties.getTakeProfitLong();
            if (lastDeltaPriceTakeProfit > takeProfitProp) {
                r = true;
            }
        } else {
            takeProfitProp = strategyProperties.getTakeProfitShort();
            if (lastDeltaPriceTakeProfit < -takeProfitProp) {
                r = true;
            }
        }
        if (r) {
            log.warn("Take profit! " +
                    "\n dPricePerc: " + lastDeltaPriceTakeProfit + "" +
                    "\n last price: " + lastPrice + "" +
                    "\n openPrice: " + openPrice + "" +
                    "\n take profit: " + takeProfitProp);
        }
        return r;
    }

    Candle decisionLong(List<Candle> candles) {
        if (!positionStatus.isOpenLong()) {
            if (decisionOpenLong(candles)) {
                if (positionStatus.isOpenShort()) {
                    if (strategyProperties.isEnableReverse()) {
                        log.info("Close SHORT candidate as reverse");
                        return makeShortClose(candles);
                    } else {
                        return null;
                    }
                }
                log.info("long open candidate");
                return makeLongOpen(candles);
            }
        } else {
            if (decisionCloseLong(candles, checkStopLose(LONG_CLOSE), checkTakeProfit(LONG_CLOSE))) {
                log.info("long close candidate");
                return makeLongClose(candles);
            }
        }
        return null;
    }

    Candle decisionShort(List<Candle> candles) {

        if (!positionStatus.isOpenShort()) {
            if (decisionOpenShort(candles)) {
                if (positionStatus.isOpenLong()) {
                    if (strategyProperties.isEnableReverse()) {
                        log.info("Close LONG candidate as reverse");
                        return makeLongClose(candles);
                    } else {
                        return null;
                    }
                }
                log.info("short open candidate");
                return makeShortOpen(candles);
            }
        } else {
            if (decisionCloseShort(candles, checkStopLose(SHORT_CLOSE), checkTakeProfit(SHORT_CLOSE))) {
                log.info("short close candidate");
                return makeShortClose(candles);
            }
        }
        return null;
    }

    private Candle makeLongOpen(List<Candle> candles) {
        Candle candle = new Candle(candles.get(0));
        candle.setOrderSide(LONG_OPEN);
        return candle;
    }

    private Candle makeLongClose(List<Candle> candles) {
        Candle candle = new Candle(candles.get(0));
        candle.setOrderSide(LONG_CLOSE);
        return candle;
    }

    private Candle makeShortOpen(List<Candle> candles) {
        Candle candle = new Candle(candles.get(0));
        candle.setOrderSide(SHORT_OPEN);
        return candle;
    }

    private Candle makeShortClose(List<Candle> candles) {
        Candle candle = new Candle(candles.get(0));
        candle.setOrderSide(SHORT_CLOSE);
        return candle;
    }

    Order createOrder(Candle candle) {
        return orderService.createMarketOrder(candle);
    }

    private Order createLimitOrder(Candle decisionCandle) {
        return orderService.createLimitOrder(decisionCandle);
    }

    protected abstract boolean decisionCloseShort(List<Candle> candles, boolean stopLose, boolean takeProfit);

    protected abstract boolean decisionOpenShort(List<Candle> candles);

    protected abstract boolean decisionCloseLong(List<Candle> candles, boolean stopLose, boolean takeProfit);

    protected abstract boolean decisionOpenLong(List<Candle> candles);

    public abstract StrategyPropertiesI getStrategyPropertiesI();

    public abstract void finalClear();

    public void setTradeStatusBuilder(TradeStatusBuilder tradeStatusBuilder) {
        this.tradeStatusBuilder = tradeStatusBuilder;
    }

    public abstract String getStrategyName();

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public void setUseMarketOrder(boolean useMarketOrder) {
        this.useMarketOrder = useMarketOrder;
    }

    public StrategyProperties getStrategyProperties() {
        return strategyProperties;
    }

    void setWithoutDecision(boolean withoutDecision) {
        this.withoutDecision = withoutDecision;
    }

    void setCheckedPositions(boolean checkedPositions) {
        this.checkedPositions = checkedPositions;
    }

    public void setCheckOrders(boolean checkOrders) {
        this.checkOrders = checkOrders;
    }

    public void setGlobalPostfix(String globalPostfix) {
        this.globalPostfix = globalPostfix;
    }

    void finalStrategyClearing() {

        tradeStatusBuilder = null;
        positionStatus.clear();
        openDecisionTrade = null;

        lastPrice = null;
        openPrice = null;

        tickService.clearCache();
    }

    public enum ProcessingType {
        CANDLE, TICK, SRSI
    }
}
