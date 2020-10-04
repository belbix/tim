package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BucketedResponse {
    private String timestamp;
    private String symbol;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double trades;
    private Double volume;
    private Double vwap;
    private Double lastSize;
    private Double turnover;
    private Double homeNotional;
    private Double foreignNotional;
}
