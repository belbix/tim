package pro.belbix.tim.models;

import pro.belbix.tim.exceptions.TIMRuntimeException;

public enum OrderSide {
    LONG_OPEN(0, 0), LONG_CLOSE(0, 1), SHORT_OPEN(1, 0), SHORT_CLOSE(1, 1);
    private final int longOrShort;
    private final int openOrClose;

    OrderSide(int longOrShort, int openOrClose) {
        this.longOrShort = longOrShort;
        this.openOrClose = openOrClose;
    }

    public int longOrShort() {
        return longOrShort;
    }

    public boolean isLong() {
        return longOrShort == 0;
    }

    public boolean isShort() {
        return longOrShort == 1;
    }

    public int openOrClose() {
        return openOrClose;
    }

    /**
     * 0 - buy, 1 - sell
     */
    public int buyOrSell() {
        if (longOrShort == 0) {
            if (openOrClose == 0) return 0;
            if (openOrClose == 1) return 1;
        }
        if (longOrShort == 1) {
            if (openOrClose == 0) return 1;
            if (openOrClose == 1) return 0;
        }
        throw new TIMRuntimeException("Invalid OrderSide " + this);
    }

    public boolean isOpen() {
        return this.openOrClose == 0;
    }

    public boolean isClose() {
        return this.openOrClose == 1;
    }

    public boolean isTheSameDirection(OrderSide orderSide) {
        return this.longOrShort == orderSide.longOrShort;
    }
}
