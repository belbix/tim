package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BitmaxOrderStatus {
    private String coid; // "xxx...xxx",
    private String action; // "new",
    private boolean success; // true
}
