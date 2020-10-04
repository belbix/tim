package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.download.TickDownloader;
import pro.belbix.tim.services.SchedulerService;

import java.util.TimeZone;

public class DownloaderApp {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        SchedulerService schedulerService = context.getBean(SchedulerService.class);
        schedulerService.addSchedulable(context.getBean(TickDownloader.class));
    }
}
