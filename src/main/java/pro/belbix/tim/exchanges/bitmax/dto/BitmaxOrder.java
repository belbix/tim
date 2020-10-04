package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BitmaxOrder {
    private long time; //        1528988100000,
    private String coid; //        "xxx...xxx",     // the unique identifier, you will need this value to cancel this order
    private String symbol; //      "ETH/BTC",
    private String baseAsset; //   "ETH",
    private String quoteAsset; //  "BTC",
    private String side; //        "buy",
    private String orderPrice; //  "13.45",         // only available for limit and stop limit orders
    private String stopPrice; //   "20.05",         // only available for stop market and stop limit orders
    private String orderQty; //    "3.5",
    private String filled; //      "1.5",           // filled quantity
    private String fee; //         "0.00012",       // cumulative fee paid for this order
    private String feeAsset; //    "ETH",           // the asset
    private String status; //      "PartiallyFilled"
}
