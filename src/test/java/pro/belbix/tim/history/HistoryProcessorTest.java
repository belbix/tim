package pro.belbix.tim.history;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.SrsiTick;
import pro.belbix.tim.entity.SrsiTickI;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.properties.*;
import pro.belbix.tim.services.AppContextService;
import pro.belbix.tim.services.DBCandleService;
import pro.belbix.tim.services.SrsiTickService;
import pro.belbix.tim.services.TickService;
import pro.belbix.tim.strategies.common.TradeStatusBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class HistoryProcessorTest {
    private static final LocalDateTime START = LocalDateTime.parse("2020-01-01T00:00:00");
    private static final LocalDateTime END = LocalDateTime.parse("2020-01-01T00:00:00");
    private static final List<SrsiTickI> ONE_TICK = List.of(
            new SrsiTick(START, 10, 3, 10, 11, 20, 2, 20, 21, 30),
            new SrsiTick(START, 10, 3, 10, 11, 20, 2, 20, 21, 30)
    );
    private static final List<SrsiTickI> TICKS = List.of(
            new SrsiTick(START, 10, 300, 10, 11, 2000, 2, 20, 21, 30),
            new SrsiTick(START.plusSeconds(1), 10, 350, 11, 11, 2000, 2, 20, 21, 30),
            new SrsiTick(START.plusSeconds(2), 10, 400, 12, 11, 2000, 2, 20, 21, 30),
            new SrsiTick(START.plusSeconds(3), 10, 200, -1, 11, 2000, 2, 20, 21, 30),
            new SrsiTick(START.plusSeconds(4), 10, 100, 10, 11, 2000, 2, 20, 21, 30)
//            new SrsiTick(START.plusSeconds(5), 300, 10, 11, 2000, 2, 20, 21, 30),
//            new SrsiTick(START.plusSeconds(6), 300, 10, 11, 2000, 2, 20, 21, 30),
//            new SrsiTick(START.plusSeconds(7), 300, 10, 11, 2000, 2, 20, 21, 30),
//            new SrsiTick(START.plusSeconds(8), 300, 10, 11, 2000, 2, 20, 21, 30)
    );
    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();
    private HistoryProcessor historyProcessor;
    @Autowired
    private DBCandleService candleService;
    @Autowired
    private HistoryProperties historyProperties;
    @Autowired
    private AppContextService appContextService;
    @Mock
    private TickService tickService;
    @Autowired
    private OrderServiceProperties orderServiceProperties;
    @Mock
    private SrsiTickService srsiTickService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        historyProperties.setStrategyName("srsi");
        historyProcessor = new HistoryProcessor(candleService, historyProperties, appContextService, tickService, orderServiceProperties, srsiTickService);
        historyProcessor.init(historyProperties);
        TradeStatusBuilder ts = new TradeStatusBuilder(historyProperties.getDeposit(), historyProperties.getFee(), historyProperties.getLeverage());
        historyProcessor.setTradeStatusBuilder(ts);
        historyProcessor.getStrategy().setTradeStatusBuilder(ts);
        changeProperties();
        initTicks();
    }

    @Test
    public void shouldProcessOneTrade() {
        Mockito.when(srsiTickService.load(START, END, historyProcessor.srsiVersion())).thenReturn(ONE_TICK);
        historyProcessor.handleSrsi(START, END);
        TradeStatusBuilder ts = historyProcessor.getTradeStatusBuilder();
        assertEquals("Trade count", 1, ts.getCount(), 0);
        assertEquals("Trade eff", -0.07, ts.getEffective(), 0);
        assertEquals("Deposit", 999.3, ts.getDeposit(), 0);
    }

    @Test
    public void shouldNotProcessOneTrade() {
        orderServiceProperties.setPriceChangeForUpdatePerc(0.1);
        orderServiceProperties.setDifForPricePerc(0.1);
        orderServiceProperties.setMaxUpdateCount(1);
        Mockito.when(srsiTickService.load(START, END, historyProcessor.srsiVersion())).thenReturn(ONE_TICK);
        historyProcessor.handleSrsi(START, END);
        TradeStatusBuilder ts = historyProcessor.getTradeStatusBuilder();
        assertEquals("Trade count", 0, ts.getCount(), 0);
    }

    @Test
    public void shouldProcessTrades() {
        orderServiceProperties.setPriceChangeForUpdatePerc(0.1);
        orderServiceProperties.setDifForPricePerc(0.1);
        orderServiceProperties.setMaxUpdateCount(1);
        Mockito.when(srsiTickService.load(START, END, historyProcessor.srsiVersion())).thenReturn(TICKS);
        historyProcessor.handleSrsi(START, END);
        TradeStatusBuilder ts = historyProcessor.getTradeStatusBuilder();
        assertEquals("Trade count", 1, ts.getCount(), 0);
        assertEquals("Trade eff", -90.94727272727273, ts.getEffective(), 0);
        assertEquals("Deposit", 90.5272727272727, ts.getDeposit(), 0);
    }

    private void changeProperties() {
        historyProcessor.getStrategy().getStrategyProperties().setStopLoseLong(Double.MAX_VALUE);
        historyProcessor.getStrategy().getStrategyProperties().setStopLoseShort(Double.MAX_VALUE);
        historyProcessor.getStrategy().getStrategyProperties().setTakeProfitLong(Double.MAX_VALUE);
        historyProcessor.getStrategy().getStrategyProperties().setTakeProfitShort(Double.MAX_VALUE);

        StrategyPropertiesI p = historyProcessor.getStrategy().getStrategyPropertiesI();
        if (p instanceof SrsiProperties) {
            SrsiProperties sp = (SrsiProperties) p;
            sp.setRsiDeltaSecondOpen(-19); //-18
            sp.setPriceDiffLongPerc(1000); //566
            sp.setRsiDeltaFirstOpen(-10); //-1
            sp.setKFirstMin(20); //10
            sp.setRsiDeltaOldOpenLong(20.0); //-9

            sp.setRsiDeltaFirstCloseLong(-0.5); //-1
        }
    }

    private void initTicks() {
        StrategyProperties sp = historyProcessor.getStrategy().getStrategyProperties();
        Mockito.when(tickService.getTickAtDate(sp.getServer(), sp.getSymbol(), START))
                .thenReturn(new Tick("", 0d, 1.0, START, true));
        Mockito.when(tickService.getTickAtDate(sp.getServer(), sp.getSymbol(), START.plusSeconds(1)))
                .thenReturn(new Tick("", 0d, 11.0, START.plusSeconds(1), true));
        Mockito.when(tickService.getTickAtDate(sp.getServer(), sp.getSymbol(), START.plusSeconds(2)))
                .thenReturn(new Tick("", 0d, 2.0, START.plusSeconds(2), true));
        Mockito.when(tickService.getTickAtDate(sp.getServer(), sp.getSymbol(), START.plusSeconds(3)))
                .thenReturn(new Tick("", 0d, 3.0, START.plusSeconds(3), true));
        Mockito.when(tickService.getTickAtDate(sp.getServer(), sp.getSymbol(), START.plusSeconds(4)))
                .thenReturn(new Tick("", 0d, 1.0, START.plusSeconds(4), true));
    }
}
