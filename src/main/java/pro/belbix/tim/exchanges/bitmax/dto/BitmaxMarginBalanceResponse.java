package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

import java.util.List;

@Getter
@Setter
@ToString
public class BitmaxMarginBalanceResponse implements Response {
    private long code;
    private String message;
    private List<BitmaxMarginBalance> data;
}
