package pro.belbix.tim.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class CandleKey {
    private String server;
    private String symbol;
    private Integer time;
}
