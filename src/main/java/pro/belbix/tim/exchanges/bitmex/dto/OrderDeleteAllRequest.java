package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.rest.Request;

@Getter
@Setter
@ToString
@Validated
@NoArgsConstructor
public class OrderDeleteAllRequest implements Request {
    private String symbol;

    public OrderDeleteAllRequest(String symbol) {
        this.symbol = symbol;
    }
}
