package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class OrderBookResponse implements Response {
    private String symbol;
    private Double id;
    private String side;
    private Double size;
    private Double price;
}
