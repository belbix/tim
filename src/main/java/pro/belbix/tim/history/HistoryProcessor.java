package pro.belbix.tim.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.SrsiTickI;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.IndicatorException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.models.ExcludeFromRuns;
import pro.belbix.tim.models.ExcludeFromTests;
import pro.belbix.tim.properties.HistoryProperties;
import pro.belbix.tim.properties.OrderServiceProperties;
import pro.belbix.tim.properties.SrsiProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.services.*;
import pro.belbix.tim.strategies.SrsiStrategy;
import pro.belbix.tim.strategies.Strategy;
import pro.belbix.tim.strategies.common.TradeStatus;
import pro.belbix.tim.strategies.common.TradeStatusBuilder;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@ExcludeFromTests
@ExcludeFromRuns
public class HistoryProcessor {
    private static final Logger log = LoggerFactory.getLogger(HistoryProcessor.class);
    private final DBCandleService candleService;
    private final HistoryProperties prop;
    private final AppContextService appContextService;
    private final TickService tickService;
    private final OrderServiceProperties orderServiceProperties;
    private final SrsiTickService srsiTickService;
    private Strategy strategy;
    private TradeStatusBuilder tradeStatusBuilder;
    private DecisionService decisionService;
    private List<Position> positions = new ArrayList<>();
    private List<Candle> decisions = new ArrayList<>();

    @Autowired
    public HistoryProcessor(DBCandleService candleService,
                            HistoryProperties prop,
                            AppContextService appContextService,
                            TickService tickService,
                            OrderServiceProperties orderServiceProperties,
                            SrsiTickService srsiTickService) {
        this.candleService = candleService;
        this.prop = prop;
        this.appContextService = appContextService;
        this.tickService = tickService;
        this.orderServiceProperties = orderServiceProperties;
        this.srsiTickService = srsiTickService;
    }

    public static List<Candle> loadCandles(ICandleService candleService,
                                           ICandleService.CandleRequest candleRequest,
                                           List<Candle> loadedCandles,
                                           int batch) {
        if (loadedCandles != null && loadedCandles.size() > 1) {
            candleRequest.setCount(2);
        } else {
            candleRequest.setCount(batch);
        }

        List<Candle> newCandles = candleService.loadCandles(candleRequest);
        if (newCandles != null && !newCandles.isEmpty()) {

            if (loadedCandles != null && loadedCandles.size() > 1) {
                if (loadedCandles.get(0).getDate().equals(newCandles.get(0).getDate())) {
                    loadedCandles.set(0, newCandles.get(0));
                } else {
                    loadedCandles.add(0, newCandles.get(0));
                    if (loadedCandles.size() > batch) {
                        loadedCandles.remove(loadedCandles.size() - 1);
                    }
                }
                return loadedCandles;
            } else {
                return newCandles;
            }

        }
        return loadedCandles;
    }

    public static ICandleService.CandleRequest createCandleRequest(StrategyProperties prop, int batch, int timeframe) {
        ICandleService.CandleRequest candleRequest = new ICandleService.CandleRequest();
        candleRequest.setServer(prop.getServer());
        candleRequest.setSymbol(prop.getSymbol());
        candleRequest.setCount(batch);
        candleRequest.setTimeFrame(timeframe);
        candleRequest.setFirstDynamic(true);
        candleRequest.setOnlyClose(true);
        return candleRequest;
    }

    public void start() {
        log.info("Start history from " + prop);
        init(prop);
        processing(prop);
        System.exit(0);
    }

