package pro.belbix.tim.exchanges.binance;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exchanges.models.Position;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class BinanceRESTTest {

    @Autowired
    private BinanceREST binance;

    @Test
    public void getExchangeName() {
        Assert.assertEquals("binance", binance.getExchangeName());
    }

    @Test
    public void addOrder() {
    }

    @Test
    public void updateOrder() {
    }

    @Test
    public void getOrders() {
    }

    @Test
    public void closeOrders() {
    }

    @Test
    public void closeAllOrders() {
    }

    @Test
    public void getBalance() {
    }

    @Test
    public void getBalances() {
    }

    @Test
    public void lastPrice() {
    }

    //    @Test
    public void positions() throws TIMRetryException {
        List<Position> positions = binance.positions();
        assertNotNull("Position is not null", positions);
        positions.forEach(System.out::println);
        assertTrue(positions.isEmpty());
    }

    @Test
    public void historyTicks() {
    }

    @Test
    public void getOrderBook() {
    }

    @Test
    public void loadAmountAndPrice() {
    }

    @Test
    public void normalizeSymbolPair() {
    }
}
