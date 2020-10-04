package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Request;

@Getter
@Setter
@ToString
public class BitmaxCancelAllOrderRequest implements Request {
    private String symbol;

    public BitmaxCancelAllOrderRequest(String symbol) {
        this.symbol = symbol;
    }
}