    private void handleCandles(LocalDateTime dateStart,
                               LocalDateTime dateStop,
                               HistoryProperties prop) {
        ICandleService.CandleRequest candleRequestLong =
                createCandleRequest(strategy.getStrategyProperties(), prop.getBatch(),
                        strategy.getStrategyProperties().getTimeframeLong());
        ICandleService.CandleRequest candleRequestShort =
                createCandleRequest(strategy.getStrategyProperties(), prop.getBatch(),
                        strategy.getStrategyProperties().getTimeframeShort());
        Candle candleDecision;
        Tick tickExecution;
        while (dateStart.isBefore(dateStop)) {
            candleRequestLong.setBeforeDate(dateStart);
            candleRequestShort.setBeforeDate(dateStart);

            List<Candle> candlesLong = candleService.loadCandles(candleRequestLong);
            if (candlesLong == null || candlesLong.isEmpty()) {
                LocalDateTime newDateStart = dateStart.plus(prop.getEmptyTicktime(), ChronoUnit.MINUTES);
                log.info(dateStart + " Empty candles, continue to " + newDateStart + "\n" + candleRequestLong);
                dateStart = newDateStart;
                continue;
            }

            List<Candle> candlesShort;
            if (strategy.getStrategyProperties().isTimeframeTheSame()) {
                candlesShort = new ArrayList<>(candlesLong);
            } else {
                candlesShort = candleService.loadCandles(candleRequestShort);
            }

            if (candlesShort == null || candlesShort.isEmpty()) {
                LocalDateTime newDateStart = dateStart.plus(prop.getEmptyTicktime(), ChronoUnit.MINUTES);
                log.info(dateStart + " Empty candles, continue to " + newDateStart + "\n" + candleRequestShort);
                dateStart = newDateStart;
                continue;
            }

            try {
                candleDecision = strategy.processingCandles(candlesLong, candlesShort);
                tickExecution = makeMainProcessing(candleDecision, dateStart);
            } catch (IndicatorException e) {
                log.error(dateStart + " " + e.getMessage() + " to " + dateStop);
                dateStart = dateStart.plus(prop.getEmptyTicktime(), ChronoUnit.MINUTES);
                continue;
            } catch (Throwable e) {
                e.printStackTrace();
                log.error("handleCandles error ", e);
                System.exit(-1);
                continue;
            }

            completePosition(tickExecution, decisionService.getPosition());

            dateStart = dateStart.plus(prop.getTicktime(), ChronoUnit.SECONDS);

            delay();
        }
    }

    public Strategy init(HistoryProperties prop) {
        strategy = appContextService.findStrategy(prop.getStrategyName());
        if (strategy == null) {
            throw new TIMRuntimeException("Strategy " + prop.getStrategyName() + " not found");
        }
        this.decisionService = new DecisionService(strategy, tickService, orderServiceProperties);
        return strategy;
    }

    public TradeStatusBuilder processing(HistoryProperties prop) {
        log.info("start history processing");
        LocalDateTime dateStart = LocalDateTime.parse(prop.getDateStart());
        LocalDateTime dateStop = LocalDateTime.parse(prop.getDateEnd());
        tradeStatusBuilder = new TradeStatusBuilder(prop.getDeposit(), prop.getFee(), prop.getLeverage());
        strategy.setTradeStatusBuilder(tradeStatusBuilder);

        if (strategy.getProcessingType().equals(Strategy.ProcessingType.CANDLE)) {
            handleCandles(dateStart, dateStop, prop);
        } else if (strategy.getProcessingType().equals(Strategy.ProcessingType.TICK)) {
            handleTicks(dateStart, dateStop, prop);
        } else if (strategy.getProcessingType().equals(Strategy.ProcessingType.SRSI)) {
            handleSrsi(dateStart, dateStop);
        }
        printResult(tradeStatusBuilder);
        tickService.clearCache();
        return tradeStatusBuilder;
    }

