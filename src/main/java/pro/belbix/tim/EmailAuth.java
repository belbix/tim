package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.services.EmailService;

import java.util.TimeZone;

public class EmailAuth {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        EmailService emailService = context.getBean(EmailService.class);
        emailService.sendEmail("7453635@gmail.com", "sbj auth", "text auth");
    }

}
