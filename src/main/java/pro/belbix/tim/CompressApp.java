package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.services.SrsiTickService;

import java.util.TimeZone;

public class CompressApp {

    public static void main(String[] args) {
        int version = Integer.parseInt(args[0]);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        context.getBean(SrsiTickService.class).create(version);
        System.exit(1);
    }
}
