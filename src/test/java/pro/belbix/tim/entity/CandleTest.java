package pro.belbix.tim.entity;

import org.junit.Assert;
import org.junit.Test;
import pro.belbix.tim.validators.CandleMother;

import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;

public class CandleTest {

    @Test
    public void calcDeltaK() {
    }

    @Test
    public void calcDifWithPrev() {
        double d = CandleMother.createCandle(LocalDateTime.now(), 0, 0, 0, 0, 30)
                .calcDiffWithPrev(
                        CandleMother.createCandle(LocalDateTime.now(), 0, 0, 0, 0, 120)
                );
        assertEquals("Calc with previous", 300, d, 0.0);

        d = CandleMother.createCandle(LocalDateTime.now(), 0, 0, 0, 0, 120)
                .calcDiffWithPrev(
                        CandleMother.createCandle(LocalDateTime.now(), 0, 0, 0, 0, 30)
                );
        assertEquals("Calc with previous", -75.0, d, 0.0);
    }

    @Test
    public void isValidDate() {
    }

    @Test
    public void calcStartDateFromTimeFrame() {
        LocalDateTime min = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T11:12:33"), 1, Candle.Accuracy.DAY);
        System.out.println(min);
        Assert.assertEquals(0, min.getSecond());

        LocalDateTime min3 = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T00:13:33"), 3, Candle.Accuracy.DAY);
        System.out.println(min3);
        Assert.assertEquals(0, min3.getSecond());

        LocalDateTime min7 = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T07:17:33"), 7, Candle.Accuracy.DAY);
        System.out.println(min7);
        Assert.assertEquals(0, min7.getSecond());

        LocalDateTime min10 = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T11:12:33"), 10, Candle.Accuracy.DAY);
        System.out.println(min10);
        Assert.assertEquals(0, min10.getSecond());

        LocalDateTime hour = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T11:12:33"), 60, Candle.Accuracy.DAY);
        System.out.println(hour);
        Assert.assertEquals(0, hour.getSecond());
        Assert.assertEquals(0, hour.getMinute());

        LocalDateTime hour6 = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T04:12:33"), 360, Candle.Accuracy.DAY);
        System.out.println(hour6);
        Assert.assertEquals(0, hour6.getSecond());
        Assert.assertEquals(0, hour6.getMinute());

        LocalDateTime day = Candle.calcStartDateFromTimeFrame(LocalDateTime.parse("2019-05-14T04:12:33"), 1440, Candle.Accuracy.DAY);
        System.out.println(day);
        Assert.assertEquals(0, day.getSecond());
        Assert.assertEquals(0, day.getMinute());
        Assert.assertEquals(0, day.getHour());

    }
}
