package pro.belbix.tim.validators;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.exchanges.bitmex.BitmexREST;
import pro.belbix.tim.services.DBCandleService;
import pro.belbix.tim.services.ICandleService;
import pro.belbix.tim.utils.Common;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CandleValidator {
    private static final Logger log = LoggerFactory.getLogger(CandleValidator.class);

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        DBCandleService candleService = context.getBean(DBCandleService.class);
        BitmexREST bitmexREST = context.getBean(BitmexREST.class);
        validate(candleService, bitmexREST);
        validateDateAndOpenClose(candleService);
    }

    private static void validate(DBCandleService candleService, BitmexREST bitmexREST) throws Exception {
        ICandleService.CandleRequest r = new ICandleService.CandleRequest();
        LocalDateTime after = LocalDateTime.parse("2019-01-01T00:00:00");
        int countCandle = 500;
        String timeframe = "5m";
        r.setServer("bitmex");
        r.setSymbol("XBTUSD");
        r.setTimeFrame(Common.minutesFromPeriod(timeframe));
        r.setAfterDate(after);
        r.setCount(countCandle);
        List<Candle> candles = candleService.loadCandlesAfter(r);
        List<Candle> candlesForValidate = bitmexREST.candles("XBTUSD", timeframe, after, null, countCandle);
        log.info("Candles: " + candles.size());
        int count = 0;
        for (Candle c : candles) {

            Candle cfv = candlesForValidate.get(count);

//            log.info("From bd:" + c);
//            log.info("From ex:" + cfv);
            mergeUnvalidateFields(c, cfv);

            assertEquals(c.getServer(), cfv.getServer());
            assertEquals(c.getSymbol(), cfv.getSymbol());
            assertEquals(c.getDate(), cfv.getDate());
            assertEquals(c.getTime(), cfv.getTime());

            double open = c.getOpen() - cfv.getOpen();
            double high = c.getHigh() - cfv.getHigh();
            double low = c.getLow() - cfv.getLow();
            double close = c.getClose() - cfv.getClose();
//            if (open + high + low + close > 5) {
//                log.info(c.getDate() + " o:" + open + " h:" + high + " l:" + low + " c:" + close);
//            }

            count++;
        }
    }

    private static void mergeUnvalidateFields(Candle fromDb, Candle exist) {
        exist.setId(fromDb.getId());
        exist.setAmount(fromDb.getAmount());
//        exist.setVolume(fromDb.getVolume());
    }


    public static void validateDateAndOpenClose(DBCandleService candleService) {
        LocalDateTime start = LocalDateTime.parse("2019-01-01T00:00:00");
        int timeFrame = 720;
        ICandleService.CandleRequest r = new ICandleService.CandleRequest();
        r.setServer("bitmex");
        r.setSymbol("XBTUSD");
        r.setTimeFrame(timeFrame);
        r.setCount(60);
        LocalDateTime end = LocalDateTime.now();
        Candle lastCandle = null;
        while (start.isBefore(end)) {
            r.setAfterDate(start);
            List<Candle> candles = candleService.loadCandlesAfter(r);
            if (candles == null || candles.isEmpty()) {
//                log.info("Empty candles");
                break;
            }
            Candle candle = candles.iterator().next();
//            log.info(candle.toString());
            if (lastCandle != null) {
                assertEquals(lastCandle.getDate().plus(timeFrame, ChronoUnit.MINUTES), candle.getDate());
                assertEquals(lastCandle.getTime(), candle.getTime());
                assertEquals(lastCandle.getServer(), candle.getServer());
                assertEquals(lastCandle.getSymbol(), candle.getSymbol());
                assertEquals(lastCandle.getClose(), candle.getOpen());
            }
            lastCandle = candle;

            start = start.plus(timeFrame, ChronoUnit.MINUTES);
        }

    }

    private static void assertEquals(LocalDateTime expected, LocalDateTime actual) {
        if (expected.equals(actual)) return;
        throw new IllegalStateException("Nor equal, expected: " + expected + " actual: " + actual);
    }

    private static void assertEquals(int expected, int actual) {
        if (expected == actual) return;
        throw new IllegalStateException("Nor equal, expected: " + expected + " actual: " + actual);
    }

    private static void assertEquals(double expected, double actual) {
        if (expected == actual) return;
        throw new IllegalStateException("Nor equal, expected: " + expected + " actual: " + actual);
    }

    private static void assertEquals(String expected, String actual) {
        if (expected.equals(actual)) return;
        throw new IllegalStateException("Nor equal, expected: " + expected + " actual: " + actual);
    }
}
