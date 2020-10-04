package pro.belbix.tim.strategies.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.models.OrderSide;

public class PositionStatus {
    static final Logger log = LoggerFactory.getLogger(PositionStatus.class);
    private Candle openDecisionCandleLong;
    private Candle openDecisionCandleShort;

    public boolean isOpenLong() {
        if (openDecisionCandleLong != null
                && openDecisionCandleLong.getOrderSide().isShort())
            throw new IllegalStateException("Long decision has short");
        return openDecisionCandleLong != null;
    }

    public boolean isOpenShort() {
        if (openDecisionCandleShort != null
                && openDecisionCandleShort.getOrderSide().isLong())
            throw new IllegalStateException("Short decision has long");
        return openDecisionCandleShort != null;
    }

    public void clear() {
        openDecisionCandleLong = null;
        openDecisionCandleShort = null;
    }

    public OrderSide calcPosition(Candle decision) {
        if (openDecisionCandleLong != null) {
            if (openDecisionCandleShort != null) throw new IllegalStateException("Double decision");
            return openDecisionCandleLong.getOrderSide();
        } else if (openDecisionCandleShort != null) {
            return openDecisionCandleShort.getOrderSide();
        }
        if (decision != null) return decision.getOrderSide();
        return null;
    }

    public void setLongOpen(Candle candle) {
        printPositionStatus();
        log.info("makeLongOpen open long set " + candle.getOrderSide());
        openDecisionCandleShort = null;
        openDecisionCandleLong = new Candle(candle);
    }

    public void setLongClose() {
        printPositionStatus();
        log.info("makeLongClose open long set null");
        openDecisionCandleLong = null;
    }


    public void setShortOpen(Candle candle) {
        printPositionStatus();
        log.info("makeShortOpen open short set " + candle.getOrderSide());
        openDecisionCandleLong = null;
        openDecisionCandleShort = new Candle(candle);
    }

    public void setShortClose() {
        printPositionStatus();
        log.info("makeShortClose open short set null");
        openDecisionCandleShort = null;
    }

    private void printPositionStatus() {
        String msg = "POSITION STATUS BEFORE SETUP | ";
        if (openDecisionCandleLong != null) {
            msg += " openDecisionCandleLong: " + openDecisionCandleLong.getOrderSide();
        } else {
            msg += " openDecisionCandleLong: null";
        }

        if (openDecisionCandleShort != null) {
            msg += " openDecisionCandleShort: " + openDecisionCandleShort.getOrderSide();
        } else {
            msg += " openDecisionCandleShort: null";
        }
        log.info(msg);
    }

}
