package pro.belbix.tim.exchanges.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderRequest;
import com.binance.api.client.domain.market.OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.exchanges.ExchangesUtils;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;
import pro.belbix.tim.properties.BinanceProperties;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static pro.belbix.tim.utils.Common.doubleToString;
import static pro.belbix.tim.utils.Common.roundDouble;

@Component
public class BinanceREST implements Exchange {
    private static final Logger log = LoggerFactory.getLogger(BinanceREST.class);
    private static final int MAX_ATTEMPTS = 30;
    private static final int ATTEMPT_DELAY = 1000;
    private static final String EXCHANGE_NAME = "binance";
    private static final String SECOND_SYMBOL = "USDT";

    private final BinanceProperties prop;
    private final BinanceApiRestClient client;

    @Autowired
    public BinanceREST(BinanceProperties prop) {
        this.prop = prop;
        this.client = BinanceApiClientFactory.newInstance(prop.getApiKey(), prop.getApiSecret()).newRestClient();
    }

    @Override
    public String getExchangeName() {
        return EXCHANGE_NAME;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Order addOrder(Order order) throws TIMRetryException {
        normalizeOrderSymbol(order);
        log.info("Add a Binance order from " + order);

        try {
            ExchangesUtils.setAmountBySide(order, this);
        } catch (TIMNothingToDo timNothingToDo) {
            log.info("Nothing to do ");
            return order;
        }
        checkLeverage(order);
        checkOrderType(order);

        NewOrder newOrder = OrderConverter.convertToNewBinance(order);

        client.newOrderTest(newOrder);
        NewOrderResponse newOrderResponse = client.newOrder(newOrder);

        log.info("Response for addOrder: " + newOrderResponse.toString());
        order.setId(newOrderResponse.getOrderId().toString());
        order.setStatus(1);
        order.setPrice(Double.parseDouble(newOrderResponse.getPrice()));
        return order;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Order updateOrder(Order order) throws TIMRetryException {
        closeOrder(order);
        return addOrder(order);
    }


    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Order> getOrders(String symbol, int count) throws TIMRetryException {
        symbol = normalizeSymbolPair(symbol, "-");
        return client.getOpenOrders(new OrderRequest(symbol)).stream()
                .map(o -> Order.fromBinanceOrderResponse(o, EXCHANGE_NAME))
                .collect(Collectors.toList());
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Order> closeOrders(List<Order> orders) throws TIMRetryException {
        for (Order o : orders) {
            closeOrder(o);
        }
        return orders;
    }

    private void closeOrder(Order order) {
        normalizeOrderSymbol(order);
        client.cancelOrder(new CancelOrderRequest(order.getSymbol(), order.getId()));
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public void closeAllOrders(String symbol) throws TIMRetryException {
        String symbolN = normalizeSymbolPair(symbol, "-");
        client.getOpenOrders(new OrderRequest(symbol))
                .forEach(o -> client.cancelOrder(new CancelOrderRequest(symbolN, o.getOrderId())));
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Balance getBalance(String symbol) throws TIMRetryException {
        Account account = client.getAccount();
        for (AssetBalance ab : account.getBalances()) {
            if (ab.getAsset().equals(SECOND_SYMBOL))
                return new BinanceBalance(SECOND_SYMBOL, Double.parseDouble(ab.getFree()));
        }

        return new BinanceBalance(symbol, 0d);
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Balance> getBalances(List<String> symbols) throws TIMRetryException {
        Account account = client.getAccount();

        Set<String> symbolsSet = new HashSet<>();
        symbols.forEach(s -> symbolsSet.add(normalizeSymbolPair(s, "-")));

        List<Balance> balances = new ArrayList<>();
        for (AssetBalance ab : account.getBalances()) {
            if (symbolsSet.contains(ab.getAsset()))
                balances.add(new BinanceBalance(ab.getAsset(), Double.parseDouble(ab.getFree())));
        }

        return balances;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Tick lastPrice(String symbol) throws TIMRetryException {
        symbol = normalizeSymbolPair(symbol, "-");
        Tick tick = new Tick();
        tick.setDate(LocalDateTime.now());
        tick.setStrId(UUID.randomUUID().toString());
        tick.setServer(EXCHANGE_NAME);
        tick.setPrice(Double.parseDouble(client.getPrice(symbol).getPrice()));
        return tick;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Position> positions() throws TIMRetryException {
        List<Position> positions = new ArrayList<>();
        List<BinanceBalance> balances = allBalance();
        for (BinanceBalance balance : balances) {
            if (balance.symbol().equals(SECOND_SYMBOL)) {
                continue;
            }
            positions.add(new BinancePosition(balance.balance(), balance.symbol() + SECOND_SYMBOL, EXCHANGE_NAME));
        }
        return positions;
    }

    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    private List<BinanceBalance> allBalance() {
        List<BinanceBalance> balances = new ArrayList<>();
        client.getAccount().getBalances()
                .forEach(b -> {
                    double pos = Double.parseDouble(b.getFree());
                    if (pos > minimalAmountByAsset(b.getAsset())) {
                        balances.add(new BinanceBalance(b.getAsset(), pos));
                    }
                });
        return balances;
    }

    private double minimalAmountByAsset(String symbol) {
        switch (symbol) {
            case "BTC":
                return 0.001;
            default:
                return 0.001;
        }
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> historyTicks(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException {
        return null;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> getOrderBook(String symbol, int depth) throws TIMRetryException {
        symbol = normalizeSymbolPair(symbol, "-");
        OrderBook orderBook = client.getOrderBook(symbol, 5);
        List<Tick> ticks = new ArrayList<>();
        orderBook.getAsks().forEach(e -> {
            Tick tick = Tick.fromBinanceResponse(e);
            tick.setBuy(true);
            ticks.add(tick);
        });
        orderBook.getBids().forEach(e -> {
            Tick tick = Tick.fromBinanceResponse(e);
            tick.setBuy(false);
            ticks.add(tick);
        });
        return ticks;
    }

    @Override
    public void loadAmountAndPrice(Order order) throws TIMRetryException, TIMNothingToDo {
        String symbol;
        if (order.getOrderSide().isLong()) {
            symbol = SECOND_SYMBOL;
        } else {
            symbol = normalizeSymbolPair(order.getSymbol(), "").replace(SECOND_SYMBOL, "");
        }
        Balance balance = getBalance(symbol);
        double actualBalanceInOrderSymbol = balance.balance();
        actualBalanceInOrderSymbol *= (prop.getAmountPercent() / 100d);
        if (order.getPrice() == null) loadPrice(order);

        if (order.getAdditionalAmount() != 0) {
            log.warn("Additional  Amount: " + order.getAdditionalAmount());
            actualBalanceInOrderSymbol += Math.abs(order.getAdditionalAmount());
        }
        actualBalanceInOrderSymbol = actualBalanceInOrderSymbol / order.getPrice();
        actualBalanceInOrderSymbol = roundDouble(actualBalanceInOrderSymbol);
        log.info(symbol + " Final Amount : " + doubleToString(actualBalanceInOrderSymbol));
        if (actualBalanceInOrderSymbol == 0) {
            log.warn("actualBalanceInOrderSymbol = 0");
            throw new TIMNothingToDo();
        }
        order.setAmount(actualBalanceInOrderSymbol);
    }

    private void loadPrice(Order order) throws TIMRetryException {
        Tick tick = lastPrice(order.getSymbol());
        double lastPrice = 0;
        if (tick.getPrice() != null && tick.getPrice() != 0) {
            lastPrice = tick.getPrice();
            log.info("Last  price: " + lastPrice);
        }
        order.setPrice(lastPrice);
    }

    @Override
    public String normalizeSymbolPair(String orderSymbol, String separator) {
        if (orderSymbol == null || orderSymbol.length() < 3 || orderSymbol.length() >= 10) {
            throw new TIMRuntimeException("Order symbol not valid: " + orderSymbol);
        }
        separator = "";
        int firstSize = 3;
        if (orderSymbol.length() > 5) {
            if (orderSymbol.contains("/")) {
                firstSize = orderSymbol.split("/")[0].length();
            } else if (orderSymbol.contains("-")) {
                firstSize = orderSymbol.split("-")[0].length();
            } else if (orderSymbol.endsWith("USDT")) {
                firstSize = orderSymbol.replace("USDT", "").length();
            } else if (orderSymbol.endsWith("USD")) {
                firstSize = orderSymbol.replace("USD", "").length();
            } else if (orderSymbol.endsWith("BTC")) {
                firstSize = orderSymbol.replace("BTC", "").length();
            }
        } else {
            firstSize = orderSymbol.length();
        }

        if (orderSymbol.startsWith("USD") || orderSymbol.startsWith("USDT")) return "USDT" + separator + "USDT";
        if (!orderSymbol.contains("USDT") && orderSymbol.contains("USD")) {
            orderSymbol = orderSymbol.replace("USD", "USDT");
        }
        orderSymbol = orderSymbol.replace("-", "").replace("/", "");
        if (orderSymbol.length() == firstSize) {
            orderSymbol += separator + SECOND_SYMBOL;
        } else {
            if (!orderSymbol.contains(separator)) {
                orderSymbol = orderSymbol.substring(0, firstSize) + separator + orderSymbol.substring(firstSize);
            }
        }

        return orderSymbol.replace("XBT", "BTC");
    }

    private void normalizeOrderSymbol(Order order) {
        order.setSymbol(normalizeSymbolPair(order.getSymbol(), "-"));
    }

    private void checkLeverage(Order order) {
    }

    private void checkOrderType(Order order) {
        if (order.getOrderType() == null) {
            order.setOrderType(prop.getOrderTypeDefault());
        }
    }
}
