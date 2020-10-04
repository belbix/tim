package pro.belbix.tim.exchanges.bitmax.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class BitmexAllOrdersResponse {
    private int code;
    private String message;
    private List<BitmaxOrder> data;
}
