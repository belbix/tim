package pro.belbix.tim;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.services.DBCandleService;
import pro.belbix.tim.services.ICandleService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

public class CreateDatasetFromCandles {
    private static final Logger log = LoggerFactory.getLogger(CreateDatasetFromCandles.class);

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        context.getBean(CreateDatasetFromCandles.CreateDatasetInner.class).start();
    }

    @Component
    public static class CreateDatasetInner {
        private final DBCandleService candleService;

        @Autowired
        public CreateDatasetInner(DBCandleService candleService) {
            this.candleService = candleService;
        }


        public void start() {
            log.info("CreateDatasetFromCandles new started");

            String server = "bitmex";
            String symbol = "XBTUSD";
            int timeframe = 1;
            int batch = 500;
            LocalDateTime afterDate = LocalDateTime.parse("2019-01-01T00:00:00");

            ICandleService.CandleRequest candleRequest = new ICandleService.CandleRequest();
            candleRequest.setCount(batch);
            candleRequest.setAfterDate(afterDate);
            candleRequest.setTimeFrame(timeframe);
            candleRequest.setSymbol(symbol);
            candleRequest.setServer(server);

            FileWriter fw = null;
            File file = new File("/trainsets/candles/" + server + symbol + timeframe + ".txt");
            try {
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                fw = new FileWriter(file);
            } catch (IOException e) {
                log.error(e.getMessage());
                System.exit(1);
            }

            while (true) {
                List<Candle> candles = candleService.loadCandlesAfter(candleRequest);
                if (candles == null || candles.size() < batch) break;
                log.info("Loaded candles " + candles.size());
                StringBuilder sb = new StringBuilder();
                for (Candle candle : candles) {
                    sb.append(candle.getClose()).append(",");
                }
                sb.setLength(sb.length() - 1);
                try {
                    fw.append(sb);
                    fw.append("\n");
                    fw.flush();
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                afterDate = candles.get(candles.size() - 1).getDate();
                candleRequest.setAfterDate(afterDate);
            }

            try {
                fw.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }

            log.info("CreateDatasetFromCandles finished");
            System.exit(1);
        }


    }

}
