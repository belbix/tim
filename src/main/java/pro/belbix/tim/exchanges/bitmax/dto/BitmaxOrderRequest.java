package pro.belbix.tim.exchanges.bitmax.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.rest.Request;
import pro.belbix.tim.utils.Common;

import java.util.UUID;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BitmaxOrderRequest implements Request {
    private String coid; //         string       "xxx...xxx"     a unique identifier of length 32
    private long time; //         long         1528988100000   milliseconds since UNIX epoch in UTC
    private String symbol;//       string       "ETH/BTC"
    private String orderPrice; //   string       "13.5"          optional, limit price of the order. This field is required for limit orders and stop limit orders.
    private String stopPrice; //    string       "15.7"          optional, stop price of the order. This field is required for stop market orders and stop limit orders.
    private String orderQty; //     string       "3.5"
    private String orderType; //    string       "limit"         order type, you shall specify one of the following: "limit", "market", "stop_market", "stop_limit".
    private String side; //         string       "buy"           "buy" or "sell"
    private Boolean postOnly; //     boolean      true            Optional, if true, the order will either be posted to the limit order book or be cancelled, i.e. the order cannot take liquidity; default value is false
    private String timeInForce; //  string       "GTC"           Optional, default is "GTC". Currently, we support "GTC" (good-till-canceled) and "IOC" (immediate-or-cancel).

    public static BitmaxOrderRequest fromOrder(Order order, long timestamp) {
        BitmaxOrderRequest dto = new BitmaxOrderRequest();
        OrderType orderType = parseOrderType(order);

        dto.setCoid(UUID.randomUUID().toString().replace("-", ""));
        dto.setTime(timestamp);
        dto.setSymbol(order.getSymbol().replace("-", "/"));

        double qty = Common.roundDouble(order.getAmount());
        if (order.getOrderSide().buyOrSell() == 0) { //BUY
            dto.setSide("buy");
            qty = Math.abs(qty);
        } else if (order.getOrderSide().buyOrSell() == 1) { //SELL
            dto.setSide("sell");
            qty = Math.abs(qty);
        }
        dto.setOrderQty(qty + "");

        dto.setOrderType(orderType.toString());
        if (orderType.equals(OrderType.LIMIT)) {
            dto.setOrderPrice(order.getPrice() + "");
            dto.setPostOnly(true);
        }
        return dto;
    }

    private static BitmaxOrderRequest.OrderType parseOrderType(Order order) {
        if (order.getOrderType() == null) {
            throw new TIMRuntimeException("Order type is null");
        }
        switch (order.getOrderType()) {
            case "Limit":
                return BitmaxOrderRequest.OrderType.LIMIT;
            case "Market":
                return BitmaxOrderRequest.OrderType.MARKET;
        }
        throw new TIMRuntimeException("Order type not found: " + order.getOrderType());
    }

    public enum OrderType {
        MARKET("market"),
        LIMIT("limit"),
        STOP("stop_market"),
        STOP_LOMIT("stop_limit");
        private final String value;

        OrderType(String value) {
            this.value = value;
        }

        @JsonValue
        @Override
        public String toString() {
            return value;
        }
    }
}
