package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.history.Mutate;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "tim.order")
@Getter
@Setter
@ToString
public class OrderServiceProperties implements MutableProperties {
    @NotNull
    @NotEmpty
    private Set<String> exchanges;
    private boolean clearAllOrders = true;
    @Mutate(value = 0.05, min = 0.1, max = 10)
    private double priceChangeForUpdatePerc = 0;
    @Mutate(value = 1, min = 1, max = 10)
    private double maxUpdateCount = -1;
    @Mutate(value = 0.05, min = 0.1, max = 10)
    private double difForPricePerc = 0;
    private double chanceToMutate = 0.5;
}
