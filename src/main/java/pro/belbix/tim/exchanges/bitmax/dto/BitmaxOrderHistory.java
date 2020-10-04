package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class BitmaxOrderHistory {
    private long startTime; // 1541100302446
    private long endTime; // 1541111092827,
    private int size; // 49,
    private List<BitmaxOrder> data;
}
