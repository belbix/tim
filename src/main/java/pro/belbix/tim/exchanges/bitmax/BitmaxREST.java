package pro.belbix.tim.exchanges.bitmax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMDuplicatePosition;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.exchanges.ExchangesUtils;
import pro.belbix.tim.exchanges.bitmax.dto.*;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;
import pro.belbix.tim.properties.BitmaxProperties;
import pro.belbix.tim.rest.Request;
import pro.belbix.tim.rest.Response;
import pro.belbix.tim.rest.RestService;
import pro.belbix.tim.utils.Common;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class BitmaxREST implements Exchange {
    private static final Logger log = LoggerFactory.getLogger(BitmaxREST.class);
    private static final int MAX_ATTEMPTS = 30;
    private static final int ATTEMPT_DELAY = 1000;
    private static final String EXCHANGE_NAME = "bitmax";


    private final RestService restService;
    private final BitmaxProperties prop;

    private BitmaxUserInfo bitmaxUserInfo = null;

    @Autowired
    public BitmaxREST(RestService restService, BitmaxProperties prop) {
        this.restService = restService;
        this.prop = prop;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Order addOrder(Order order) throws TIMRetryException {
        String requestUrl = prop.getUrl() + prop.getMarginPrefix() + "order ";
        order.setSymbol(BitmaxCommon.normalizeSymbolPair(order.getSymbol(), "-"));
        log.info("Add order from " + order);
        if (order.getPrice() == null && order.getOrderSide().openOrClose() == 1) loadPrice(order);
        try {
            if (prop.isAmountBySide()) ExchangesUtils.setAmountBySide(order, this);
        } catch (TIMNothingToDo | TIMDuplicatePosition e) {
            log.error("Nothing to do: " + e.getMessage());
            return null;
        }
        checkOrderType(order);


        BitmaxQuery<BitmaxOrderRequest, BitmaxOrderResponse> query = buildQuery(requestUrl, HttpMethod.POST);
        long timestamp = System.currentTimeMillis();
        BitmaxOrderRequest bitmaxOrderRequest = BitmaxOrderRequest.fromOrder(order, timestamp);

        log.info("orderRequest for add order: " + bitmaxOrderRequest);
        query.setRequestModel(bitmaxOrderRequest);
        query.setResponseClass(BitmaxOrderResponse.class);
        query.setCoid(bitmaxOrderRequest.getCoid());
        query.setTimestamp(timestamp);


        BitmaxOrderResponse response = post(query);
        log.info("Response for addOrder: " + response.toString());
        if (response.getCode() != 0)
            throw new TIMRuntimeException(response.getCode() + " Error add Order: " + response.getMessage());

        order.setId(bitmaxOrderRequest.getCoid());
        order.setStatus(1);
        order.setServer(EXCHANGE_NAME);
        order.setSymbol(bitmaxOrderRequest.getSymbol());
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
        String requestUrl = prop.getUrl() + prop.getMarginPrefix() + "order/open";

        BitmaxQuery<?, BitmexAllOrdersResponse> query = buildQuery(requestUrl, HttpMethod.GET);
        query.setResponseClass(BitmexAllOrdersResponse.class);

        BitmexAllOrdersResponse response = get(query);
        if (response.getCode() != 0)
            throw new TIMRuntimeException(response.getCode() + " Error get Orders: " + response.getMessage());
        List<Order> orders = new ArrayList<>();
        if (response.getData().isEmpty()) return orders;
        if (symbol != null) symbol = BitmaxCommon.normalizeSymbolPair(symbol, "/");
        for (BitmaxOrder bitmaxOrder : response.getData()) {
            Order o = Order.fromBitmaxOrderResponse(bitmaxOrder, EXCHANGE_NAME);
            if (symbol != null && !o.getSymbol().equals(symbol)) continue;
            orders.add(o);
        }

        return orders;
    }

    @Override
    public List<Order> closeOrders(List<Order> orders) throws TIMRetryException {
        List<Order> ordersResult = new ArrayList<>();
        for (Order order : orders) {
            ordersResult.add(closeOrder(order));
        }
        return ordersResult;
    }

    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Order closeOrder(Order order) throws TIMRetryException {
        String requestUrl = prop.getUrl() + prop.getMarginPrefix() + "order";
        order.setSymbol(BitmaxCommon.normalizeSymbolPair(order.getSymbol(), "/"));

        BitmaxQuery<BitmaxCancelOrderRequest, BitmaxCancelOrderResponse> query = buildQuery(requestUrl, HttpMethod.DELETE);
        long timestamp = System.currentTimeMillis();
        BitmaxCancelOrderRequest bitmaxCancelOrderRequest = BitmaxCancelOrderRequest.fromOrder(order, timestamp);

        log.info("orderRequest for cancel order: " + bitmaxCancelOrderRequest);
        query.setRequestModel(bitmaxCancelOrderRequest);
        query.setResponseClass(BitmaxCancelOrderResponse.class);
        query.setCoid(bitmaxCancelOrderRequest.getCoid());
        query.setTimestamp(timestamp);


        BitmaxCancelOrderResponse response = delete(query);
        log.info("Response for closeOrder: " + response.toString());
        if (response.getCode() != 0) {
            if (response.getCode() == 60060) {
                log.warn(response.getMessage());
            }
            throw new TIMRuntimeException(response.getCode() + " Error close Order: " + response.getMessage());
        }
        return order;

    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public void closeAllOrders(String symbol) throws TIMRetryException {
        List<Order> orders = getOrders(symbol, 50);
        log.info("open orders: " + orders.size());
        closeOrders(orders);
    }

//    public void closeAllOrdersV2(String symbol) throws TIMRetryException {
//        String requestUrl = prop.getUrl() + MARGIN_PREFIX + "order/all";
//        requestUrl = requestUrl.replace("v1", "r/v2");
//        symbol = BitmaxCommon.normalizeSymbolPair(symbol);
//
//        BitmaxQuery<BitmaxCancelAllOrderRequest, BitmaxCancelAllOrderResponse> query = buildQuery(requestUrl, HttpMethod.DELETE);
//        long timestamp = System.currentTimeMillis();
//
//        query.setRequestModel(new BitmaxCancelAllOrderRequest(symbol.replace("-", "/")));
//        query.setResponseClass(BitmaxCancelAllOrderResponse.class);
////        query.setCoid(bitmaxCancelOrderRequest.getCoid());
//        query.setTimestamp(timestamp);
//
//
//        BitmaxCancelAllOrderResponse response = delete(query);
//        log.info("Response for close all Orders: " + response.toString());
//        if (response.getCode() != 0)
//            throw new TIMRuntimeException(response.getCode() + " Error close all Order: " + response.getMessage());
//    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> getOrderBook(String symbol, int depth) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "quote";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .queryParam("symbol", BitmaxCommon.normalizeSymbolPair(symbol, "-"))
                .queryParam("n", depth);

        BitmaxQuery<?, BitmaxQuote> query = buildOpenQuery(builder);
        query.setResponseClass(BitmaxQuote.class);

        BitmaxQuote response = get(query);

        if (response.getBidPrice().isBlank() || response.getAskPrice().isBlank()) return null;

        List<Tick> ticks = new ArrayList<>();

        Tick tickBid = new Tick();
        tickBid.setStrId(UUID.randomUUID().toString());
        tickBid.setServer("bitmax");
        tickBid.setSymbol(response.getSymbol());
        tickBid.setAmount(Double.parseDouble(response.getBidSize()));
        tickBid.setPrice(Double.parseDouble(response.getBidPrice()));
        tickBid.setDate(LocalDateTime.now());
        tickBid.setBuy(true);
        ticks.add(tickBid);

        Tick tickAsk = new Tick();
        tickAsk.setStrId(UUID.randomUUID().toString());
        tickAsk.setServer("bitmax");
        tickAsk.setSymbol(response.getSymbol());
        tickAsk.setAmount(Double.parseDouble(response.getAskSize()));
        tickAsk.setPrice(Double.parseDouble(response.getAskPrice()));
        tickAsk.setDate(LocalDateTime.now());
        tickAsk.setBuy(false);
        ticks.add(tickAsk);
        return ticks;
    }

    @Override
    public void loadAmountAndPrice(Order order) throws TIMRetryException, TIMNothingToDo {
        String symbol;
        if (order.getOrderSide().longOrShort() == 0) {
            symbol = BitmaxCommon.normalizeSymbolPair(order.getSymbol(), "-").split("-")[1];
        } else {
            symbol = order.getSymbol();
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
        actualBalanceInOrderSymbol = Common.roundDouble(actualBalanceInOrderSymbol);
        log.info(symbol + " Final Amount : " + actualBalanceInOrderSymbol);
        if (actualBalanceInOrderSymbol == 0) {
            log.warn("actualBalanceInOrderSymbol = 0");
            throw new TIMNothingToDo();
        }
        order.setAmount(actualBalanceInOrderSymbol);
    }

    @Override
    public String normalizeSymbolPair(String orderSymbol, String separator) {
        return BitmaxCommon.normalizeSymbolPair(orderSymbol, separator);
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
    public Balance getBalance(String symbol) throws TIMRetryException {
        BitmaxMarginBalanceResponse response = allBalance();
        return findBalance(response, symbol);
    }

    private Balance findBalance(BitmaxMarginBalanceResponse bitmaxMarginBalanceResponse, String symbol) {
        symbol = BitmaxCommon.normalizeSymbolPair(symbol, "-").split("-")[0];
        for (BitmaxMarginBalance balance : bitmaxMarginBalanceResponse.getData()) {
            if (balance.getAssetCode().equals(symbol)) {
                return balance;
            }
        }
        throw new TIMRuntimeException("Incorrect symbol " + symbol);
    }

    @Override
    public List<Balance> getBalances(List<String> symbols) throws TIMRetryException {
        BitmaxMarginBalanceResponse response = allBalance();
        List<Balance> balances = new ArrayList<>();
        for (String symbol : symbols) {
            if (symbol == null || symbol.isBlank()) continue;
            balances.add(findBalance(response, symbol));
        }
        return balances;
    }

    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public BitmaxMarginBalanceResponse allBalance() throws TIMRetryException {
        String requestUrl = prop.getUrl() + prop.getMarginPrefix() + "balance";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        BitmaxQuery<?, BitmaxMarginBalanceResponse> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(BitmaxMarginBalanceResponse.class);

        BitmaxMarginBalanceResponse response = get(query);
        if (response == null || response.getData() == null || response.getData().isEmpty()) return null;

        if (response.getCode() != 0) throw new TIMRuntimeException("Error get balance " + response.getMessage());
        return response;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Tick lastPrice(String symbol) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "trades";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .queryParam("symbol", BitmaxCommon.normalizeSymbolPair(symbol, "-"))
                .queryParam("n", 1);

        BitmaxQuery<?, BitmaxTrades> query = buildOpenQuery(builder);
        query.setResponseClass(BitmaxTrades.class);

        BitmaxTrades response = get(query);
        if (response == null || response.getTrades() == null || response.getTrades().isEmpty()) return null;
        return BitmaxCommon.bitmaxTradeToTick(response.getTrades().get(0), response.getS());
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Position> positions() throws TIMRetryException {
        String defaultPair = "USDT";
        List<Position> positions = new ArrayList<>();
        BitmaxMarginBalanceResponse balanceResponse = allBalance();
        for (BitmaxMarginBalance balance : balanceResponse.getData()) {
            if (balance.currentQty() != 0) {
                if (balance.getAssetCode().equals(defaultPair)) {
                    continue;
                }
//                balance.setAssetCode(balance.getAssetCode() + "/" + defaultPair);
                positions.add(balance);
            }
        }
        return positions;
    }

    public List<BitmaxOrder> historyOrders() throws TIMRetryException {
        String requestUrl = prop.getUrl() + "order/history";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .queryParam("symbol", "null")
                .queryParam("n", 50);

        BitmaxQuery<?, BitmaxOrderHistoryResponse> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(BitmaxOrderHistoryResponse.class);

        BitmaxOrderHistoryResponse response = get(query);
        log.info("Response for historyOrders: " + response.toString());
        if (response.getCode() != 0)
            throw new TIMRuntimeException(response.getCode() + " Error history Orders: " + response.getMessage());
        return response.getData().getData();
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> historyTicks(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException {
        throw new TIMRuntimeException("Not support");
    }

    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    private BitmaxUserInfo getUserInfo() throws TIMRetryException {
        if (bitmaxUserInfo != null) return bitmaxUserInfo;

        String requestUrl = prop.getUrl() + "user/info";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        BitmaxQuery<?, BitmaxUserInfo> query = buildQuery(builder.toUriString(), HttpMethod.GET, "");
        query.setResponseClass(BitmaxUserInfo.class);

        BitmaxUserInfo response = get(query);
        if (response == null || response.getAccountGroup() == null || response.getAccountGroup().isBlank())
            throw new TIMRuntimeException("Not found user info");
        bitmaxUserInfo = response;
        return response;
    }

    private <T extends Request, N extends Response> N post(BitmaxQuery<T, N> query)
            throws TIMRetryException {
        waitRateLimit();
        try {
            HttpEntity<T> httpEntity = query.buildHttpEntity();
            String url = query.getFullUrl();
            ResponseEntity<N> response = restService.post(url, httpEntity, query.getResponseClass());
            return response.getBody();
        } catch (Exception e) {
            log.info(query.toString());
            handleError(e);
            throw e;
        }
    }

    private <T extends Request, N extends Response> N put(BitmaxQuery<T, N> query)
            throws TIMRetryException {
        waitRateLimit();
        try {
            HttpEntity<T> entity = query.buildHttpEntity();
            ResponseEntity<N> response =
                    restService.put(query.getFullUrl(), entity, query.getResponseClass());
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private <T> T get(BitmaxQuery query) throws TIMRetryException {
        waitRateLimit();
        try {
            HttpEntity<T> entity = new HttpEntity<>(query.buildHeaders());
            @SuppressWarnings("unchecked")
            ResponseEntity<T> response =
                    restService.get(query.getFullUrl(), entity, query.getResponseClass());
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private <T extends Request, N> N delete(BitmaxQuery<T, N> query)
            throws TIMRetryException {
        waitRateLimit();
        try {
            HttpEntity<T> entity = query.buildHttpEntity();
            ResponseEntity<N> response =
                    restService.delete(query.getFullUrl(), entity, query.getResponseClass());
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private void waitRateLimit() {
    }

    private <T extends Request, N> BitmaxQuery<T, N> buildQuery(String url, HttpMethod method) throws TIMRetryException {
        return buildQuery(url, method, getUserInfo().getAccountGroup());
    }


    private <T extends Request, N> BitmaxQuery<T, N> buildQuery(String url, HttpMethod method, String group) {
        BitmaxQuery<T, N> query = new BitmaxQuery<>();
        query.setApiKey(prop.getApiKey());
        query.setApiSecret(prop.getApiSecret());
        query.setFullUrl(url);
        query.setHttpMethod(method);
        query.setGroup(group);
        return query;
    }

    private <T extends Request, N> BitmaxQuery<T, N> buildOpenQuery(UriComponentsBuilder builder) {
        BitmaxQuery<T, N> query = buildQuery(builder.toUriString(), HttpMethod.GET, "");
        query.setUseAuth(false);
        return query;
    }

    private void handleError(Exception ex) throws TIMRetryException {
        log.error("" + ex.getMessage(), ex);
//        throw new TIMRetryException(ex.getMessage());

        if (ex instanceof HttpClientErrorException) {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            log.error(httpEx.getStatusCode().value() + ": " + httpEx.getStatusCode().getReasonPhrase());

            if ((httpEx.getStatusCode().is5xxServerError()) || httpEx.getStatusCode().is4xxClientError()) {
                log.info("Error for trying...");
                throw new TIMRuntimeException(httpEx.toString());
            }
        } else {
            log.error("Unknown error: " + ex.getMessage());
            throw new TIMRetryException("Handle unknown error: " + ex.getMessage(), ex);
        }


    }

    private void checkOrderType(Order order) {
        if (order.getOrderType() == null) {
            order.setOrderType("market");
        }
    }


    public String getExchangeName() {
        return EXCHANGE_NAME;
    }
}
