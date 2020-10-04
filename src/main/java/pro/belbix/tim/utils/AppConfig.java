package pro.belbix.tim.utils;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import pro.belbix.tim.properties.*;

@EnableRetry
@Configuration
@EnableConfigurationProperties({
        StrategyProperties.class,
        BitmexProperties.class,
        OrderServiceProperties.class,
        SrsiProperties.class,
        TickDownloaderProperties.class,
        RestProperties.class,
        EmailProperties.class,
        Srsi2Properties.class,
        VisualProperties.class,
        BitmaxProperties.class,
        BinanceProperties.class,
        HistoryProperties.class,
        EvolutionProperties.class,
        NsrsiProperties.class
})
public class AppConfig {
}
