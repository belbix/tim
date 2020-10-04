package pro.belbix.tim.exchanges.bitmax;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static pro.belbix.tim.entity.Order.OrderType.LIMIT;
import static pro.belbix.tim.models.OrderSide.SHORT_CLOSE;
import static pro.belbix.tim.models.OrderSide.SHORT_OPEN;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class BitmaxRESTTestApi {
    @Autowired
    private BitmaxREST bitmaxREST;

    //    @Test
    public void addOrder() throws TIMRetryException {
        Order shortOpen = new Order();
        shortOpen.setId(UUID.randomUUID().toString());
        shortOpen.setServer("bitmax");
        shortOpen.setSymbol("BTCUSD");
        shortOpen.setDateCreate(LocalDateTime.now());
        shortOpen.setOrderSide(SHORT_OPEN);
        shortOpen.setOrderType(LIMIT.toString());
        shortOpen.setAmount(0.00125);

        try {
            bitmaxREST.addOrder(shortOpen);
        } catch (TIMRuntimeException e) {
            if (e.getMessage().startsWith("6010")) {
                System.out.println("" + e.getMessage());
                return;
            } else throw e;
        }

        Order shortClose = new Order();
        shortClose.setId(UUID.randomUUID().toString());
        shortClose.setServer("bitmax");
        shortClose.setSymbol("BTCUSD");
        shortClose.setDateCreate(LocalDateTime.now());
        shortClose.setOrderSide(SHORT_CLOSE);
        shortClose.setOrderType(LIMIT.toString());
        shortClose.setAmount(0.00125);


        bitmaxREST.addOrder(shortClose);
    }

    //    @Test
    public void updateOrder() throws InterruptedException, TIMRetryException {
        Order orderSell = new Order();
        orderSell.setId(UUID.randomUUID().toString());
        orderSell.setServer("bitmax");
        orderSell.setSymbol("BTCUSD");
        orderSell.setDateCreate(LocalDateTime.now());
        orderSell.setOrderSide(SHORT_OPEN);
        orderSell.setOrderType(LIMIT.toString());
        orderSell.setAmount(0.00125);

        try {
            Order responseSell = bitmaxREST.addOrder(orderSell);
            Thread.sleep(10000);
            bitmaxREST.updateOrder(responseSell);
        } catch (TIMRuntimeException e) {
            if (e.getMessage().startsWith("6010")) System.out.println("" + e.getMessage());
            else throw e;
        }

    }

    //    @Test
    public void getOrders() throws TIMRetryException {
        Order orderSell = new Order();
        orderSell.setId(UUID.randomUUID().toString().replace("-", ""));
        orderSell.setServer("bitmax");
        orderSell.setSymbol("BTCUSD");
        orderSell.setDateCreate(LocalDateTime.now());
        orderSell.setOrderSide(SHORT_OPEN);
        orderSell.setOrderType(LIMIT.toString());
        orderSell.setAmount(1d);
        bitmaxREST.getOrders(orderSell.getSymbol(), 100);
    }

    //    @Test
    public void closeOrder() throws TIMRetryException {
        Order orderSell = new Order();
        orderSell.setId(UUID.randomUUID().toString().replace("-", ""));
        orderSell.setServer("bitmax");
        orderSell.setSymbol("BTCUSD");
        orderSell.setDateCreate(LocalDateTime.now());
        orderSell.setOrderSide(SHORT_OPEN);
        orderSell.setOrderType(LIMIT.toString());
        orderSell.setAmount(1d);
        bitmaxREST.closeOrder(orderSell);
    }

    //    @Test
    public void closeAllOrders() throws TIMRetryException {
        bitmaxREST.closeAllOrders("BTC/USDT");
    }

    @Test
    public void getOrderBook() throws TIMRetryException {
        List<Tick> ticks = bitmaxREST.getOrderBook("BTC", 0);
//        Assert.assertNotNull(ticks);
//        Assert.assertTrue(ticks.size() >= 2);
//        for(Tick tick: ticks){
//            System.out.println(tick.toString());
//        }
    }

    //    @Test
    public void getBalance() throws TIMRetryException {
        Balance balance = bitmaxREST.getBalance("BTC");
        System.out.println(balance.toString());
    }

    @Test
    public void lastPrice() throws TIMRetryException {
        Tick tick = bitmaxREST.lastPrice("BTC");
        System.out.println(tick);
    }

    //    @Test
    public void positions() throws TIMRetryException {
        List<Position> positions = bitmaxREST.positions();
        for (Position position : positions) {
            System.out.println(position.symbol() + ": " + position.currentQty());
        }
    }

    @Test
    public void historyOrders() throws TIMRetryException {
        bitmaxREST.historyOrders();
    }
}
