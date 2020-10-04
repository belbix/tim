package pro.belbix.tim.strategies.common;

import org.junit.Test;
import pro.belbix.tim.models.OrderSide;

import static org.springframework.test.util.AssertionErrors.assertEquals;

public class TradeStatusBuilderTest {

    @Test
    public void calcProfitTest() {
        TradeStatusBuilder tradeStatusBuilder = new TradeStatusBuilder(5046, 0.035, 3);
        double profit = tradeStatusBuilder.calcProfit(5466, 5072, OrderSide.LONG_CLOSE, true);
        assertEquals("Profit", -1101.3914170142712, profit);
    }

    @Test
    public void calcProfitTest2() {
        TradeStatusBuilder tradeStatusBuilder = new TradeStatusBuilder(8576.78, 0.035, 3);
        double profit = tradeStatusBuilder.calcProfit(4208.5, 4000, OrderSide.LONG_CLOSE, false);
        assertEquals("Profit", -430.77094083889807, profit);
    }
}
