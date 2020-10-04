package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class BitmaxOrderResponse implements Response {
    private int code;
    private String message;
    private BitmaxOrderStatus data;
}
