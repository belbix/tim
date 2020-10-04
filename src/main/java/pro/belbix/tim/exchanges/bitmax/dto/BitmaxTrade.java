package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BitmaxTrade {
    private String p;
    private String q;
    private long t;
    private boolean bm;
}
