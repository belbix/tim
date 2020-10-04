package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class TradeResponse implements Response {
    private String timestamp;
    private String symbol;
    private String side;
    private Double size;
    private Double price;
    private String tickDirection;
    private String trdMatchID;
    private Double grossValue;
    private Double homeNotional;
    private Double foreignNotional;
}
