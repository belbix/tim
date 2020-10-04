package pro.belbix.tim.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Validated
@ConfigurationProperties(prefix = "tim.downloader")
@Getter
@Setter
public class TickDownloaderProperties {
    private String server = "bitmex";
    private String symbol = "XBTUSD";
    private int batch = 500;
    private String dateStart;
    private boolean buildCandle = true;
    private Set<Integer> timeframes;
    private boolean useCache = false;
    private boolean initExist = false;
    private int failStepSec = 3600;
    private int existCount = 10_000;
    private boolean useWs = false;
    private int tickBuffer = 0;
    private boolean onlyClose = false;
}
