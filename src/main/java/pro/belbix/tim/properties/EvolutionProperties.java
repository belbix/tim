package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tim.evolution")
@Getter
@Setter
@ToString
public class EvolutionProperties {
    private double factor = 1;
    private int top = 100;
    private boolean main = true;
    private boolean orders = false;
    private boolean strategy = false;
    private boolean death = false;
    private int alive = 10;
}
