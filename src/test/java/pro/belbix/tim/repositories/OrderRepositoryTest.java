package pro.belbix.tim.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.models.OrderSide;

import java.time.LocalDateTime;
import java.util.UUID;

import static pro.belbix.tim.entity.Order.OrderType.MARKET;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    public void saveOrder() {
        Order order = new Order();
        order.setId(UUID.randomUUID().toString());
        order.setServer("bitmextest");
        order.setSymbol("XBT");
        order.setDateCreate(LocalDateTime.now());
        order.setType(1);
        order.setOrderType(MARKET.toString());
        order.setAmount(1d);
        order.setOrderSide(OrderSide.LONG_OPEN);
        order.setAdditionalAmount(1);
        order.setOrdStatus("stat");
        order.setSideEx("asd");
        order.setDecisionPrice(123);
        order.setActualPrice(12);
        order = orderRepository.save(order);
        orderRepository.delete(order);
    }
}
