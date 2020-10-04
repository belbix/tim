package pro.belbix.tim.history;

import lombok.Getter;
import lombok.Setter;
import pro.belbix.tim.entity.Candle;

import java.time.LocalDateTime;

@Getter
@Setter
public class Position {
    private final double decisionPrice;
    private final double price;
    private final LocalDateTime created;
    private final int updateCount;
    private final Position reverse;
    private final Candle candle;
    private boolean immediatelyExecute = false;
    private boolean executed = false;

    public Position(Candle candle, double price, double decisionPrice, LocalDateTime created) {
        this(candle, price, decisionPrice, created, 0, null);
    }

    public Position(Candle candle, double price, double decisionPrice, LocalDateTime created, int updateCount) {
        this(candle, price, decisionPrice, created, updateCount, null);
    }

    public Position(Candle candle, double price, double decisionPrice, LocalDateTime created, int updateCount, Position reverse) {
        this.candle = candle;
        this.decisionPrice = decisionPrice;
        this.price = price;
        this.created = created;
        this.updateCount = updateCount;
        this.reverse = reverse;
    }

    @Override
    public String toString() {
        return "Position{" +
                "side=" + candle.getOrderSide() +
                ", price=" + price +
                ", decisionPrice=" + decisionPrice +
                ", created=" + created +
                ", updateCount=" + updateCount +
                ", immediatelyExecute=" + immediatelyExecute +
                ", reverse=" + reverse +
                ", candle=" + candle +
                '}';
    }
}
