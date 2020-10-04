package pro.belbix.tim.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.neurons.NeuroLink;
import pro.belbix.tim.properties.NsrsiProperties;
import pro.belbix.tim.properties.SrsiProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.properties.StrategyPropertiesI;
import pro.belbix.tim.protobuf.neuron.Nsrsi;
import pro.belbix.tim.services.EmailService;
import pro.belbix.tim.services.ICandleService;
import pro.belbix.tim.services.IOrderService;
import pro.belbix.tim.services.TickService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Function;

import static pro.belbix.tim.strategies.Strategy.ProcessingType.SRSI;

@Component
public class NsrsiStrategy extends SrsiStrategy {
    private static final String NAME = "nsrsi";
    private final NsrsiProperties nsrsiProperties;
    private final Function<Candle, Double> deltaSrsiReceiver = Candle::calcDeltaK;
    private Nsrsi nsrsi;
    private String lastNsrsi;
    private NeuroLink longOpenNL;
    private NeuroLink longCloseNL;
    private NeuroLink shortOpenNL;
    private NeuroLink shortCloseNL;

    @Autowired
    public NsrsiStrategy(ICandleService candleService,
                         StrategyProperties strategyProperties,
                         IOrderService orderService,
                         SrsiProperties srsiProperties,
                         NsrsiProperties nsrsiProperties,
                         EmailService emailService,
                         TickService tickService) {
        super(candleService, strategyProperties, orderService, srsiProperties, emailService, tickService);
        setUseMarketOrder(nsrsiProperties.isUseMarketOrders());
        this.nsrsiProperties = nsrsiProperties;
    }

    @PostConstruct
    private void init() {
        preProcessing();
    }

    @Override
    void preProcessing() {
        if (nsrsi == null || !lastNsrsi.equals(nsrsiProperties.getModel())) {
            lastNsrsi = nsrsiProperties.getModel();
            this.nsrsi = nsrsiProperties.toNsrsi();
            this.longOpenNL = new NeuroLink(this.nsrsi.getOpenLong());
            this.longCloseNL = new NeuroLink(this.nsrsi.getCloseLong());
            this.shortOpenNL = new NeuroLink(this.nsrsi.getOpenShort());
            this.shortCloseNL = new NeuroLink(this.nsrsi.getCloseShort());
        }
    }

    @Override
    protected boolean decisionOpenLong(List<Candle> candles) {
        if (!nsrsiProperties.isUseLong()) return false;
        return longOpenNL.handleCandles(candles, deltaSrsiReceiver);
    }

    @Override
    protected boolean decisionCloseLong(List<Candle> candles, boolean stopLose, boolean takeProfit) {
        if (stopLose || takeProfit) return true;
        return longCloseNL.handleCandles(candles, deltaSrsiReceiver);
    }

    @Override
    protected boolean decisionOpenShort(List<Candle> candles) {
        if (!nsrsiProperties.isUseShort()) return false;
        return shortOpenNL.handleCandles(candles, deltaSrsiReceiver);
    }

    @Override
    protected boolean decisionCloseShort(List<Candle> candles, boolean stopLose, boolean takeProfit) {
        if (stopLose || takeProfit) return true;
        return shortCloseNL.handleCandles(candles, deltaSrsiReceiver);
    }

    public NeuroLink getLongOpenNL() {
        return longOpenNL;
    }

    public NeuroLink getLongCloseNL() {
        return longCloseNL;
    }

    public NeuroLink getShortOpenNL() {
        return shortOpenNL;
    }

    public NeuroLink getShortCloseNL() {
        return shortCloseNL;
    }

    @Override
    public StrategyPropertiesI getStrategyPropertiesI() {
        return nsrsiProperties;
    }

    @Override
    public void finalClear() {
        finalStrategyClearing();
    }

    @Override
    public String getStrategyName() {
        return NAME;
    }

    @Override
    public ProcessingType getProcessingType() {
        return SRSI;
//        return CANDLE;
    }
}
