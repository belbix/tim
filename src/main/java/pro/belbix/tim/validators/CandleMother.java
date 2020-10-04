package pro.belbix.tim.validators;

import pro.belbix.tim.entity.Candle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandleMother {


    public static List<Candle> candleForValidate() {
        int time = 15;
        List<Candle> candles = new ArrayList<>();

        //o 3693
        candles.add(createCandle(
                LocalDateTime.parse("2019-01-01T00:00:00"),
                time,
                3693,
                3694.5,
                3684.5,
                3689
        ));

        candles.add(createCandle(
                LocalDateTime.parse("2019-01-01T00:15:00"),
                time,
                3689,
                3689,
                3686,
                3687.5
        ));

        candles.add(createCandle(
                LocalDateTime.parse("2019-01-01T00:30:00"),
                time,
                3687.5,
                3697.5,
                3687,
                3695
        ));

        candles.add(createCandle(
                LocalDateTime.parse("2019-01-01T00:45:00"),
                time,
                3695,
                3705,
                3692.5,
                3694
        ));


        return candles;
    }

    public static Candle createCandle(LocalDateTime date, int time, double open, double high, double low, double close) {
        Candle candle = new Candle();
        candle.setServer("bitmex");
        candle.setSymbol("XBTUSD");
        candle.setTime(time);
        candle.setDate(date);
        candle.setOpen(open);
        candle.setHigh(high);
        candle.setLow(low);
        candle.setClose(close);
        return candle;
    }

}
