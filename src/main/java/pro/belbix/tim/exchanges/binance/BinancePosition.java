package pro.belbix.tim.exchanges.binance;

import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exchanges.models.Position;

public class BinancePosition implements Position {
    private final Double qty;
    private final String symbol;
    private final String server;

    public BinancePosition(Double qty, String symbol, String server) {
        this.qty = qty;
        this.symbol = symbol;
        this.server = server;
    }

    @Override
    public Double currentQty() {
        return qty;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String server() {
        return server;
    }

    @Override
    public boolean compareWithOrder(Order order) {
        return order.getSymbol().equals(symbol);
    }

    @Override
    public String toString() {
        return "BinancePosition{" +
                "qty=" + qty +
                ", symbol='" + symbol + '\'' +
                ", server='" + server + '\'' +
                '}';
    }
}
