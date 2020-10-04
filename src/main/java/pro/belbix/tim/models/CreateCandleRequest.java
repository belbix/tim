package pro.belbix.tim.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
public class CreateCandleRequest {
    private String server;
    private String symbol;
    private LocalDateTime startDate;
    private Set<Integer> timeframes;
    private Map<Integer, LocalDateTime> lastCandleDates = new HashMap<>();

//    public boolean isValid(){
//
//    }
}
