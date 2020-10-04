package pro.belbix.tim.exchanges.bitfinex;

import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;

import java.time.LocalDateTime;
import java.util.List;

public class Bitfinex implements Exchange {

    private static final String EXCHANGE_NAME = "bitfinex";

    @Override
    public String getExchangeName() {
        return EXCHANGE_NAME;
    }

    @Override
    public Order addOrder(Order order) throws TIMRetryException {
        return null;
    }

    @Override
    public Order updateOrder(Order order) throws TIMRetryException {
        return null;
    }

    @Override
    public List<Order> getOrders(String symbol, int count) throws TIMRetryException {
        return null;
    }

    @Override
    public List<Order> closeOrders(List<Order> orders) throws TIMRetryException {
        return null;
    }

    @Override
    public void closeAllOrders(String symbol) throws TIMRetryException {

    }

    @Override
    public Balance getBalance(String symbol) throws TIMRetryException {
        return null;
    }

    @Override
    public List<Balance> getBalances(List<String> symbols) throws TIMRetryException {
        return null;
    }

    @Override
    public Tick lastPrice(String symbol) throws TIMRetryException {
        return null;
    }

    @Override
    public List<Position> positions() throws TIMRetryException {
        return null;
    }

    @Override
    public List<Tick> historyTicks(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException {
        return null;
    }

    @Override
    public List<Tick> getOrderBook(String symbol, int depth) throws TIMRetryException {
        return null;
    }

    @Override
    public void loadAmountAndPrice(Order order) throws TIMRetryException, TIMNothingToDo {

    }

    @Override
    public String normalizeSymbolPair(String orderSymbol, String separator) {
        return null;
    }
}
