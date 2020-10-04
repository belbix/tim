package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.download.TickDownloader;
import pro.belbix.tim.services.OrdersWatcher;
import pro.belbix.tim.services.SchedulerService;

import static pro.belbix.tim.utils.Common.addStrategyInSchedule;


public class LoadAndTradeApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);

        SchedulerService schedulerService = context.getBean(SchedulerService.class);
        schedulerService.addSchedulable(context.getBean(TickDownloader.class));

        OrdersWatcher ordersWatcher = context.getBean(OrdersWatcher.class);
        schedulerService.addSchedulable(ordersWatcher);

        addStrategyInSchedule(context, schedulerService);
    }
}
