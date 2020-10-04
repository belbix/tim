package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.exchanges.ExchangesUtils;
import pro.belbix.tim.exchanges.models.Position;
import pro.belbix.tim.properties.OrderServiceProperties;
import pro.belbix.tim.repositories.OrderRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static pro.belbix.tim.entity.Order.OrderType.MARKET;
import static pro.belbix.tim.models.OrderSide.LONG_CLOSE;
import static pro.belbix.tim.models.OrderSide.SHORT_CLOSE;

@Component
public class OrderService implements IOrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final Map<String, Order> limitOrders = new ConcurrentHashMap<>();
    private final AppContextService appContextService;
    private final OrderServiceProperties prop;
    private final OrderRepository orderRepository;
    private final Map<String, Integer> countOfUpdate = new HashMap<>();
    private Set<Exchange> exchanges = new HashSet<>(); //TODO implement multiple exchanges
    private boolean inited = false;
    private Double lastOrderPrice;

    @Autowired
    public OrderService(AppContextService appContextService, OrderServiceProperties prop, OrderRepository orderRepository) {
        this.appContextService = appContextService;
        this.prop = prop;
        this.orderRepository = orderRepository;
    }


    public void init() {
        if (inited) return;
        findExchanges();
        inited = true;
    }

    private void findExchanges() {
        this.exchanges = appContextService.findExchanges(prop.getExchanges());
        if (this.exchanges == null || this.exchanges.isEmpty())
            throw new TIMRuntimeException("Exchange not found");
    }

    public Order createMarketOrder(Candle candle) {
        lastOrderPrice = null;
        log.info("Create order from " + candle);
        return sendOrder(buildOrder(candle, MARKET.toString()));

    }

    public Order createLimitOrder(Candle candle) {
        lastOrderPrice = null;
        log.info("Create order from " + candle);
        Order order = buildOrder(candle, Order.OrderType.LIMIT.toString());

        List<Order> existOrder = getExist(order);

        if (existOrder != null && !existOrder.isEmpty()) {
            log.info("Handle exist orders: " + existOrder.size());
            try {
                handleExistOrder(existOrder, order);
            } catch (TIMNothingToDo e) {
                log.info("Nothing to do, all closed");
                return null;
            }
        } else {
            log.info("Exist orders not found");
        }

        order = sendOrder(order);
        if (order != null && order.getId() != null) {
            order.setDecisionPrice(order.getPrice());
            limitOrders.put(order.getId(), order);
        }

        return order;
    }


    private List<Order> getExist(Order order) {
        List<Order> orders = new ArrayList<>();
        for (Order limitOrder : limitOrders.values()) {

            for (Exchange exchange : exchanges) {
                String open = exchange.normalizeSymbolPair(limitOrder.getSymbol(), "-");
                String exist = exchange.normalizeSymbolPair(order.getSymbol(), "-");
                log.info("Open order currency: " + open);
                log.info("Exist order currency: " + exist);
                if (open.equals(exist)) {
                    orders.add(limitOrder);
                }
            }
        }
        return orders;
    }

    private void closeOrders(List<Order> existOrders) {
        for (Order existOrder : existOrders) { // clear all another orders, before create new
            Order closedOrder = closeOrder(existOrder);
            if (closedOrder != null) {
                log.info("Order was canceled: " + closedOrder);
            } else {
                log.error("Order not canceled, may be it is completed?  " + existOrder);
            }
        }
    }

    private void handleExistOrder(List<Order> existOrders, Order order) throws TIMNothingToDo {
        if (prop.isClearAllOrders()) {
            closeAllOrders(order.getSymbol());
        } else {
            closeOrders(existOrders);
        }

        List<Position> positions = getPositions();
        if (positions == null || positions.isEmpty()) {
            log.info("We dont have any positions");
            return;
        }

        Position position = null;
        for (Position p : positions) {
            if (p.compareWithOrder(order)) {
                position = p;
                break;
            }
            log.warn("We have another positions:" + p);
        }

        if (position == null || position.currentQty() == 0) {
            if (order.getOrderSide().isClose()) throw new TIMNothingToDo();
            return;
        }

        log.info("Open position: " + position);

        if (position.currentQty() > 0) {
            if (order.getOrderSide().longOrShort() == 0) { //if long
                if (!order.getOrderSide().isClose()) { //if open position
                    order.setAdditionalAmount(-position.currentQty()); //need to less amount
                    log.info("Set additional amount:" + order.getAdditionalAmount());
                } else {
                    log.warn("This is LONG_CLOSE (" + order.getOrderSide().name()
                            + ") we get amount from open positions already");
                }
            } else { //if short
                if (!order.getOrderSide().isClose()) { //if open position
                    order.setAdditionalAmount(position.currentQty()); //need to more amount
                    log.info("We need to add exist amount to open short position " + order.getAdditionalAmount());
                } else {
                    log.error("We have long position, CLOSE SHORT is wrong operation");
                    throw new TIMNothingToDo();
                }
            }
        } else {
            if (order.getOrderSide().longOrShort() == 1) { //if short
                if (!order.getOrderSide().isClose()) { //if open position
                    order.setAdditionalAmount(position.currentQty()); //need to more amount
                    log.info("Set additional amount:" + order.getAdditionalAmount());
                } else {
                    log.warn("This is SHORT_CLOSE (" + order.getOrderSide().name()
                            + ") we get amount from open positions already");
                }
            } else { // if long
                if (!order.getOrderSide().isClose()) { //if open position
                    order.setAdditionalAmount(position.currentQty()); //need to more amount
                    log.info("We need to add exist amount to open long position " + order.getAdditionalAmount());
                } else {
                    log.error("We have short position, CLOSE LONG is wrong operation");
                    throw new TIMNothingToDo();
                }
            }
        }
    }

    private Order sendOrder(Order order) {
        for (Exchange exchange : exchanges) {
            order.setServer(exchange.getExchangeName());
            try {
                //you should know price
                if (!order.getOrderType().equals(MARKET.toString())) {
                    log.info("Check last price");
                    ExchangesUtils.checkLastPrice(order, exchange);
                    ExchangesUtils.addDiffToPrice(order, prop.getDifForPricePerc());
                }
                order = exchange.addOrder(order);
                if (order != null && order.getId() != null) {
                    orderRepository.save(order);
                }
                return order;
            } catch (TIMRetryException e) {
                log.error("Error send order", e);
            }
        }

        return null;
    }


    public Order closeOrder(Order order) {
        log.info("Close orders for " + order.getId());
        for (Exchange exchange : exchanges) {
            try {
                List<Order> orders = exchange.closeOrders(Collections.singletonList(order));
                for (Order o : orders) {
                    limitOrders.remove(o.getId());
                    if (o.getId().equals(order.getId())) return o;
                }
            } catch (TIMRetryException e) {
                log.error("Error send order", e);
            }
        }
        return null;
    }

    public void closeAllOrders(String symbol) {
        for (Exchange exchange : exchanges) {
            try {
                exchange.closeAllOrders(symbol);
//                for (Order order : orders) {
//                    limitOrders.remove(order.getId());
//                }
                List<String> keyForDelete = new ArrayList<>();
                for (Order order : limitOrders.values()) {
                    if (order.getSymbol().equals(symbol)) {
                        keyForDelete.add(order.getId());
                    }
                }
                for (String key : keyForDelete) {
                    limitOrders.remove(key);
                }
            } catch (TIMRetryException e) {
                log.error("Error send order", e);
            }
        }
    }

    public void closeAllPositions(Candle candle) {
        log.warn("Try close all positions");
        List<Position> positions = getPositions();
        if (positions == null) return;
        log.info("Open positions: " + positions.size());
        for (Position position : positions) {
            if (position.currentQty() > 0) {
                candle.setOrderSide(LONG_CLOSE);
                createMarketOrder(candle);
            } else {
                candle.setOrderSide(SHORT_CLOSE);
                createMarketOrder(candle);
            }
        }
    }

    public void closeAll(Candle candle) {
        closeAllOrders(candle.getSymbol());
        closeAllPositions(candle);
    }


    private Order buildOrder(Candle candle, String orderType) {
        if (candle.getOrderSide() == null) {
            throw new TIMRuntimeException("candle.getOrderSide() is null");
        }
        Order order = new Order();
//        order.setServer(candle.getServer()); you dont know what exchange will be
        order.setSymbol(candle.getSymbol());
        order.setDateCreate(LocalDateTime.now());
        order.setOrderSide(candle.getOrderSide());
        order.setPrice(candle.getClose());
        order.setAmount(candle.getAmount());
        order.setOrderType(orderType);
        return order;
    }

    public void updateOrderPosition(Order order, Exchange exchange) throws TIMRetryException {
        ExchangesUtils.checkLastPrice(order, exchange);
        double actualPrice = order.getActualPrice();

        double delta;
        if (order.isBuy()) {
            delta = actualPrice - order.getDecisionPrice();
        } else {
            delta = order.getDecisionPrice() - actualPrice;
        }

        double priceChange = (delta / actualPrice) * 100;
        if (priceChange > prop.getPriceChangeForUpdatePerc()) {
            log.info("Update price for: " + order.getPrice()
                    + " ActualPrice:" + actualPrice
                    + " decisionPrice:" + order.getDecisionPrice() +
                    " | priceChange:" + priceChange
                    + " delta: " + delta);

            String oldId = order.getId();
            if (countOfUpdate.containsKey(oldId)) {
                int count = countOfUpdate.get(oldId);
                if (count > (int) Math.round(prop.getMaxUpdateCount())) {
                    log.warn("Count of update " + count + ". Immediately execute order!");
                    exchange.closeOrders(List.of(order));
                    limitOrders.remove(oldId);
                    order.setOrderType(MARKET.toString());
                    exchange.addOrder(order);
                    return;
                }
                countOfUpdate.put(oldId, count + 1);
            } else {
                countOfUpdate.put(oldId, 1);
            }

            ExchangesUtils.addDiffToPrice(order, prop.getDifForPricePerc());
            order = exchange.updateOrder(order);
            if (order != null
//                    && !oldId.equals(order.getId()) //TODO not sure
            ) {
                log.info("remove old order and put new with id: " + order.getId());
                limitOrders.remove(oldId);
                order.setDecisionPrice(actualPrice);
                limitOrders.put(order.getId(), order);
            }
        }
    }

    public Order getOrder(Order order) {
        for (Exchange exchange : exchanges) {
            try {
                List<Order> orders = exchange.getOrders(order.getSymbol(), 30);
                for (Order o : orders) {
                    if (o.getId().equals(order.getId())) return o;
                }

            } catch (TIMRetryException e) {
                log.error("Error send order", e);
            }
        }
        return null;
    }

    private List<Order> getOrders(String symbol) {
        List<Order> result = new ArrayList<>();
        for (Exchange exchange : exchanges) {
            try {
                List<Order> orders = exchange.getOrders(symbol, 500);
                result.addAll(orders);
            } catch (TIMRetryException e) {
                log.error("Error send order", e);
            }
        }
        return result;
    }

    private List<Position> getPositions() {
        for (Exchange exchange : exchanges) {
            try {
                return exchange.positions();
            } catch (TIMRetryException e) {
                log.error("Error send order", e);
            }
        }
        return null;
    }

    public Exchange getExchangeByName(String name) {
        for (Exchange exchange : exchanges) {
            if (exchange.getExchangeName().equals(name)) return exchange;
        }
        return null;
    }

    @Override
    public boolean checkOpenPositionsOrOrders(String symbol) {
        if (checkOpenPositions()) {
            return true;
        }

        return checkOpenOrders(symbol);
    }

    @Override
    public boolean checkOpenOrders(String symbol) {
        List<Order> orders = getOrders(symbol);
        if (orders.isEmpty()) return false;
        log.info("Check orders: " + orders.size());
        for (Order order : orders) {
            if (!order.isCompleted()) {
                log.info("We have incomplete order: " + order);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkOpenPositions() {
        List<Position> positions = getPositions();
        if (positions != null && !positions.isEmpty()) {
            for (Position position : positions) {
                log.info("Position: " + position);
                if (position.currentQty() != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Double getLastOrderPrice() {
        return lastOrderPrice;
    }

    public void orderComplete(Order order) {
        lastOrderPrice = order.getPrice();
    }

    public Map<String, Order> getLimitOrders() {
        return limitOrders;
    }
}
