package pro.belbix.tim.exchanges.bitmex;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exchanges.bitmex.dto.WalletResponse;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;
import pro.belbix.tim.properties.BitmexProperties;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.springframework.util.Assert.notEmpty;
import static pro.belbix.tim.entity.Order.OrderType.MARKET;
import static pro.belbix.tim.models.OrderSide.SHORT_CLOSE;
import static pro.belbix.tim.models.OrderSide.SHORT_OPEN;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class BitmexRESTTestApi {

    @Autowired
    private BitmexREST bitmexREST;

    @Autowired
    private BitmexProperties prop;

    //    @Test
    public void addOrderMarket() throws TIMRetryException {
        Order orderSell = new Order();
        orderSell.setId(UUID.randomUUID().toString());
        orderSell.setServer("bitmex");
        orderSell.setSymbol("XBTUSD");
        orderSell.setDateCreate(LocalDateTime.now());
        orderSell.setOrderSide(SHORT_OPEN);
        orderSell.setOrderType(MARKET.toString());
        orderSell.setAmount(1d);

        Order responseSell = bitmexREST.addOrder(orderSell);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Order orderBuy = new Order();
        orderBuy.setId(UUID.randomUUID().toString());
        orderBuy.setServer("bitmex");
        orderBuy.setSymbol("XBTUSD");
        orderBuy.setDateCreate(LocalDateTime.now());
        orderBuy.setOrderSide(SHORT_CLOSE);
        orderBuy.setOrderType(MARKET.toString());
        orderBuy.setAmount(1d);

        Order responseBuy = bitmexREST.addOrder(orderBuy);
    }

    @Test
    public void wallet() throws TIMRetryException {
        WalletResponse response = bitmexREST.wallet("XBT");
        System.out.println("response:" + response);
    }

    @Test
    public void getBalance() throws TIMRetryException {
        Balance response = bitmexREST.getBalance("XBT");
        System.out.println("Amount: " + response.balance());
        Assert.assertNotNull(response.balance());
        bitmexREST.normalizeBalance(response);
    }

    @Test
    public void lastPrice() throws TIMRetryException {
        Tick tick = bitmexREST.lastPrice("XBT");
        System.out.println(tick.getPrice());
    }

    @Test
    public void positions() throws TIMRetryException {
        List<Position> positions = bitmexREST.positions();
        for (Position response : positions) {
            System.out.println("getCurrentQty: " + response.currentQty());
        }
    }

    @Test
    public void historyTicks() throws TIMRetryException {
        List<Tick> ticks = bitmexREST.historyTicks(
                "XBTUSD",
                100,
                LocalDateTime.now(UTC).minus(10, ChronoUnit.MINUTES),
                null,
                0);
        notEmpty(ticks, "Last ticks must be");
        System.out.println(ticks.get(0));
//        for(Tick tick: ticks){
//            System.out.println(tick);
//        }
    }

    @Test
    public void leverage() throws TIMRetryException {
        bitmexREST.leverage("XBTUSD", prop.getLeverage());
    }

    @Test
    public void candles() throws TIMRetryException {
        List<Candle> candles = bitmexREST.candles("XBTUSD", "1m", LocalDateTime.parse("2019-01-01T00:00:00"), null, 3);
        for (Candle candle : candles) {
            System.out.println(candle);
        }
    }

    @Test
    public void getOrders() throws TIMRetryException {
        Order order = new Order();
        order.setSymbol("XBTUSD");
        List<Order> orders = bitmexREST.getOrders(order.getSymbol(), 1);
        Assert.assertNotNull(orders);
        for (Order o : orders) {
            System.out.println(o);
        }
    }

    @Test
    public void orderBook() throws TIMRetryException {
        List<Tick> ticks = bitmexREST.getOrderBook("XBTUSD", 1);
        Assert.assertNotNull(ticks);
        Assert.assertTrue(ticks.size() >= 2);
        for (Tick tick : ticks) {
            System.out.println(tick);
        }
    }
}
