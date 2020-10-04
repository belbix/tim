package pro.belbix.tim.utils;

import org.junit.Test;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class IndicatorsTest {

    @Test
    public void stochasticRsi() {
        List<Candle> candles = new ArrayList<>();
        candles.add(new Candle(3577.7));
        candles.add(new Candle(3653d));
        candles.add(new Candle(3586.1));
        candles.add(new Candle(3588.1));
        candles.add(new Candle(3587.6));
        candles.add(new Candle(3585d));
        candles.add(new Candle(3569d));
        candles.add(new Candle(3566.11));
        candles.add(new Candle(3568.2));
        candles.add(new Candle(3571.5));
        candles.add(new Candle(3573.1));
        candles.add(new Candle(3577.7));
        candles.add(new Candle(3579.27));
        candles.add(new Candle(3585.8));
        candles.add(new Candle(3587.34));
        candles.add(new Candle(3584.79));
        candles.add(new Candle(3589.9));
        candles.add(new Candle(3588.7));
        candles.add(new Candle(3587d));
        candles.add(new Candle(3590.3));
        candles.add(new Candle(3579.15));
        candles.add(new Candle(3579.9));
        candles.add(new Candle(3574.1));
        candles.add(new Candle(3653d));
        candles.add(new Candle(3717d));
        candles.add(new Candle(3768.9));
        candles.add(new Candle(3769.5));
        candles.add(new Candle(3769.8));
        candles.add(new Candle(3767.93));
        candles.add(new Candle(3767.2));
        candles.add(new Candle(3763.5));
        candles.add(new Candle(3766.2));
        candles.add(new Candle(3769.7));
        candles.add(new Candle(3766.8));
        candles.add(new Candle(3770.3));
        candles.add(new Candle(3767.1));
        candles.add(new Candle(3769.87));
        candles.add(new Candle(3772.1));
        candles.add(new Candle(3773.3));
        candles.add(new Candle(3775d));
        candles.add(new Candle(3777.1));
        candles.add(new Candle(3776.2));
        candles.add(new Candle(3780.44));
        candles.add(new Candle(3782.1));
        candles.add(new Candle(3785.1));
        candles.add(new Candle(3768.1));
        candles.add(new Candle(3766.1));
        candles.add(new Candle(3762.3));
        candles.add(new Candle(3766.2));
        candles.add(new Candle(3773.1));
        candles.add(new Candle(3779d));
        candles.add(new Candle(3777.8));
        candles.add(new Candle(3776.1));
        candles.add(new Candle(3780d));
        candles.add(new Candle(3777.14));
        candles.add(new Candle(3781.5));
        candles.add(new Candle(3782.6));
        candles.add(new Candle(3778.8));
        candles.add(new Candle(3774.8));
        candles.add(new Candle(3796.2));
        candles.add(new Candle(3793.2));
        candles.add(new Candle(3793.1));
        candles.add(new Candle(3792.1));
        candles.add(new Candle(3789.3));
        candles.add(new Candle(3791.9));
        candles.add(new Candle(3788.8));
        candles.add(new Candle(3791d));
        candles.add(new Candle(3796.1));
        candles.add(new Candle(3797.3));
        candles.add(new Candle(3808.7));
        candles.add(new Candle(3808.31));
        candles.add(new Candle(3808.9));
        candles.add(new Candle(3819.9));
        candles.add(new Candle(3825.6));
        candles.add(new Candle(3830.6));
        candles.add(new Candle(3711.9));
        candles.add(new Candle(3709d));
        candles.add(new Candle(3707.4));
        candles.add(new Candle(3706.98));
        candles.add(new Candle(3702.7));
        candles.add(new Candle(3703.7));
        candles.add(new Candle(3697d));
        candles.add(new Candle(3704.8));
        candles.add(new Candle(3703.8));
        candles.add(new Candle(3712.9));
        candles.add(new Candle(3706.58));
        candles.add(new Candle(3707.2));
        candles.add(new Candle(3706.12));
        candles.add(new Candle(3707.2));
        candles.add(new Candle(3708d));
        candles.add(new Candle(3699.7));
        candles.add(new Candle(3690.8));
        candles.add(new Candle(3681.1));
        candles.add(new Candle(3679.2));
        candles.add(new Candle(3680.9));
        candles.add(new Candle(3675.7));
        candles.add(new Candle(3681.5));
        candles.add(new Candle(3676.4));
        candles.add(new Candle(3678.4));
        candles.add(new Candle(3678.8));

//        candles = Lists.reverse(candles);

        LocalDateTime now = LocalDateTime.now();
        Indicators.stochasticRsi(candles);
        System.out.println("srsi time: " + Duration.between(now, LocalDateTime.now()).toNanos() / 1000);
        for (Candle candle : candles) {
//            System.out.println(candle.getClose().intValue() + " k:" + candle.getSlowk() + " d:" + candle.getSlowd());
        }
    }

    @Test
    public void stochasticRsi2() {
        List<Candle> candles = new ArrayList<>();
        for (int i = 0; i < 200000; i++) {
            candles.add(new Candle((Math.random() * 100000) + i * 1000 + 100000));
        }


        LocalDateTime now = LocalDateTime.now();
        Indicators.stochasticRsi(candles);
        System.out.println("srsi time: " + Duration.between(now, LocalDateTime.now()).toNanos() / 1000);
//        for (Candle candle : candles) {
//            System.out.println(candle.getClose().intValue() + " k:" + candle.getSlowk() + " d:" + candle.getSlowd());
//        }
    }

    @Test
    public void convolutedTicks() {
        List<Tick> ticks = new ArrayList<>();
        ticks.add(new Tick("1", 10d, 100d, LocalDateTime.parse("2019-01-01T00:00:01"), true));
        ticks.add(new Tick("2", 10d, 101d, LocalDateTime.parse("2019-01-01T00:00:02"), true));
        ticks.add(new Tick("3", 10d, 102d, LocalDateTime.parse("2019-01-01T00:00:03"), true));
        ticks.add(new Tick("4", 10d, 103d, LocalDateTime.parse("2019-01-01T00:00:04"), true));
        ticks.add(new Tick("5", 10d, 104d, LocalDateTime.parse("2019-01-01T00:00:05"), true));
        ticks.add(new Tick("6", 10d, 105d, LocalDateTime.parse("2019-01-01T00:00:06"), true));
        ticks.add(new Tick("7", 10d, 106d, LocalDateTime.parse("2019-01-01T00:00:06"), false));
        ticks.add(new Tick("8", 10d, 107d, LocalDateTime.parse("2019-01-01T00:00:06"), true));
        ticks.add(new Tick("9", 10d, 108d, LocalDateTime.parse("2019-01-01T00:00:07"), true));
        ticks.add(new Tick("10", 10d, 109d, LocalDateTime.parse("2019-01-01T00:00:07"), true));
        ticks.add(new Tick("11", 10d, 110d, LocalDateTime.parse("2019-01-01T00:00:07"), true));
        ticks.add(new Tick("12", 10d, 110d, LocalDateTime.parse("2019-01-01T00:00:07"), true));
        ticks.add(new Tick("13", 10d, 111d, LocalDateTime.parse("2019-01-01T00:00:07"), true));
        ticks.add(new Tick("14", 10d, 111d, LocalDateTime.parse("2019-01-01T00:00:08"), true));
        ticks.add(new Tick("15", 10d, 111d, LocalDateTime.parse("2019-01-01T00:00:08"), true));
        ticks.add(new Tick("16", 10d, 112d, LocalDateTime.parse("2019-01-01T00:00:08"), true));
        ticks.add(new Tick("17", 10d, 112d, LocalDateTime.parse("2019-01-01T00:00:08"), true));
        ticks.add(new Tick("18", 10d, 112d, LocalDateTime.parse("2019-01-01T00:00:09"), true));
        ticks.add(new Tick("19", 10d, 112d, LocalDateTime.parse("2019-01-01T00:00:09"), true));
        ticks.add(new Tick("20", 10d, 112d, LocalDateTime.parse("2019-01-01T00:00:09"), true));
        ticks.add(new Tick("21", 10d, 113d, LocalDateTime.parse("2019-01-01T00:00:09"), false));
        ticks.add(new Tick("22", 10d, 113d, LocalDateTime.parse("2019-01-01T00:00:09"), false));
        ticks.add(new Tick("23", 10d, 110d, LocalDateTime.parse("2019-01-01T00:00:11"), true));
        ticks.add(new Tick("24", 10d, 110d, LocalDateTime.parse("2019-01-01T00:00:11"), true));
        ticks.add(new Tick("25", 10d, 109d, LocalDateTime.parse("2019-01-01T00:00:11"), true));
        ticks.add(new Tick("26", 10d, 107d, LocalDateTime.parse("2019-01-01T00:00:11"), true));

        TreeSet<Tick> convTicks = Indicators.convolutedTicks(ticks, true, 3);

        Indicators.calcVelocity(convTicks);

        for (Tick tick : convTicks) {
            System.out.println(tick);
        }

        Candle c = Indicators.generateConvCandle(convTicks, 1);
        System.out.println(c);
    }
}
