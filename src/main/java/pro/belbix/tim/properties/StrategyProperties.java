package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.history.Mutate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "tim.strategy")
@Getter
@Setter
@ToString
public class StrategyProperties implements MutableProperties {
    @NotBlank
    private String server;
    @NotBlank
    private String symbol;
    @Min(1)
    private int timeframeLong;
    @Min(1)
    private int timeframeShort;
    @Min(1)
    private int count = 100;
    private boolean validateCandleDate = true;
    private double initDeposit = 1000;
    private double fee = 0;
    private int leverage = 1;
    private boolean useDb = true;
    @NotBlank
    private String strategy;
    private boolean useMarketOrder = true;
    private boolean skipLoad = false;
    private boolean directClose = false;
    private boolean enableReverse = true;
    private boolean deleteDuplicate = false;

    @Mutate(value = 1, min = 0.1, max = 100)
    private double stopLoseLong = 7;
    @Mutate(value = 1, min = 0.1, max = 100)
    private double stopLoseShort = 7;
    @Mutate(value = 1, min = 0.1, max = 100)
    private double takeProfitLong = 100;
    @Mutate(value = 1, min = 0.1, max = 100)
    private double takeProfitShort = 100;

    public boolean isTimeframeTheSame() {
        return timeframeLong == timeframeShort;
    }
}
