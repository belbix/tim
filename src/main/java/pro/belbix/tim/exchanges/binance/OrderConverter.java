package pro.belbix.tim.exchanges.binance;

import com.binance.api.client.domain.OrderType;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMRuntimeException;

import static pro.belbix.tim.utils.Common.doubleToString;

public class OrderConverter {
    private OrderConverter() {
    }

    public static NewOrder convertToNewBinance(Order order) {
        if (!order.getServer().equals("binance"))
            throw new TIMRuntimeException("Invalid order server: " + order.getServer());
        OrderType orderType = parseOrderType(order);

        if (order.getOrderSide().isOpen()) {
            if (order.getOrderSide().isLong()) {
                if (orderType.equals(OrderType.LIMIT)) {
                    return newLimitBuy(order);
                }

                if (orderType.equals(OrderType.MARKET)) {
                    return newMarketBuy(order);
                }
            }

            if (order.getOrderSide().isShort()) {
                if (orderType.equals(OrderType.LIMIT)) {
                    return newLimitSell(order);
                }

                if (orderType.equals(OrderType.MARKET)) {
                    return newMarketSell(order);
                }
            }
        }

        if (order.getOrderSide().isClose()) {
            if (order.getOrderSide().isLong()) {
                if (orderType.equals(OrderType.LIMIT)) {
                    return newLimitSell(order);
                }

                if (orderType.equals(OrderType.MARKET)) {
                    return newMarketSell(order);
                }
            }

            if (order.getOrderSide().isShort()) {
                if (orderType.equals(OrderType.LIMIT)) {
                    return newLimitBuy(order);
                }

                if (orderType.equals(OrderType.MARKET)) {
                    return newMarketBuy(order);
                }
            }
        }

        throw new TIMRuntimeException("Wrong state for order " + order);
    }

    private static NewOrder newLimitBuy(Order order) {
        return NewOrder.limitBuy(
                order.getSymbol(),
                TimeInForce.GTC,
                doubleToString(order.getAmount()),
                doubleToString(order.getPrice())
        );
    }

    private static NewOrder newMarketBuy(Order order) {
        return NewOrder.marketBuy(
                doubleToString(order.getAmount()),
                doubleToString(order.getPrice())
        );
    }

    private static NewOrder newLimitSell(Order order) {
        return NewOrder.limitSell(
                order.getSymbol(),
                TimeInForce.GTC,
                doubleToString(order.getAmount()),
                doubleToString(order.getPrice())
        );
    }

    private static NewOrder newMarketSell(Order order) {
        return NewOrder.marketSell(
                doubleToString(order.getAmount()),
                doubleToString(order.getPrice())
        );
    }

    private static OrderType parseOrderType(Order order) {
        if (order.getOrderType() == null) {
            throw new TIMRuntimeException("Order type is null");
        }
        switch (order.getOrderType()) {
            case "Limit":
                return OrderType.LIMIT;
            case "Market":
                return OrderType.MARKET;
        }
        throw new TIMRuntimeException("Order type not found: " + order.getOrderType());
    }
}
