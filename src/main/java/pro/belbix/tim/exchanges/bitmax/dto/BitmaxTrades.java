package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

import java.util.List;

@Getter
@Setter
@ToString
public class BitmaxTrades implements Response {
    private String m;
    private String s;
    private List<BitmaxTrade> trades;
}
