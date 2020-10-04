package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.rest.Request;

import java.util.UUID;

@Getter
@Setter
@ToString
public class BitmaxCancelOrderRequest implements Request {
    private String coid; //        string       "xxx...xxx"     a unique identifier, see POST api/order for details
    private String origCoid; //    string       "yyy...yyy"     the coid of the order to cancel
    private long time; //        long         1528988100000   milliseconds since UNIX epoch in UTC, see POST api/order for details
    private String symbol; //      string       "ETH/BTC"

    public static BitmaxCancelOrderRequest fromOrder(Order order, long timestamp) {
        BitmaxCancelOrderRequest dto = new BitmaxCancelOrderRequest();
        dto.setCoid(UUID.randomUUID().toString().replace("-", ""));
        dto.setOrigCoid(order.getId());
        dto.setTime(timestamp);
        dto.setSymbol(order.getSymbol().replace("-", "/"));
        return dto;
    }
}
