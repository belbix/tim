package pro.belbix.tim.exchanges.binance;

import pro.belbix.tim.exchanges.models.Balance;

public class BinanceBalance implements Balance {

    private final String symbol;
    private final Double balance;

    public BinanceBalance(String symbol, Double balance) {
        this.symbol = symbol;
        this.balance = balance;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public Double balance() {
        return balance;
    }

    @Override
    public String toString() {
        return "BinanceBalance{" +
                "symbol='" + symbol + '\'' +
                ", balance=" + balance +
                '}';
    }
}
