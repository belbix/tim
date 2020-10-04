package pro.belbix.tim.validators;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.PageRequest;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.SrsiTickI;
import pro.belbix.tim.repositories.SrsiTickRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.temporal.ChronoUnit.MONTHS;

public class SrsiValidator {
    private static final Logger log = LoggerFactory.getLogger(SrsiValidator.class);

    public static void main(String... args) {
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        SrsiTickRepository srsiTickRepository = context.getBean(SrsiTickRepository.class);
        LocalDateTime lastDate = srsiTickRepository.lastDate(10, PageRequest.of(0, 1)).get(0);
        List<SrsiTickI> srsiTicks;
        do {
            LocalDateTime dateStart = lastDate.minus(1, MONTHS);
            srsiTicks = srsiTickRepository.load(dateStart, lastDate, 10);
            for (int i = 0; i < srsiTicks.size() - 1; i++) {
                Duration d = Duration.between(srsiTicks.get(i).getDate(), srsiTicks.get(i + 1).getDate());
                if (d.toSeconds() > 11) {
                    log.warn(srsiTicks.get(i).getDate().toString() + " is too long! " + d);
                }
            }
            lastDate = dateStart;
        } while (srsiTicks.size() != 0);

    }

}
