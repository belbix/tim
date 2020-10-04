package pro.belbix.tim.strategies;

import org.junit.Test;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.models.OrderSide;

public class StrategyTest {

    public static Candle createOpenLongDecision() {
        Candle candle = new Candle();
        candle.setOrderSide(OrderSide.LONG_OPEN);
        return candle;
    }

    public static Candle createCloseLongDecision() {
        Candle candle = new Candle();
        candle.setOrderSide(OrderSide.LONG_CLOSE);
        return candle;
    }

    public static Candle createOpenShortDecision() {
        Candle candle = new Candle();
        candle.setOrderSide(OrderSide.SHORT_OPEN);
        return candle;
    }

    public static Candle createCloseShortDecision() {
        Candle candle = new Candle();
        candle.setOrderSide(OrderSide.SHORT_CLOSE);
        return candle;
    }

    @Test
    public void testDecision() {
//        Map<Candle, Tuple<Candle, Candle>> decisions = new HashMap<>();
//        decisions.put(createOpenShortDecision(), new Tuple<>(createOpenLongDecision(), null));
//        Candle candle = Strategy.calcDecisions(decisions, createOpenLongDecision(), null);
//        Assert.notNull(candle);
    }


}
