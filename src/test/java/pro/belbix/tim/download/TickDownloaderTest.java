package pro.belbix.tim.download;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.properties.TickDownloaderProperties;
import pro.belbix.tim.services.TickService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class TickDownloaderTest {
    @Autowired
    private TickDownloader tickDownloader;

    @Autowired
    private TickDownloaderProperties tickDownloaderProperties;

    @Autowired
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
    public void loadTicks() {
        tickDownloader.initStartTime();
        tickDownloader.findExchanges();
        LocalDateTime startDate = LocalDateTime.now().minus(1, ChronoUnit.MINUTES);
        List<Tick> ticks = tickDownloader.loadTicks(startDate);
        Assert.assertNotNull(ticks);
        Assert.assertFalse(ticks.isEmpty());
    }

    @Test
    public void buildCandleTest() {
        tickDownloaderProperties.setBuildCandle(true);
        List<Candle> candles = tickDownloader.buildCandlesNew(LocalDateTime.parse("2019-01-01T00:00:00"));
//        Assert.assertNotNull(candles);
//        Assert.assertTrue(candles.size() != 0);
    }
}
