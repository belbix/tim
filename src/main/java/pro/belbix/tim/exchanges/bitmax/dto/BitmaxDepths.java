package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

import java.util.Map;

@Getter
@Setter
@ToString
public class BitmaxDepths implements Response {
    private String m;
    private String s;
    private Map<String, String> asks; //[price, quantity]
    private Map<String, String> bids;
}
