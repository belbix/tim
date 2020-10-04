package pro.belbix.tim.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.history.Mutate;

@Validated
@ConfigurationProperties(prefix = "tim.strategies.srsi")
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SrsiProperties implements StrategyPropertiesI {
    //COMMON
    private boolean useMarketOrders = false;
    private boolean useLong = true;
    private boolean useShort = true;
    private boolean computeSrsi = true;
    private boolean printProcessing = true;

    private int oldRowNumber = 2;
    private int secondRowNumber = 1; //second
    private int firstRowNumber = 0;

    @Mutate(value = 0.1, min = -40, max = 40)
    private double rsiDeltaSecondOpen = 2;
    @Mutate(value = 0.1, min = -40, max = 40)
    private double priceDiffLongPerc = 2;
    @Mutate(value = 0.1, min = -40, max = 40)
    private double priceDiffShortPerc = 2;
    @Mutate(value = 0.1, min = -40, max = 40)
    private double rsiDeltaFirstOpen = 2;

    @Mutate(value = 0.1, min = -40, max = 40)
    private double kFirstMin = 100;
    @Mutate(value = 0.1, min = -40, max = 40)
    private double kmax = 7;
    //    @Mutate(value = 0.1, min = 0.0, max = 40.0, chance = 0.05)
    private double openPositionBigPriceChange = 9999999.0;


    @Mutate(value = 0.1, min = -40, max = 40)
    private Double rsiDeltaOldOpenLong = -1.4d;
    @Mutate(value = 0.1, min = -40, max = 40)
    private Double rsiDeltaFirstCloseLong = -2d;
    //    @Mutate(value = 0.1, min = -40, max = 40)
    private Double rsiDeltaFirstCloseLongAfterPump = -2d;

    @Mutate(value = 0.1, min = -40, max = 40)
    private Double rsiDeltaOldOpenShort = -1.4d;
    @Mutate(value = 0.1, min = -40, max = 40)
    private Double rsiDeltaFirstCloseShort = -2d;
    //    @Mutate(value = 0.1, min = -40, max = 40)
    private Double rsiDeltaFirstCloseShortAfterDump = -2d;
}
