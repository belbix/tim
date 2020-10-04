package pro.belbix.tim.entity;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class Srsi2TickTest {

    private static boolean compareCandles(Candle c1, Candle c2) {
        if (!c1.getTime().equals(c2.getTime())) return false;
        if (!c1.getSlowk().equals(c2.getSlowk())) return false;
        if (!c1.getSlowd().equals(c2.getSlowd())) return false;
        if (c1.getClose() != null && !c1.getClose().equals(c2.getClose())) return false;
        if (c1.getLow() != null && !c1.getLow().equals(c2.getLow())) return false;
        return true;
    }

    @Test
    public void createFromCandle() {
        List<Candle> candles = new ArrayList<>();

        Candle c1 = new Candle();
        c1.setClose(1d);
        c1.setSlowk(11.1235);
        c1.setSlowd(111d);
        c1.setTime(55);
        candles.add(c1);

        Candle c2 = new Candle();
        c2.setClose(2d);
        c2.setSlowk(22d);
        c2.setSlowd(222d);
        c2.setTime(55);
        candles.add(c2);

        Candle c3 = new Candle();
        c3.setSlowk(33d);
        c3.setSlowd(333d);
        c3.setTime(55);
        candles.add(c3);

        LocalDateTime now = LocalDateTime.now();

        Srsi2Tick t = (Srsi2Tick) Srsi2Tick.createFromCandle(candles, now, 3);

        assertEquals(t.getFirstClose(), 1d, 0);
        assertEquals(t.getSecondClose(), 2d, 0);
        assertEquals("11.1235 111.0;22.0 222.0;33.0 333.0", t.getSrsi());

        List<Candle> tCandles = t.toCandles(55);
        assertEquals(candles.size(), tCandles.size());
        for (int i = 0; i < candles.size(); i++) {
            Candle tCandle = tCandles.get(i);
            assertNotNull(tCandle);
            assertTrue(compareCandles(candles.get(i), tCandle));
        }
    }
}
