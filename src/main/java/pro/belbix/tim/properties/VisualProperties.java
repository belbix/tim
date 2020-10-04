package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "tim.visual")
@Getter
@Setter
@ToString
public class VisualProperties implements StrategyPropertiesI {
    private boolean useLocalGraphic = true;
    private int updaterCount = 1;
    private int height = 850;
    private int width = 1800;
    private int moveSpeed = 100;

    private float widthNeuron = 8;
    private float heightLayer = 7;
}
