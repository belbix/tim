package pro.belbix.tim.services;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.entity.Candle;

import java.time.LocalDateTime;
import java.util.List;

public interface ICandleService {
    List<Candle> loadCandles(CandleRequest candleRequest);

    List<Candle> loadMultiCandles(CandleRequest candleRequest);

    Candle candleFromTicks(Candle candle, LocalDateTime dateBefore, boolean onlyClose);

    Candle lastCandle(String server, String symbol, int timeframe);

    @Getter
    @Setter
    @ToString
    class CandleRequest {
        private int count;
        private LocalDateTime beforeDate;
        private LocalDateTime afterDate;
        private int timeFrame;
        private String symbol;
        private String server;
        private boolean firstDynamic = false;
        private boolean onlyClose = false;

        public boolean isValid() {
            return count != 0
                    && beforeDate != null
                    && timeFrame != 0
                    && symbol != null && !symbol.isBlank()
                    && server != null && !server.isBlank();
        }
    }
}
