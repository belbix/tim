package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BitmaxOrderHistoryResponse {
    private int code;
    private String message;
    private BitmaxOrderHistory data;
}
