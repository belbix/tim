package pro.belbix.tim;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class SimpleApp {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
}
