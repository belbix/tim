package pro.belbix.tim.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.exchanges.bitmax.dto.BitmaxOrder;
import pro.belbix.tim.exchanges.bitmex.dto.BitmexOrderResponse;
import pro.belbix.tim.models.OrderSide;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

import static java.time.ZoneOffset.UTC;

@Entity
@Table(name = "orders")
@Cacheable(false)
@Getter
@Setter
@ToString
public class Order {
    @Id
    @Column(length = 50)
    private String id;
    @Column(insertable = false, updatable = false, length = 15)
    private String server;
    @Column(insertable = false, updatable = false, length = 10)
    private String symbol;
    private LocalDateTime dateCreate;
    private Integer type;
    private Double amount;
    private Double price;
    private Integer status;
    private String orderType;

    private OrderSide orderSide;
    private double additionalAmount;
    private String ordStatus;
    private String sideEx;
    private double decisionPrice;
    private double actualPrice;

    public static Order fromBitmexOrderResponse(BitmexOrderResponse response, String exchangeName) {
        Order order = new Order();
        order.setId(response.getOrderID());
        order.setAmount(response.getOrderQty());
        order.setOrdStatus(response.getOrdStatus());
        order.setSymbol(response.getSymbol());
        order.setServer(exchangeName);
        order.setSideEx(response.getSide());
//        order.setOrderSide(); //we dont know him
        order.setOrderType(response.getOrdType());
        Instant d = Instant.parse(response.getTransactTime());
        order.setDateCreate(LocalDateTime.ofInstant(d, UTC));
        order.setPrice(response.getPrice());
        return order;
    }

    public static Order fromBitmaxOrderResponse(BitmaxOrder bitmaxOrder, String exchangeName) {
        Order order = new Order();
        order.setId(bitmaxOrder.getCoid());
        order.setAmount(Double.parseDouble(bitmaxOrder.getOrderQty()));
        order.setOrdStatus(bitmaxOrder.getStatus());
        order.setSymbol(bitmaxOrder.getSymbol());
        order.setServer(exchangeName);
        order.setSideEx(bitmaxOrder.getSide());
        Instant d = Instant.ofEpochMilli(bitmaxOrder.getTime());
        order.setDateCreate(LocalDateTime.ofInstant(d, UTC));
        order.setPrice(Double.parseDouble(bitmaxOrder.getOrderPrice()));
        return order;
    }

    public static Order fromBinanceOrderResponse(com.binance.api.client.domain.account.Order binanceOrder, String exchangeName) {
        Order order = new Order();
        order.setId(binanceOrder.getOrderId().toString());
        order.setAmount(Double.parseDouble(binanceOrder.getOrigQty()));
        order.setOrdStatus(binanceOrder.getStatus().name());
        order.setSymbol(binanceOrder.getSymbol());
        order.setServer(exchangeName);
        order.setSideEx(binanceOrder.getSide().name());
        Instant d = Instant.ofEpochMilli(binanceOrder.getTime());
        order.setDateCreate(LocalDateTime.ofInstant(d, UTC));
        order.setPrice(Double.parseDouble(binanceOrder.getPrice()));
        return order;
    }

    public boolean isTheSameCurrency(Order order) {
//        if (!this.server.equals(order.getServer())) return false;
        if (!this.symbol.equals(order.getSymbol())) return false;
        return true;
    }

    public boolean isCompleted() {
        if (ordStatus.equals("Filled")) return true;
        if (ordStatus.equals("Canceled")) return true;
        if (ordStatus.equals("Rejected")) return true;
        return false;
    }

    public boolean isBuy() {
        if (sideEx != null && sideEx.equals("Buy")) return true;
        if (orderSide != null && orderSide.buyOrSell() == 0) return true;
        return false;
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
