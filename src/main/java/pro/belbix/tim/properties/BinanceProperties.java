package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "tim.exchanges.binance")
public class BinanceProperties {
    private Set<Integer> noRetryableCodes = new HashSet<>();
    @NotBlank
    private String url = "https://api.binance.com/api/v3";
    @NotBlank
    private String apiKey = "";
    @NotBlank
    private String apiSecret = "";
    @DecimalMin("1")
    @DecimalMax("100")
    private int leverage = 3;
    @DecimalMin("1")
    @DecimalMax("100")
    private int amountPercent = 100;
    private Boolean useAuthForOpenData = false;
    private String urlOpenData = "https://api.binance.com/api/v3";
    private String apiKeyOpenData = "";
    private String apiSecretOpenData = "";
    private String orderTypeDefault = "Market";
}
