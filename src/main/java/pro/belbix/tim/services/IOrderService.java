package pro.belbix.tim.services;

import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exchanges.Exchange;

import java.util.Map;

public interface IOrderService {
    Order createMarketOrder(Candle candle);

    Order createLimitOrder(Candle candle);

    Order closeOrder(Order limitOrder);

    boolean checkOpenPositionsOrOrders(String symbol);

    boolean checkOpenOrders(String symbol);

    boolean checkOpenPositions();

    void closeAll(Candle candle);

    void init();

    Exchange getExchangeByName(String name);

    Map<String, Order> getLimitOrders();

    Double getLastOrderPrice();
}
