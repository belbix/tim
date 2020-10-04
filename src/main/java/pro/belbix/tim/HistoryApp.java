package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.history.HistoryProcessor;

import java.util.TimeZone;

public class HistoryApp {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        context.getBean(HistoryProcessor.class).start();
    }
}
