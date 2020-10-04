package pro.belbix.tim.exchanges.bitmex.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.bitmex.BitmexCommon;
import pro.belbix.tim.rest.Request;

@Getter
@Setter
@ToString
@Validated
public class OrderRequest implements Request {
    private String clOrdID;
    private String symbol;
    private String side;
    private Integer orderQty;
    private Double price;
    private Double displayQty;
    private Double stopPx;
    private Double pegOffsetValue;
    private String pegPriceType;
    private String ordType;
    private String timeInForce;
    private String execInst;
    private String text;

    public static OrderRequest fromOrder(Order order) {
        if (order.getAmount() == null || order.getAmount() == 0) {
            throw new TIMRuntimeException("Amount not found");
        }

        OrderType orderType = parseOrderType(order);
        int qty = order.getAmount().intValue();
        if (order.getOrderSide().buyOrSell() == 0) { //BUY
            qty = Math.abs(qty);
        } else if (order.getOrderSide().buyOrSell() == 1) { //SELL
            qty = -Math.abs(qty);
        }


        OrderRequest dto = new OrderRequest();
        dto.setSymbol(BitmexCommon.parseSymbolPair(order.getSymbol()));
        dto.setOrderQty(qty);

        if (order.getId() != null && !order.getId().isBlank()) {
            dto.setClOrdID(order.getId() + "");
        }

        dto.setOrdType(orderType.toString());

        if (!orderType.equals(OrderType.MARKET) && order.getPrice() != null && order.getPrice() > 0) {
            dto.setPrice(Long.valueOf(Math.round(order.getPrice())).doubleValue());
        }
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
