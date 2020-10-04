package pro.belbix.tim.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.properties.Srsi2Properties;
import pro.belbix.tim.properties.SrsiProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.properties.StrategyPropertiesI;
import pro.belbix.tim.protobuf.srsi.Srsi;
import pro.belbix.tim.services.EmailService;
import pro.belbix.tim.services.ICandleService;
import pro.belbix.tim.services.IOrderService;
import pro.belbix.tim.services.TickService;

import java.util.List;

import static pro.belbix.tim.strategies.Strategy.ProcessingType.SRSI;

@Component
public class Srsi2Strategy extends SrsiStrategy {
    private static final String NAME = "srsi2";
    private final Srsi2Properties srsi2Properties;
    private Srsi srsi;
    private String lastNodes;

    @Autowired
    public Srsi2Strategy(ICandleService candleService,
                         StrategyProperties strategyProperties,
                         IOrderService orderService,
                         SrsiProperties srsiProperties,
                         Srsi2Properties srsi2Properties,
                         EmailService emailService,
                         TickService tickService) {
        super(candleService, strategyProperties, orderService, srsiProperties, emailService, tickService);
        setUseMarketOrder(srsi2Properties.isUseMarketOrders());
        this.srsi2Properties = srsi2Properties;
    }

    @Override
    void preProcessing() {
        if (srsi == null || !lastNodes.equals(srsi2Properties.getNodes())) {
            lastNodes = srsi2Properties.getNodes();
            this.srsi = srsi2Properties.toSrsi();
        }
    }

    @Override
    protected boolean decisionOpenLong(List<Candle> candles) {
        if (!srsi2Properties.isUseLong()) return false;
        return SrsiDecisionMaker.make(candles, srsi.getNodesLongOpenList());
    }

    @Override
    protected boolean decisionCloseLong(List<Candle> candles, boolean stopLose, boolean takeProfit) {
        if (stopLose || takeProfit) return true;
        return SrsiDecisionMaker.make(candles, srsi.getNodesLongCloseList());
    }

    @Override
    protected boolean decisionOpenShort(List<Candle> candles) {
        if (!srsi2Properties.isUseShort()) return false;
        return SrsiDecisionMaker.make(candles, srsi.getNodesShortOpenList());
    }

    @Override
    protected boolean decisionCloseShort(List<Candle> candles, boolean stopLose, boolean takeProfit) {
        if (stopLose || takeProfit) return true;
        return SrsiDecisionMaker.make(candles, srsi.getNodesShortCloseList());
    }

    @Override
    public StrategyPropertiesI getStrategyPropertiesI() {
        return srsi2Properties;
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
