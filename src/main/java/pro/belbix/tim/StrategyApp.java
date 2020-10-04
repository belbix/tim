package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.services.OrdersWatcher;
import pro.belbix.tim.services.SchedulerService;

import java.util.TimeZone;

import static pro.belbix.tim.utils.Common.addStrategyInSchedule;


public class StrategyApp {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);

        SchedulerService schedulerService = context.getBean(SchedulerService.class);

        OrdersWatcher ordersWatcher = context.getBean(OrdersWatcher.class);
        schedulerService.addSchedulable(ordersWatcher);

        addStrategyInSchedule(context, schedulerService);
    }

}

