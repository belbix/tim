package pro.belbix.tim.strategies.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.entity.Candle;

import java.time.Instant;

@Getter
@Setter
@ToString
public class Trade {
    private Candle candle;
    private Instant createDate;

    public static Trade fromCandle(Candle candle) {
        Trade trade = new Trade();
        trade.setCandle(new Candle(candle));
        trade.setCreateDate(Instant.now());
        return trade;
    }
}
