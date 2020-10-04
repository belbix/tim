package pro.belbix.tim.exchanges;

import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;

import java.time.LocalDateTime;
import java.util.List;

public interface Exchange {
    String getExchangeName();

    Order addOrder(Order order) throws TIMRetryException;

    Order updateOrder(Order order) throws TIMRetryException;

    List<Order> getOrders(String symbol, int count) throws TIMRetryException;

    List<Order> closeOrders(List<Order> orders) throws TIMRetryException;

    void closeAllOrders(String symbol) throws TIMRetryException;

    Balance getBalance(String symbol) throws TIMRetryException;

    List<Balance> getBalances(List<String> symbols) throws TIMRetryException;

    Tick lastPrice(String symbol) throws TIMRetryException;

    List<Position> positions() throws TIMRetryException;

    List<Tick> historyTicks(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException;

    default List<Tick> historyTicksWs(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException {
        return null;
    }

    List<Tick> getOrderBook(String symbol, int depth) throws TIMRetryException;

    void loadAmountAndPrice(Order order) throws TIMRetryException, TIMNothingToDo;

    String normalizeSymbolPair(String orderSymbol, String separator);

}
