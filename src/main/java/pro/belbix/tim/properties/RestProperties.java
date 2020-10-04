package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tim.rest")
@Getter
@Setter
public class RestProperties {
    private boolean loggingRequest = false;
}
