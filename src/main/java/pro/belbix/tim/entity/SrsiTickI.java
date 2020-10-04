package pro.belbix.tim.entity;

import java.time.LocalDateTime;
import java.util.List;

public interface SrsiTickI {
    LocalDateTime getDate();

    double getFirstClose();

    List<Candle> toCandles(int timeframe);

    boolean compare(SrsiTickI t);
}
