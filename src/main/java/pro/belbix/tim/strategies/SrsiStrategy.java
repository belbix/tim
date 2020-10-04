package pro.belbix.tim.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.properties.SrsiProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.properties.StrategyPropertiesI;
import pro.belbix.tim.services.EmailService;
import pro.belbix.tim.services.ICandleService;
import pro.belbix.tim.services.IOrderService;
import pro.belbix.tim.services.TickService;
import pro.belbix.tim.utils.Indicators;

import java.util.List;

import static pro.belbix.tim.strategies.Strategy.ProcessingType.SRSI;

@Component
public class SrsiStrategy extends Strategy {
    private static final String NAME = "srsi";
    private final SrsiProperties srsiProperties;

    @Autowired
    public SrsiStrategy(ICandleService candleService,
                        StrategyProperties strategyProperties,
                        IOrderService orderService,
                        SrsiProperties srsiProperties,
                        EmailService emailService,
                        TickService tickService) {
        super(candleService, strategyProperties, orderService, emailService, tickService);
        setUseMarketOrder(srsiProperties.isUseMarketOrders());
        this.srsiProperties = srsiProperties;
    }

    void preProcessing() {

    }

    @Override
    public Candle processingCandles(List<Candle> candlesLong, List<Candle> candlesShort) {
        preProcessing();

        if (srsiProperties.isComputeSrsi()) {
            Indicators.stochasticRsi(candlesLong);
            if (!candlesLong.equals(candlesShort)) {
                Indicators.stochasticRsi(candlesShort);
            }
        }
        if (srsiProperties.isPrintProcessing()) {
            if (srsiProperties.isUseLong()) {
                printProcessing(candlesLong, "LONG | ");
            }
            if (srsiProperties.isUseShort()) {
                printProcessing(candlesShort, "SHRT | ");
            }
        }
        lastPrice = candlesLong.get(0).getClose();

        if (!validate(candlesLong)) return null;
        if (!validate(candlesShort)) return null;

        Candle decisionLongCandle = decisionLong(candlesLong);
        Candle decisionShortCandle = decisionShort(candlesShort);

        if (decisionLongCandle != null && decisionShortCandle != null) {
            if (decisionLongCandle.getOrderSide().isOpen()) {
                saveLastPrices(decisionLongCandle, null);
                return decisionLongCandle;
            }
            if (decisionShortCandle.getOrderSide().isOpen()) {
                saveLastPrices(null, decisionShortCandle);
                return decisionShortCandle;
            }
            log.info("DOUBLE decision, skip result and set");
            return null;
        }
        if (decisionLongCandle != null) {
            saveLastPrices(decisionLongCandle, null);
            return decisionLongCandle;
        }
        if (decisionShortCandle != null) {
            saveLastPrices(null, decisionShortCandle);
            return decisionShortCandle;
        }
        return null;
    }

    private void saveLastPrices(Candle decisionLongCandle, Candle decisionShortCandle) {
        if (decisionLongCandle != null) {
            if (decisionLongCandle.getOrderSide().isOpen()) {
                openPrice = decisionLongCandle.getClose();
            } else {
                openPrice = null;
            }
        }

        if (decisionShortCandle != null) {
            if (decisionShortCandle.getOrderSide().isOpen()) {
                openPrice = decisionShortCandle.getClose();
            } else {
                openPrice = null;
            }
        }
    }

    private void printProcessing(List<Candle> candles, String prefix) {
        if (candles == null || candles.isEmpty()) {
            log.info("Empty candles");
            return;
        }

        String msg = prefix;

        Candle first = candles.get(srsiProperties.getFirstRowNumber());
        Candle second = candles.get(srsiProperties.getSecondRowNumber());
        Candle old = candles.get(srsiProperties.getOldRowNumber());

        msg += first.tickTime() + " | ";
        msg += first.getDate().getHour() + " | ";
        msg += String.format("%.1f", first.getClose()) + " | ";
        msg += String.format("k/d:%.1f", first.getSlowk()) + "/" + String.format("%.1f", first.getSlowd()) + " | ";
        msg += String.format("1dk:%.1f", first.calcDeltaK()) + " | ";
        msg += String.format("2dk:%.1f", second.calcDeltaK()) + " | ";
        msg += String.format("3dk:%.1f", old.calcDeltaK()) + " | ";
        msg += String.format("sl:%.1f", lastDeltaPriceStopLose) + " | ";
        msg += String.format("op:%.1f", openPrice) + " | ";
        if (tradeStatusBuilder != null) msg += tradeStatusBuilder.toString();
        if (globalPostfix != null) msg += globalPostfix;
        log.info(msg);
    }

