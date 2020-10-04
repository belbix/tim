package pro.belbix.tim.exchanges.bitmex.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.rest.Request;

@Getter
@Setter
@ToString
@Validated
public class OrderUpdateRequest implements Request {
    private String orderID;
    private Double orderQty;
    private Double price;
    private Double stopPx;


    public static OrderUpdateRequest fromOrder(Order order) {
        if (order.getPrice() == null || order.getPrice() == 0) {
            throw new TIMRuntimeException("Price not found");
        }

        if (order.getId() == null || order.getId().isBlank()) {
            throw new TIMRuntimeException("ID not found");
        }

        OrderUpdateRequest dto = new OrderUpdateRequest();
        dto.setOrderID(order.getId());
        dto.setPrice(Long.valueOf(Math.round(order.getPrice())).doubleValue());
        return dto;
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

    public enum OrderType {
        MARKET("Market"),
        LIMIT("Limit"),
        STOP("Stop"),
        STOP_LOMIT("StopLimit"),
        MARKET_IF_TOUCHED("MarketIfTouched"),
        LIMIT_IF_TOUCHED("LimitIfTouched"),
        MARKET_WITH_LEFT_OVER_AS_LIMIT("MarketWithLeftOverAsLimit"),
        PEGGED("Pegged");
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
