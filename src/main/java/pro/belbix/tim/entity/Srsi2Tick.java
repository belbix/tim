package pro.belbix.tim.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "srsi2", indexes = {
        @Index(name = "idx_srsi2", columnList = "date")
})
@Cacheable(false)
@Getter
@Setter
@ToString
public class Srsi2Tick implements SrsiTickI {
    @Transient
    private static final String CANDLE_DELIMITER = ";";
    @Transient
    private static final String SRSI_DELIMITER = " ";
    @Id
    private LocalDateTime date;
    private double firstClose;
    private double secondClose;
    private String srsi;


    public static SrsiTickI createFromCandle(List<Candle> candles, LocalDateTime date, int deep) {
        Srsi2Tick srsiTick = new Srsi2Tick();
        srsiTick.setDate(date);
        srsiTick.setFirstClose(candles.get(0).getClose());
        srsiTick.setSecondClose(candles.get(1).getClose());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deep; i++) {
            Candle c = candles.get(i);
            sb.append(doubleToString(c.getSlowk()))
                    .append(SRSI_DELIMITER)
                    .append(doubleToString(c.getSlowd()))
                    .append(CANDLE_DELIMITER);
        }
        sb.setLength(sb.length() - 1);
        srsiTick.setSrsi(sb.toString());
        return srsiTick;
    }

    private static String doubleToString(Double d) {
        int x = 10000;
        d = d * x;
        d = (double) Math.round(d);
        return String.valueOf(d / x);
    }

    public List<Candle> toCandles(int timeframe) {
        List<Candle> candles = new ArrayList<>();
        for (String s : srsi.split(CANDLE_DELIMITER)) {
            String[] srsi = s.split(SRSI_DELIMITER);
            if (srsi.length < 2) {
                throw new IllegalStateException("Wrong srsi in tick " + this.toString());
            }
            Candle candle = new Candle();
            candle.setSlowk(Double.parseDouble(srsi[0]));
            candle.setSlowd(Double.parseDouble(srsi[1]));
            candle.setTime(timeframe);
            candles.add(candle);
        }
        if (candles.size() < 2)
            throw new IllegalStateException("To low candles in " + this.toString());
        candles.get(0).setClose(firstClose);
        candles.get(1).setClose(secondClose);
        return candles;
    }

    public boolean compare(SrsiTickI o) {
        if (!(o instanceof Srsi2Tick)) {
            return false;
        }
        Srsi2Tick t = (Srsi2Tick) o;
        double firstCloseD = Math.abs(t.getFirstClose() - firstClose);
        double secondLowD = Math.abs(t.getSecondClose() - secondClose);
        return firstCloseD == 0
                && secondLowD == 0
                && srsi.equals(t.getSrsi());
    }


}
