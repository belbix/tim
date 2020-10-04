package pro.belbix.tim.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.services.TickService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static pro.belbix.tim.utils.Common.validateTicks;

public class TickValidator {
    private static final Logger log = LoggerFactory.getLogger(TickValidator.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        TickService tickService = context.getBean(TickService.class);

        String server = "bitmex";
        String symbol = "XBTUSD";
        int betweenSec = 600;
        int hours = 24;
        if (args.length == 1) betweenSec = Integer.parseInt(args[0]);
        if (args.length == 2) hours = Integer.parseInt(args[1]);

        LocalDateTime minDate = tickService.getMinDateFromDb(server, symbol);
        if (minDate == null) return;

        LocalDateTime maxDate = tickService.getMaxDateFromDb(server, symbol);

        while (minDate.isBefore(maxDate)) {
            log.info("minDate: " + minDate + " currentSrsiMax date: " + maxDate);
            LocalDateTime beforeDate = minDate.plus(hours, ChronoUnit.HOURS);
            List<Tick> ticks = tickService.getTicksCached(server, symbol, minDate, beforeDate, false);
            log.info("Load ticks:" + ticks.size());
            validateTicks(ticks, betweenSec);
            minDate = beforeDate.minus(1, ChronoUnit.MINUTES);
        }
        log.info("Done");
        System.exit(0);
    }


}
