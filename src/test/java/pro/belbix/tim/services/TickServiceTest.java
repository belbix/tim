package pro.belbix.tim.services;

import org.junit.After;
import org.junit.Before;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;

import java.time.LocalDateTime;
import java.util.List;

import static pro.belbix.tim.utils.Common.fullValidateTicks;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SimpleApp.class)
public class TickServiceTest {

    //    @Autowired
    private TickService tickService;

    @Before
    public void setUp() throws Exception {
        tickService.clearCache();
    }

    @After
    public void tearDown() throws Exception {
        tickService.clearCache();
    }

    //    @Test
    public void test() {
        getTopTicksBeforeDateCached(LocalDateTime.parse("2019-01-01T06:00:00"));
        getTopTicksBeforeDateCached(LocalDateTime.parse("2019-01-01T06:00:01"));
        getTopTicksBeforeDateCached(LocalDateTime.parse("2019-01-01T06:00:02"));
        getTopTicksBeforeDateCached(LocalDateTime.parse("2019-01-01T06:00:03"));
        getTopTicksBeforeDateCached(LocalDateTime.parse("2019-01-01T06:00:04"));
    }

    public void getTopTicksBeforeDateCached(LocalDateTime before) {
        List<Tick> ticks = tickService.getTopTicksBeforeDateCached("bitmex", "XBTUSD", before, 5);
        fullValidateTicks(ticks);
    }


    //    @Test
    public void candleFromTicksTest() {
        LocalDateTime start = LocalDateTime.parse("2019-05-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2019-05-02T00:00:00");
        List<Tick> ticks = tickService.getTicks("bitmex", "XBTUSD", start, end, false);
//        ticks.forEach(System.out::println);
        int timeframe = 1440;
        LocalDateTime dateAfter = Candle.calcStartDateFromTimeFrame(start, timeframe, Candle.Accuracy.DAY);
        Candle candle = TickService.candleFromTicks(ticks, timeframe,
                dateAfter,
                end, true);
        System.out.println(candle);

    }

    //    @Test
    public void candleFromTicksTestClose() {
        LocalDateTime start = LocalDateTime.parse("2019-05-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2019-05-02T00:00:00");
        List<Tick> ticks = tickService.getTicks("bitmex", "XBTUSD", start, end, true);
//        ticks.forEach(System.out::println);
        int timeframe = 1440;
        LocalDateTime dateAfter = Candle.calcStartDateFromTimeFrame(start, timeframe, Candle.Accuracy.DAY);
        Candle candle = TickService.candleFromTicks(ticks, timeframe,
                dateAfter,
                end, true);
        System.out.println(candle);

    }
}