    private void handleTicks(LocalDateTime dateStart,
                             LocalDateTime dateStop,
                             HistoryProperties prop) {
        Candle candleDecision;
        Tick tickExecution;
        while (dateStart.isBefore(dateStop)) {
            List<Tick> ticks = loadTicks(dateStart, strategy.getStrategyProperties(), prop.getBatch());
            if (ticks == null || ticks.isEmpty()) {
                LocalDateTime newDateStart = dateStart.plus(prop.getEmptyTicktime(), ChronoUnit.MINUTES);
                log.info(dateStart + " Empty ticks, continue to " + newDateStart);
                dateStart = newDateStart;
                continue;
            }

            try {
                candleDecision = strategy.processingTicks(ticks);
                tickExecution = makeMainProcessing(candleDecision, dateStart);
            } catch (IndicatorException e) {
                log.error(dateStart + " " + e.getMessage() + " to " + dateStop);
                dateStart = dateStart.plus(prop.getEmptyTicktime(), ChronoUnit.MINUTES);
                continue;
            } catch (Throwable e) {
                log.error("processingTicks error ", e);
                continue;
            }

            completePosition(tickExecution, decisionService.getPosition());

            dateStart = dateStart.plus(prop.getTicktime(), ChronoUnit.SECONDS);

            delay();
        }
    }

    private List<Tick> loadTicks(LocalDateTime dateStart, StrategyProperties prop, int batch) {
        return tickService.getTopTicksBeforeDateCached(prop.getServer(),
                prop.getSymbol(),
                dateStart,
                batch);
    }

    public void handleSrsi(LocalDateTime dateStart, LocalDateTime dateStop) {
        Candle candleDecision;
        Tick tickExecution;
        int version = srsiVersion();
        ((SrsiStrategy) strategy).getSrsiProperties().setComputeSrsi(false);
        ((SrsiStrategy) strategy).getSrsiProperties().setPrintProcessing(false);

        log.info("Start loading srsiTicks");
        List<SrsiTickI> srsiTicks = srsiTickService.load(dateStart, dateStop, version);
        log.info("Loaded srsiTicks " + srsiTicks.size());
        SrsiTickI srsiTickLast = null;
        for (SrsiTickI srsiTick : srsiTicks) {
            if (srsiTickLast != null) {
                if (srsiTick.getDate().isBefore(srsiTickLast.getDate())) {
                    throw new IllegalStateException("SrsiTick invalid date: " + srsiTick);
                }
            } else {
                srsiTickLast = srsiTick;
            }
            List<Candle> candles = srsiTick.toCandles(strategy.getStrategyProperties().getTimeframeLong());
            try {
                candleDecision = strategy.processingCandles(candles, candles);
                tickExecution = makeMainProcessing(candleDecision, srsiTick.getDate());
            } catch (IndicatorException e) {
                log.error(srsiTick.getDate() + " " + e.getMessage() + " to " + dateStop);
                continue;
            }

            completePosition(tickExecution, decisionService.getPosition());

            if (prop.getMinDeposit() != 0 && tradeStatusBuilder.getDeposit() < prop.getMinDeposit()) {
                log.info("You lost all deposit, stop history on " + dateStart);
                break;
            }

            delay();
        }
    }

    private void completePosition(Tick tickExecution, Position p) {
        if (p != null && tickExecution != null) {
            log.info("Complete " + p + " on tick " + tickExecution);
            if (p.getReverse() != null) {
                log.info("Complete reverse " + p.getReverse());
                completePosition(tickExecution, p.getReverse());
            }
            Candle finalCandle = p.getCandle();
            finalCandle.setClose(tickExecution.getPrice());
            finalCandle.setDate(tickExecution.getDate());
            TradeStatus ts = buildAndPrintDecision(finalCandle, tradeStatusBuilder);
            if (ts != null) {
                strategy.setDecisionTrade(ts);
            }
            positions.add(decisionService.getPosition());
            decisionService.deletePosition();
        }
    }

