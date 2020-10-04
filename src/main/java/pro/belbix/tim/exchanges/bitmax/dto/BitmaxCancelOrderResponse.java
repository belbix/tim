package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

import java.util.Map;

@Getter
@Setter
@ToString
public class BitmaxCancelOrderResponse implements Response {
    private int code;
    private String message;
    private Map<String, String> data;
}
