package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "tim.history")
@ToString
public class HistoryProperties {
    private String strategyName;
    private String dateStart;
    private String dateEnd = LocalDateTime.now().toString();
    /**
     * seconds
     */
    private int ticktime;
    private int emptyTicktime = 60;
    private int deposit = 1000;
    private double fee = 0;
    private int leverage = 1;
    private int batch = 100;
    private int delay = 0;
    private int effLoseMod = 3;
    /**
     * pm - percent with mode; p - percent without mode; d - deposit
     */
    private String effType = "pm";
    private int srsi2Deep = 3;
    private String station = "default";
    private int minDeposit = 400;
}
