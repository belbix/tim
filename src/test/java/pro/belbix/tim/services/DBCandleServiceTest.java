package pro.belbix.tim.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.utils.Common;

import java.time.LocalDateTime;
import java.util.List;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SimpleApp.class)
public class DBCandleServiceTest {
    //    @Autowired
    private DBCandleService candleService;

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
    public void loadCandles() {
        ICandleService.CandleRequest candleRequest = new ICandleService.CandleRequest();
        candleRequest.setServer("bitfinex");
        candleRequest.setSymbol("BTCUSD");
        candleRequest.setTimeFrame(Common.minutesFromPeriod("6h"));
        candleRequest.setBeforeDate(LocalDateTime.now());
        candleRequest.setCount(100);
        List<Candle> candles = candleService.loadCandles(candleRequest);
//        for(Candle candle: candles){
//            System.out.println(candle);
//        }
    }

    //    @Test
    public void firstCandle() {
        String server = "bitmex";
        String symbol = "XBTUSD";
        Integer timeframe = 5;
        LocalDateTime candleDate = LocalDateTime.parse("2017-12-25T20:00:00");
        LocalDateTime dateBefore = LocalDateTime.parse("2017-12-25T20:03:59");

        Candle candle = new Candle();
        candle.setServer(server);
        candle.setSymbol(symbol);
        candle.setTime(timeframe);
        candle.setDate(candleDate);
        candle = candleService.candleFromTicks(candle, dateBefore, false);
        System.out.println("d candle:" + candle);

        Assert.assertEquals(server, candle.getServer());
        Assert.assertEquals(symbol, candle.getSymbol());
        Assert.assertEquals(timeframe, candle.getTime());
        Assert.assertEquals(candleDate, candle.getDate());

        Assert.assertEquals(118, candle.getAmount(), 0.0);
        Assert.assertEquals(14009.5, candle.getClose(), 0.0);
        Assert.assertEquals(14025.5, candle.getHigh(), 0.0);
        Assert.assertEquals(13990.0, candle.getLow(), 0.0);
        Assert.assertEquals(13990.5, candle.getOpen(), 0.0);
    }

    //    @Test
    public void loadMultiCandlesTest() {
        ICandleService.CandleRequest request = new ICandleService.CandleRequest();
        request.setServer("bitmex");
        request.setSymbol("XBTUSD");
        request.setTimeFrame(10);
        request.setCount(5);
        request.setBeforeDate(LocalDateTime.parse("2019-05-05T00:57:00"));

        List<Candle> candles = candleService.loadMultiCandles(request);
        Assert.assertEquals(candles.size(), request.getCount());
        candles.forEach(System.out::println);
    }
}
