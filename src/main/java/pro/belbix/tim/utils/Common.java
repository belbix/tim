package pro.belbix.tim.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.rng.sampling.distribution.ZigguratNormalizedGaussianSampler;
import org.apache.commons.rng.simple.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.services.SchedulerService;
import pro.belbix.tim.strategies.Strategy;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Common {
    public static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(Common.class);
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static int minutesFromPeriod(String period) {
        if (period == null || period.isBlank()) return 0;
        period = period.toLowerCase();
        if (period.contains("m")) {
            return Integer.parseInt(period.replace("m", ""));
        }
        if (period.contains("h")) {
            return Integer.parseInt(period.replace("h", "")) * 60;
        }
        if (period.contains("d")) {
            return Integer.parseInt(period.replace("d", "")) * 60 * 24;
        }
        throw new TIMRuntimeException("Wrong period: " + period);
    }

    public static void addStrategyInSchedule(ConfigurableApplicationContext context, SchedulerService schedulerService) {
        String strategy = context.getBean(StrategyProperties.class).getStrategy();
        Map<String, Strategy> strategies = context.getBeansOfType(Strategy.class);
        for (Strategy s : strategies.values()) {
            if (s.getStrategyName().equals(strategy)) {
                log.info("Add " + s.getStrategyName() + " in scheduler");
                schedulerService.addSchedulable(s);
                break;
            }
        }
    }

    public static void fullValidateTicks(List<Tick> ticks) {
        Set<String> ids = new HashSet<>();
        validateTicks(ticks, 60);
        for (Tick tick : ticks) {
            if (ids.contains(tick.getStrId())) {
                throw new RuntimeException("double: " + tick);
            }
            ids.add(tick.getStrId());
//            log.info(tick);
        }
    }

    public static void validateTicks(List<Tick> ticks, int betweenSec) {
        LocalDateTime lastDate = null;
        for (Tick tick : ticks) {
            if (lastDate == null) {
                lastDate = tick.getDate();
                continue;
            }

            if (Duration.between(lastDate, tick.getDate()).toSeconds() > betweenSec) {
                log.info("Too large date between " + lastDate + " and " + tick.getDate()
                        + " dur: " + Duration.between(lastDate, tick.getDate())
                        + "    tick: " + tick);
            }
            lastDate = tick.getDate();
        }
    }

    public static LocalDateTime minDateFromTicks(List<Tick> ticks, int betweenSec) {
        LocalDateTime lastDate = null;
        for (Tick tick : ticks) {
            if (lastDate == null) {
                lastDate = tick.getDate();
                continue;
            }

            if (Duration.between(lastDate, tick.getDate()).toSeconds() > betweenSec) {
                break;
            }
            lastDate = tick.getDate();
        }
        return lastDate;
    }

    public static double arbitration(double balanceAmount,
                                     double firstPairHighPrice,
                                     double secondPairHighPrice,
                                     double thirdPairLowPrice) {
        double firstSymbolAmount = balanceAmount / firstPairHighPrice; //PAX from USDT
        double secondSymbolAmount = firstSymbolAmount / secondPairHighPrice; //BTC
        return secondSymbolAmount * thirdPairLowPrice; //USDT
    }

    public static double arbitrationReverse(double balanceAmount,
                                            double firstPairLowPrice,
                                            double secondPairLowPrice,
                                            double thirdPairHighPrice) {
        double thirdSymbolAmount = balanceAmount / thirdPairHighPrice; //BTC from USDT
        double secondSymbolAmount = thirdSymbolAmount * secondPairLowPrice; //PAX
        return secondSymbolAmount * firstPairLowPrice; //USDT
    }

    public static double roundDouble(double a) {
        int s = 100000;
        return (double) Math.round(a * s) / s;
    }

    public static String doubleToString(double d) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.000000");
        decimalFormat.setGroupingSize(0);
        decimalFormat.setGroupingUsed(false);
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(symbols);
        decimalFormat.setRoundingMode(RoundingMode.DOWN);
        return decimalFormat.format(d);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static double normalDistributionRandom() {
//        return new Random().nextGaussian();
        return ZigguratNormalizedGaussianSampler.of(RandomSource.create(RandomSource.MT_64)).sample();
    }

}
