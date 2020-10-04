package pro.belbix.tim.validators;

import pro.belbix.tim.entity.Tick;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TickMother {


    public static List<Tick> buildTicksForCandle(LocalDateTime start, LocalDateTime end, double stepSecond) {
        List<Tick> ticks = new ArrayList<>();
        double count = 1;
        while (start.isBefore(end)) {
            Tick tick = new Tick();
            tick.setStrId(UUID.randomUUID().toString());
            tick.setDate(start);
            tick.setServer("bitmex");
            tick.setSymbol("XBTUSD");
            tick.setPrice(count);
            tick.setAmount(1d);
            tick.setBuy(true);
            ticks.add(tick);
            count++;
            start = start.plus(Math.round(stepSecond), ChronoUnit.SECONDS);
        }
        return ticks;
    }

}
