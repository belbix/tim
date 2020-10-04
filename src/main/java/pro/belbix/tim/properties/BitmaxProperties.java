package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "tim.exchanges.bitmax")
public class BitmaxProperties {
    @NotBlank
    private String url = "https://bitmax-test.io/api/v1";
    @NotBlank
    private String apiKey = "";
    @NotBlank
    private String apiSecret = "";
    private int leverage = 1;
    private int amountPercent = 100;
    private String marginPrefix = "margin/"; // margin/
    private boolean amountBySide = true;
}