    private boolean validate(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) return false;
        return true;
    }

    @Override
    protected boolean decisionOpenLong(List<Candle> candles) {
        if (!srsiProperties.isUseLong()) return false;
        return rsiDeltaSecondLong(candles)
                && priceDifferentLong(candles)
                && rsiDeltaFirstLong(candles)
                && kFirstMinLong(candles)
                && rsiDeltaOldLong(candles);
    }

    private boolean rsiDeltaOldLong(List<Candle> candles) {
        double rsiDeltaOld = candles.get(srsiProperties.getOldRowNumber()).calcDeltaK();
        return rsiDeltaOld < srsiProperties.getRsiDeltaOldOpenLong();
    }

    private boolean kFirstMinLong(List<Candle> candles) {
        Double kFirst = candles.get(srsiProperties.getFirstRowNumber()).getSlowk();
        return kFirst != null && kFirst <= srsiProperties.getKFirstMin();
    }

    private boolean rsiDeltaFirstLong(List<Candle> candles) {
        double rsiDeltaFirst = candles.get(srsiProperties.getFirstRowNumber()).calcDeltaK();
        return rsiDeltaFirst > srsiProperties.getRsiDeltaFirstOpen();
    }

    private boolean priceDifferentLong(List<Candle> candles) {
        double priceDifferent = candles.get(srsiProperties.getFirstRowNumber())
                .calcDiffWithPrev(candles.get(srsiProperties.getSecondRowNumber()));
        return priceDifferent < srsiProperties.getPriceDiffLongPerc();
    }

    private boolean rsiDeltaSecondLong(List<Candle> candles) {
        double rsiDeltaSecond = candles.get(srsiProperties.getSecondRowNumber()).calcDeltaK();
        return rsiDeltaSecond > srsiProperties.getRsiDeltaSecondOpen();
    }

    @Override
    protected boolean decisionCloseLong(List<Candle> candles, boolean stopLose, boolean takeProfit) {
        if (stopLose || takeProfit) return true;
        double rsiDeltaFirst = candles.get(srsiProperties.getFirstRowNumber()).calcDeltaK();
        if (priceDelta() > srsiProperties.getOpenPositionBigPriceChange()) {
            return rsiDeltaFirst < srsiProperties.getRsiDeltaFirstCloseLongAfterPump();
        } else {
            return rsiDeltaFirst < srsiProperties.getRsiDeltaFirstCloseLong();
        }
    }

    @Override
    protected boolean decisionOpenShort(List<Candle> candles) {
        if (!srsiProperties.isUseShort()) return false;

        return rsiDeltaSecondShort(candles)
//                && priceDifferentShort(candles)
                && rsiDeltaFirstShort(candles)
                && kFirstMinShort(candles)
                && rsiDeltaOldShort(candles);
    }

    private boolean rsiDeltaOldShort(List<Candle> candles) {
        double rsiDeltaOld = candles.get(srsiProperties.getOldRowNumber()).calcDeltaK();
        return rsiDeltaOld > -srsiProperties.getRsiDeltaOldOpenShort();
    }

    private boolean priceDifferentShort(List<Candle> candles) {
        double priceDifferent = candles.get(srsiProperties.getFirstRowNumber())
                .calcDiffWithPrev(candles.get(srsiProperties.getSecondRowNumber()));
        return priceDifferent < srsiProperties.getPriceDiffShortPerc();
    }

    private boolean kFirstMinShort(List<Candle> candles) {
        Double kFirst = candles.get(srsiProperties.getFirstRowNumber()).getSlowk();
        return kFirst != null && kFirst >= srsiProperties.getKmax();
    }

    private boolean rsiDeltaFirstShort(List<Candle> candles) {
        double rsiDeltaFirst = candles.get(srsiProperties.getFirstRowNumber()).calcDeltaK();
        return rsiDeltaFirst < -srsiProperties.getRsiDeltaFirstOpen();
    }

    private boolean rsiDeltaSecondShort(List<Candle> candles) {
        double rsiDeltaSecond = candles.get(srsiProperties.getSecondRowNumber()).calcDeltaK();
        return rsiDeltaSecond < -srsiProperties.getRsiDeltaSecondOpen();
    }

    private double priceDelta() {
        return ((lastPrice - openPrice) / lastPrice) * 100.0;
    }

    @Override
    protected boolean decisionCloseShort(List<Candle> candles, boolean stopLose, boolean takeProfit) {
        if (stopLose || takeProfit) return true;
        double rsiDeltaFirst = candles.get(srsiProperties.getFirstRowNumber()).calcDeltaK();
        if (priceDelta() < -srsiProperties.getOpenPositionBigPriceChange()) {
            return rsiDeltaFirst > srsiProperties.getRsiDeltaFirstCloseShortAfterDump();
        } else {
            return rsiDeltaFirst > srsiProperties.getRsiDeltaFirstCloseShort();
        }
    }

    public SrsiProperties getSrsiProperties() {
        return srsiProperties;
    }

    @Override
    public void finalClear() {
        finalStrategyClearing();
    }

    @Override
    public ProcessingType getProcessingType() {
        return SRSI;
//        return CANDLE;
    }

    @Override
    public StrategyPropertiesI getStrategyPropertiesI() {
        return srsiProperties;
    }

    @Override
    public String getStrategyName() {
        return SrsiStrategy.NAME;
    }

    @Override
    public String getThreadName() {
        return "SRSI";
    }
}
