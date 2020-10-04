package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "tim.exchanges.bitmex")
public class BitmexProperties {
    private Set<Integer> noRetryableCodes = new HashSet<>(Arrays.asList(401, 200));
    @NotBlank
    private String url = "https://testnet.bitmex.com/api/v1/";
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
    private String urlOpenData = "https://www.bitmex.com/api/v1/";
    private String apiKeyOpenData = "";
    private String apiSecretOpenData = "";
    private String orderTypeDefault = "Market";
}
