package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exchanges.Exchange;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrdersWatcher implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(OrdersWatcher.class);
    private final OrderService orderService;
    private boolean stopped = false;

    @Autowired
    public OrdersWatcher(OrderService orderService) {
        this.orderService = orderService;
    }

    public void start() {
        if (stopped) {
            return;
        }
        orderService.init();
        Map<String, Order> limitOrders = orderService.getLimitOrders();
        if (limitOrders == null || limitOrders.isEmpty()) return;
        log.info("Watching orders: " + limitOrders.size());
        List<String> forRemove = new ArrayList<>();
        for (Order order : limitOrders.values()) {
            if (Duration.between(order.getDateCreate(), LocalDateTime.now()).toMinutes() < 3) {
                log.info("Order so new, skip watching " + order);
                continue;
            }
            log.info("How is order " + order);
            Order orderFromEx = orderService.getOrder(order);

            if (orderFromEx == null || orderFromEx.isCompleted()) {
                if (orderFromEx == null) {
                    log.error("Dont receive order from exchange for " + order);
                } else {
                    log.info("Remove order: " + orderFromEx);
                }
                orderService.orderComplete(order);
                forRemove.add(order.getId());
                continue;
            }

            try {
                Exchange exchange = orderService.getExchangeByName(orderFromEx.getServer());
                orderService.updateOrderPosition(order, exchange);
            } catch (Throwable e) {
                log.error("Update position error", e);
            }
        }
        for (String id : forRemove) {
            limitOrders.remove(id);
        }
    }

    @Override
    public void stop() {
        stopped = true;
    }

    @Override
    public String getThreadName() {
        return "ORDERS_WATCHER";
    }

}