    private void delay() {
        if (prop.getDelay() > 0) {
            log.info("Delay: " + prop.getDelay());
            try {
                Thread.sleep(prop.getDelay());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private TradeStatus buildAndPrintDecision(Candle candleDecision, TradeStatusBuilder tradeStatusBuilder) {
        if (candleDecision != null) {
            TradeStatus ts = tradeStatusBuilder.buildTrade(candleDecision);
            log.warn(ts.toString());
            return ts;
        }
        return null;
    }

    private Tick makeMainProcessing(Candle candleDecision, LocalDateTime dateStart) {
        if (candleDecision != null) {
            log.warn("Position for " + candleDecision + " on " + dateStart);
            decisions.add(new Candle(candleDecision));
            strategy.setGlobalPostfix(" | " + candleDecision.getOrderSide() + " " + candleDecision.getClose());
            strategy.changePositionStatus(candleDecision);
        }
        return decisionService.executeDecision(candleDecision, dateStart);
    }

    private void printResult(TradeStatusBuilder tradeStatusBuilder) {
        List<TradeStatus> tradeStatuses = tradeStatusBuilder.getTradeStatuses();
        if (tradeStatuses == null || tradeStatuses.isEmpty()) return;
        TradeStatus lastTs = tradeStatuses.get(tradeStatuses.size() - 1);
        log.info("History tick the end. Trades: " + tradeStatuses.size()
                + ". Final deposit: " + lastTs.getDeposit()
                + ". Final eff: " + calcFinalEff(tradeStatusBuilder)
        );
        String fileName = System.getProperty("user.dir") + File.separator
                + "histories" + File.separator
                + "hist_" + strategy.getStrategyProperties().getServer() + "_" + calcFinalEff(tradeStatusBuilder) + "_"
                + strategy.getStrategyProperties().getSymbol()
                + "_" + strategy.getStrategyProperties().getTimeframeLong() + "_"
                + "_" + strategy.getStrategyProperties().getTimeframeShort() + "_"
                + LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_TIME)
                .replace(":", "_").replace(".", "_")
                + ".txt";
        TradeStatus.writeTradeStatusInFile(strategy, tradeStatuses, fileName);

        int imm = 0;
        for (Position position : positions) {
            if (position != null && position.isImmediatelyExecute()) {
                imm++;
            }
        }
        log.info("Immediately executed " + imm);
    }

    public double calcFinalEff(TradeStatusBuilder tradeStatusBuilder) {
        switch (prop.getEffType()) {
            default:
            case "pm":
                return Math.round(tradeStatusBuilder.getEffective() - Math.abs(tradeStatusBuilder.maxLose() * prop.getEffLoseMod()));
            case "p":
                return Math.round(tradeStatusBuilder.getEffective());
            case "d":
                return Math.round(tradeStatusBuilder.getDeposit());
        }

    }

    public String createResult() {
        if (tradeStatusBuilder == null) {
            return "empty";
        }
        List<TradeStatus> tradeStatuses = tradeStatusBuilder.getTradeStatuses();
        StringBuilder sb = new StringBuilder();
        if (strategy != null) {
            sb.append(strategy.getStrategyProperties().toString()).append("\n");
            sb.append(strategy.getStrategyPropertiesI().toString()).append("\n");
            sb.append(orderServiceProperties.toString()).append("\n");
        }
        sb.append(TradeStatus.csvHeaders()).append("\n");
        for (TradeStatus ts : tradeStatuses) {
            if (ts.getOpen() == null || ts.getOpen()) continue;
            String csv = ts.toCsvString();
            sb.append(csv).append("\n");

        }
        return sb.toString();
    }

    public void clear() {
        this.decisionService = new DecisionService(strategy, tickService, orderServiceProperties);
        positions.clear();
        decisions.clear();
    }

    public TradeStatusBuilder getTradeStatusBuilder() {
        return tradeStatusBuilder;
    }

    public void setTradeStatusBuilder(TradeStatusBuilder tradeStatusBuilder) {
        this.tradeStatusBuilder = tradeStatusBuilder;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public int srsiVersion() {
        if (strategy.getStrategyPropertiesI() instanceof SrsiProperties) {
            return 1;
        } else {
            return 2;
        }
    }
}
