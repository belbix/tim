package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.rest.Request;

import java.util.List;

@Getter
@Setter
@ToString
@Validated
public class OrderDeleteRequest implements Request {
    private String orderID;

    public static OrderDeleteRequest fromOrders(List<Order> orders) {
        OrderDeleteRequest dto = new OrderDeleteRequest();
        StringBuilder sb = new StringBuilder();
        for (Order order : orders) {
            if (order.getId() == null || order.getId().isBlank())
                throw new TIMRuntimeException("Empty ID");

            sb.append(order.getId()).append(",");
        }
        sb.setLength(sb.length() - 1);
        dto.setOrderID(sb.toString());
        return dto;
    }
}
