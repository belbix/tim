package pro.belbix.tim.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pro.belbix.tim.entity.Candle.calcStartDateFromTimeFrame;

@Entity
@Table(name = "srsi", indexes = {
        @Index(name = "idx_srsi", columnList = "date, ticktime")
})
@Cacheable(false)
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SrsiTick implements SrsiTickI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime date;
    private int ticktime;

    private double firstClose;
    private double firstSlowk;
    private double firstSlowd;

    private double secondClose;
    private double secondSlowk;
    private double secondSlowd;

    private double thirdSlowk;
    private double thirdSlowd;

    public SrsiTick(LocalDateTime date, int ticktime, double firstClose, double firstSlowk, double firstSlowd, double secondClose, double secondSlowk, double secondSlowd, double thirdSlowk, double thirdSlowd) {
        this.date = date;
        this.ticktime = ticktime;
        this.firstClose = firstClose;
        this.firstSlowk = firstSlowk;
        this.firstSlowd = firstSlowd;
        this.secondClose = secondClose;
        this.secondSlowk = secondSlowk;
        this.secondSlowd = secondSlowd;
        this.thirdSlowk = thirdSlowk;
        this.thirdSlowd = thirdSlowd;
    }

    public static SrsiTickI createFromCandle(List<Candle> candles, LocalDateTime date, int ticktime) {
        Candle first = candles.get(0);
        Candle second = candles.get(1);
        Candle third = candles.get(2);

        SrsiTick t = new SrsiTick();
        t.setDate(date);
        t.setTicktime(ticktime);

        t.setFirstClose(first.getClose());
        t.setFirstSlowk(first.getSlowk());
        t.setFirstSlowd(first.getSlowd());

        t.setSecondClose(second.getClose());
        t.setSecondSlowk(second.getSlowk());
        t.setSecondSlowd(second.getSlowd());

        t.setThirdSlowk(third.getSlowk());
        t.setThirdSlowd(third.getSlowd());
        return t;
    }

    @Override
    public List<Candle> toCandles(int timeframe) {
        List<Candle> candles = new ArrayList<>();

        Candle first = new Candle();
        first.setDate(calcStartDateFromTimeFrame(date, timeframe, Candle.Accuracy.DAY));
        first.setTickTime(date);
        first.setTime(timeframe);
        first.setClose(firstClose);
        first.setSlowk(firstSlowk);
        first.setSlowd(firstSlowd);
        candles.add(first);

        Candle second = new Candle();
        second.setTime(timeframe);
        second.setClose(secondClose);
        second.setSlowk(secondSlowk);
        second.setSlowd(secondSlowd);
        candles.add(second);

        Candle third = new Candle();
        third.setTime(timeframe);
        third.setSlowk(thirdSlowk);
        third.setSlowd(thirdSlowd);
        candles.add(third);

        return candles;
    }

    @Override
    public boolean compare(SrsiTickI o) {
        if (!(o instanceof SrsiTick)) {
            return false;
        }
        SrsiTick t = (SrsiTick) o;
        double firstCloseD = Math.abs(t.getFirstClose() - firstClose);
        double firstSlowdD = Math.abs(t.getFirstSlowd() - firstSlowd);
        double firstSlowkD = Math.abs(t.getFirstSlowk() - firstSlowk);

        double secondLowD = Math.abs(t.getSecondClose() - secondClose);
        double secondSlowdD = Math.abs(t.getSecondSlowd() - secondSlowd);
        double secondSlowkD = Math.abs(t.getSecondSlowk() - secondSlowk);

        double thirdSlowdD = Math.abs(t.getThirdSlowd() - thirdSlowd);
        double thirdSlowkD = Math.abs(t.getThirdSlowk() - thirdSlowk);
        return firstCloseD == 0
                && firstSlowdD == 0
                && firstSlowkD == 0
                && secondLowD == 0
                && secondSlowdD == 0
                && secondSlowkD == 0
                && thirdSlowdD == 0
                && thirdSlowkD == 0;
    }


}
