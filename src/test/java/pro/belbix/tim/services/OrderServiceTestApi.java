package pro.belbix.tim.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.properties.OrderServiceProperties;

import static pro.belbix.tim.models.OrderSide.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class OrderServiceTestApi {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceTestApi.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private DBCandleService candleService;

    @Autowired
    private OrdersWatcher ordersWatcher;

    @Autowired
    private OrderServiceProperties orderServiceProperties;

    @Autowired
    private SchedulerService schedulerService;

    @Before
    public void setUp() throws Exception {
        orderServiceProperties.setDifForPricePerc(0.1);
        orderServiceProperties.setPriceChangeForUpdatePerc(0.1);
        orderServiceProperties.setMaxUpdateCount(3);
    }

    @Test
    public void createLimitOrderLong() {
        schedulerService.addSchedulable(ordersWatcher);
        orderService.init();

        Candle candleLongOpen = new Candle();
        candleLongOpen.setSymbol("XBTUSD");
        candleLongOpen.setOrderSide(LONG_OPEN);
        Order responseSell = orderService.createLimitOrder(candleLongOpen);
        log.info("TEST ORDER BUY SENT: " + responseSell);
        waitOpen();

        Candle candleLongClose = new Candle();
        candleLongClose.setSymbol("XBTUSD");
        candleLongClose.setOrderSide(LONG_CLOSE);
        Order responseBuy = orderService.createLimitOrder(candleLongClose);
        log.info("TEST ORDER SELL SENT: " + responseBuy);
        waitClose();
    }

    @Test
    public void createLimitOrderShort() {
        schedulerService.addSchedulable(ordersWatcher);
        orderService.init();

        Candle candleLongOpen = new Candle();
        candleLongOpen.setSymbol("XBTUSD");
        candleLongOpen.setOrderSide(SHORT_OPEN);
        Order responseSell = orderService.createLimitOrder(candleLongOpen);
        log.info("TEST ORDER SELL SENT: " + responseSell);

        waitOpen();

        Candle candleLongClose = new Candle();
        candleLongClose.setSymbol("XBTUSD");
        candleLongClose.setOrderSide(SHORT_CLOSE);
        Order responseBuy = orderService.createLimitOrder(candleLongClose);
        log.info("TEST ORDER BUY SENT: " + responseBuy);

        waitClose();
    }

    private void waitOpen() {
        for (int i = 0; i < 100_000; i++) {
            log.info("waitOpen: " + orderService.getLimitOrders().size());
            try {
                Thread.sleep(1000);
                if (orderService.getLimitOrders().size() == 0) {
                    log.info("Check open positions");
                    if (orderService.checkOpenPositions()) {
                        Thread.sleep(1000);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Position not open");
    }

    private void waitClose() {
        for (int i = 0; i < 100_000; i++) {
            log.info("waitClose: " + orderService.getLimitOrders().size());
            try {
                Thread.sleep(1000);
                if (!orderService.checkOpenPositions()) {
                    Thread.sleep(1000);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new IllegalStateException("Position already open");
    }
}
