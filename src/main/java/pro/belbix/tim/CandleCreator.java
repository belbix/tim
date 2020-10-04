package pro.belbix.tim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.properties.TickDownloaderProperties;
import pro.belbix.tim.services.DBCandleService;
import pro.belbix.tim.services.TickService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;


public class CandleCreator {
    private static final Logger log = LoggerFactory.getLogger(CandleCreator.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        context.getBean(CandleCreatorInner.class).start();
    }

    @Component
    public static class CandleCreatorInner {
        private final DBCandleService candleService;
        private final TickService tickService;
        private final TickDownloaderProperties tickDownloaderProperties;

        @Autowired
        public CandleCreatorInner(DBCandleService candleService, TickService tickService, TickDownloaderProperties tickDownloaderProperties) {
            this.candleService = candleService;
            this.tickService = tickService;
            this.tickDownloaderProperties = tickDownloaderProperties;
        }


        public void start() {
            log.info("createCandle new started");

            String server = tickDownloaderProperties.getServer();
            String symbol = tickDownloaderProperties.getSymbol();
            Set<Integer> timeframes = tickDownloaderProperties.getTimeframes();
            LocalDateTime minTickDateFromDb = tickService.getMinDateFromDb(server, symbol);
            int maxTimeFrame = candleService.maxTimeFrame(tickDownloaderProperties.getTimeframes());
            if (minTickDateFromDb == null) {
                log.info("Empty ticks");
                return;
            }
            log.info("minTickDateFromDb: " + minTickDateFromDb);
            minTickDateFromDb = Candle.calcStartDateFromTimeFrame(minTickDateFromDb, maxTimeFrame, Candle.Accuracy.DAY);
            log.info("calculated minTickDateFromDb: " + minTickDateFromDb);
            LocalDateTime maxTickDateFromDb = tickService.getMaxDateFromDb(server, symbol);
            log.info("maxTickDateFromDb: " + maxTickDateFromDb);
            LocalDateTime start = minTickDateFromDb;

            TreeSet<Tick> treeForCache = new TreeSet<>();
//            int refreshCount = 0;
            Tick tickForSeparate = null;
            while (start.isBefore(maxTickDateFromDb)) {
                Instant logTime = Instant.now();
                LocalDateTime end = start.plus(maxTimeFrame, ChronoUnit.MINUTES);
                log.info("Start iterate " + end);
                List<Candle> candles = candleService.processTicksForCreatingCandles(
                        server, symbol, treeForCache, start, end, timeframes, Candle.Accuracy.DAY,
                        tickDownloaderProperties.isOnlyClose());
                if (candles != null) {
                    log.info("Candles for save: " + candles.size());
                    for (Candle candle : candles) {
                        candleService.saveCandle(candle, true);
                    }
                }

                if (tickDownloaderProperties.isUseCache()) {
                    if (tickForSeparate == null) {
                        tickForSeparate = treeForCache.last();
                        log.info("Tick for separate: " + tickForSeparate.getDate());
                    } else {
                        treeForCache.headSet(tickForSeparate).clear();
                        tickForSeparate = null;
                        log.info("after separate start: " + treeForCache.first().getDate() + " end: " + treeForCache.last().getDate());
                    }
                } else {
                    treeForCache.clear();
                }

                log.info("Iterate complete for: " + Duration.between(logTime, Instant.now()) + ", treeForCache:" + treeForCache.size());
                start = end;
            }
            log.info("createCandle finished");
            System.exit(1);
        }


    }
}
