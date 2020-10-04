package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class BitmaxQuote implements Response {
    private String symbol; //   "ETH/BTC",
    private String bidPrice; // "0.033048",
    private String bidSize; //  "1.56",
    private String askPrice; // "0.033057",
    private String askSize; //  "0.108"
}
