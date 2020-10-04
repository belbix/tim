package pro.belbix.tim.strategies;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.properties.OrderServiceProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.services.ICandleService;
import pro.belbix.tim.services.OrderService;

import java.util.Set;

import static pro.belbix.tim.models.OrderSide.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class SrsiStrategyTest {

    @Autowired
    private SrsiStrategy srsiStrategy;

    @Autowired
    private ICandleService candleService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private StrategyProperties strategyProperties;

    @Autowired
    private OrderServiceProperties orderServiceProperties;

    @Before
    public void setUp() {
        orderService.closeAllOrders("XBTUSD");
    }

    @Test
    public void mainLoop() {
        srsiStrategy.setWithoutDecision(true);
        srsiStrategy.setCheckedPositions(true);
        srsiStrategy.setCheckOrders(false);
        srsiStrategy.start();
    }

    @Test
    public void createOrder() {
        strategyProperties.setServer("bitmex");
        orderServiceProperties.setExchanges(Set.of("bitmex"));
        orderService.init();

        Candle candleLongOpen = candleService.lastCandle("bitmex", "XBTUSD", 1);
        if (candleLongOpen == null) {
            System.out.println("candleLongOpen is null");
            return;
        }
        candleLongOpen.setOrderSide(LONG_OPEN);
        Order orderLongOpen = srsiStrategy.createOrder(candleLongOpen);
        Assert.assertNotNull("long open is null", orderLongOpen);
        Assert.assertFalse("blank id", orderLongOpen.getId().isBlank());
        sleep();

        Candle candleLongCloseError1 = candleService.lastCandle("bitmex", "XBTUSD", 1);
        candleLongCloseError1.setOrderSide(LONG_OPEN);
        Order orderLongCloseError1 = null;
        try {
            orderLongCloseError1 = srsiStrategy.createOrder(candleLongCloseError1);
        } catch (TIMRuntimeException e) {
            System.out.println(e.getMessage());
        }
        Assert.assertNull(orderLongCloseError1);
        System.out.println("orderLongCloseError1: " + orderLongCloseError1);

        sleep();

        Candle candleLongClose = candleService.lastCandle("bitmex", "XBTUSD", 1);
        candleLongClose.setOrderSide(LONG_CLOSE);
        Order orderLongClose = srsiStrategy.createOrder(candleLongClose);
        System.out.println(orderLongClose);
        Assert.assertNotNull(orderLongClose);
        Assert.assertFalse(orderLongClose.getId().isBlank());
        System.out.println("orderLongClose: " + orderLongClose);

        sleep();

        Candle candleShortOpen = candleService.lastCandle("bitmex", "XBTUSD", 1);
        candleShortOpen.setOrderSide(SHORT_OPEN);
        Order orderShortOpen = srsiStrategy.createOrder(candleShortOpen);
        System.out.println(orderShortOpen);
        Assert.assertNotNull(orderShortOpen);
        Assert.assertFalse(orderShortOpen.getId().isBlank());
        System.out.println("orderShortOpen: " + orderShortOpen);

        sleep();

        Candle candleShortCloseError1 = candleService.lastCandle("bitmex", "XBTUSD", 1);
        candleShortCloseError1.setOrderSide(LONG_CLOSE);
        Order orderShortCloseError1 = null;
        try {
            orderShortCloseError1 = srsiStrategy.createOrder(candleShortCloseError1);
        } catch (TIMRuntimeException e) {
            System.out.println(e.getMessage());
        }
        Assert.assertNull(orderShortCloseError1);
        System.out.println("orderShortCloseError1: " + orderShortCloseError1);

        sleep();

        Candle candleShortClose = candleService.lastCandle("bitmex", "XBTUSD", 1);
        candleShortClose.setOrderSide(SHORT_CLOSE);
        Order orderShortClose = srsiStrategy.createOrder(candleShortClose);
        System.out.println(orderShortClose);
        Assert.assertNotNull(orderShortClose);
        Assert.assertFalse(orderShortClose.getId().isBlank());
        System.out.println("orderShortClose: " + orderShortClose);

        Assert.assertFalse("have a not complete pos", orderService.checkOpenPositionsOrOrders("XBTUSD"));
    }

    private void sleep() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
