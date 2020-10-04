package pro.belbix.tim.exchanges.bitmex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.exchanges.ExchangesUtils;
import pro.belbix.tim.exchanges.bitmex.dto.*;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;
import pro.belbix.tim.properties.BitmexProperties;
import pro.belbix.tim.rest.Request;
import pro.belbix.tim.rest.Response;
import pro.belbix.tim.rest.RestService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static pro.belbix.tim.utils.Common.roundDouble;

@Component
public class BitmexREST implements Exchange {
    private static final Logger log = LoggerFactory.getLogger(BitmexREST.class);
    private static final int MAX_ATTEMPTS = 15;
    private static final int ATTEMPT_DELAY = 60000;
    private static final String EXCHANGE_NAME = "bitmex";
    private final RestService restService;
    private final BitmexProperties prop;
    private final BitmexWS ws;
    private LocalDateTime rateLimitReset = null;
    private int currentRateLimit = -1;

    @Autowired
    public BitmexREST(RestService restService, BitmexProperties prop, BitmexWS ws) {
        this.restService = restService;
        this.prop = prop;
        this.ws = ws;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Order addOrder(Order order) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "order";

        log.info("Add order from " + order);

        try {
            ExchangesUtils.setAmountBySide(order, this);
        } catch (TIMNothingToDo timNothingToDo) {
            log.info("Nothing to do");
            return order;
        }
        checkOrderType(order);
        checkLeverage(order);

        BitmexQuery<OrderRequest, BitmexOrderResponse> query = buildQuery(requestUrl, HttpMethod.POST);
        OrderRequest orderRequest = OrderRequest.fromOrder(order);
        log.info("orderRequest for add order: " + orderRequest);
        query.setRequestModel(orderRequest);
        query.setResponseClass(BitmexOrderResponse.class);

        BitmexOrderResponse response = post(query);
        log.info("Response for addOrder: " + response.toString());
        order.setId(response.getOrderID());
        order.setStatus(1);
        order.setPrice(response.getPrice());
        return order;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Order updateOrder(Order order) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "order";

        BitmexQuery<OrderUpdateRequest, BitmexOrderResponse> query = buildQuery(requestUrl, HttpMethod.PUT);
        query.setRequestModel(OrderUpdateRequest.fromOrder(order));
        query.setResponseClass(BitmexOrderResponse.class);

        BitmexOrderResponse response = put(query);
        log.info("Response for updateOrder: " + response.toString());
        order.setPrice(response.getPrice());
        return order;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Order> getOrders(String symbol, int count) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "order";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        if (symbol != null) {
            builder.queryParam("symbol", BitmexCommon.parseSymbol(symbol));
        }
        builder.queryParam("reverse", "true");
        builder.queryParam("count", count);

        BitmexQuery<?, BitmexOrderResponse[]> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(BitmexOrderResponse[].class);

        BitmexOrderResponse[] response = get(query);
        log.trace("Response for getOrders: " + Arrays.toString(response));
        List<Order> orders = new ArrayList<>();
        for (BitmexOrderResponse orderResponse : response) {
            orders.add(Order.fromBitmexOrderResponse(orderResponse, EXCHANGE_NAME));
        }
        return orders;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Order> closeOrders(List<Order> orders) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "order";

        BitmexQuery<OrderDeleteRequest, BitmexOrderResponse[]> query = buildQuery(requestUrl, HttpMethod.DELETE);
        query.setRequestModel(OrderDeleteRequest.fromOrders(orders));
        query.setResponseClass(BitmexOrderResponse[].class);


        BitmexOrderResponse[] response = delete(query);
        log.info("Response for closeOrders: " + Arrays.toString(response));

        List<Order> ordersResult = new ArrayList<>();
        for (BitmexOrderResponse orderResponse : response) {
            ordersResult.add(Order.fromBitmexOrderResponse(orderResponse, EXCHANGE_NAME));
        }

        return ordersResult;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public void closeAllOrders(String symbol) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "order/all";

        BitmexQuery<OrderDeleteAllRequest, BitmexOrderResponse[]> query = buildQuery(requestUrl, HttpMethod.DELETE);
        query.setRequestModel(new OrderDeleteAllRequest(symbol));
        query.setResponseClass(BitmexOrderResponse[].class);


        BitmexOrderResponse[] response = delete(query);
        log.info("Response for closeAllOrders: " + Arrays.toString(response));
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> getOrderBook(String symbol, int depth) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "orderBook/L2";
        log.info("getOrderBook " + requestUrl + " with " + symbol + " " + depth);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        builder.queryParam("symbol", symbol);
        builder.queryParam("depth", depth);

        BitmexQuery<?, OrderBookResponse[]> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(OrderBookResponse[].class);

        OrderBookResponse[] response = get(query);
        log.debug("Response for getOrderBook: " + Arrays.toString(response));
        List<Tick> ticks = new ArrayList<>();
        for (OrderBookResponse book : response) {
            ticks.add(Tick.fromOrderBookResponse(book));
        }
        return ticks;
    }


    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public WalletResponse wallet(String symbol) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "user/wallet";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        builder.queryParam("currency", BitmexCommon.parseSymbol(symbol));

        BitmexQuery<?, WalletResponse> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(WalletResponse.class);

        WalletResponse response = get(query);
        log.info("Response for balance: " + response.toString());
        return response;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Balance getBalance(String symbol) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "user/walletHistory";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);
        builder.queryParam("currency", BitmexCommon.parseSymbol(symbol));
        builder.queryParam("count", "1");

        BitmexQuery<?, WalletHistoryResponse[]> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(WalletHistoryResponse[].class);

        WalletHistoryResponse[] response = get(query);
        if (response.length == 0) return null;
        return response[0];
    }

    @Override
    public List<Balance> getBalances(List<String> symbols) throws TIMRetryException {
        throw new TIMRuntimeException("Not support");
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public Tick lastPrice(String symbol) throws TIMRetryException {
        String requestUrl = prop.getUrl() + "instrument";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .queryParam("symbol", BitmexCommon.parseSymbolPair(symbol))
                .queryParam("reverse", "true")
                .queryParam("columns", "symbol,bidPrice,askPrice,lastPrice,volume,timestamp");

        BitmexQuery<?, InstrumentResponse[]> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(InstrumentResponse[].class);

        InstrumentResponse[] response = get(query);
        if (response.length == 0) return null;
        return BitmexCommon.instrumentResponseToTick(response[0]);
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Position> positions() throws TIMRetryException {
        String requestUrl = prop.getUrl() + "position";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl);

        BitmexQuery<?, PositionResponse[]> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        query.setResponseClass(PositionResponse[].class);

        PositionResponse[] response = get(query);
        return Arrays.asList(response);
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> historyTicks(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException {
        String requestUrl = prop.getUrlOpenData() + "trade";
        log.trace("requestUrl:" + requestUrl);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .queryParam("symbol", symbol)
                .queryParam("count", limit);
        log.trace("builder init");
        if (startTime != null) {
            builder.queryParam("startTime", startTime);
            log.trace("builder queryParam startTime");
        } else { //get top ticks
            builder.queryParam("reverse", true);
            log.trace("builder queryParam reverse");
        }

        if (endTime != null) {
            builder.queryParam("endTime", endTime);
            log.trace("builder queryParam endTime");
        }
        if (start != 0) {
            builder.queryParam("start", start);
            log.trace("builder queryParam start");
        }
        BitmexQuery<?, TradeResponse[]> query = buildOpenQuery(builder);
        log.trace("query init");
        query.setResponseClass(TradeResponse[].class);

        TradeResponse[] response = get(query);
        log.trace("query get responce");
        if (response.length == 0) return null;
        List<Tick> ticks = new ArrayList<>();
        for (TradeResponse tradeResponse : response) {
            ticks.add(BitmexCommon.tradeResponseToTick(tradeResponse));
        }
        log.trace("get ticks:" + ticks.size());
        return ticks;
    }

    @Override
    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Tick> historyTicksWs(String symbol, int limit, LocalDateTime startTime, LocalDateTime endTime, int start) throws TIMRetryException {
        if (Duration.between(startTime, LocalDateTime.now()).toMinutes() > 5) {
            log.info("Too old start date for ws");
            return Collections.emptyList();
        }

        //------------------- OPEN -------------------------
        if (!ws.isValidOpen()) {
            log.info("WS is invalid, reopen");
            ws.disconnect();
            ws.validOpen();
        } else if (Duration.between(ws.getLastData(), Instant.now()).toMinutes() > 60) {
            log.info("WS not recieved data a long time, reopen");
            ws.disconnect();
            ws.validOpen();
        }
        //------------------- SUBSCRIBE -------------------------
        if (!ws.isTickSubscribed(symbol)) {
            ws.subscribeToTrade(symbol);
        }
        //-------------- GET ALL LAST TRADES -----------------
        List<Tick> ticks = new ArrayList<>();
        List<WsTradeResponse> trades;
        while (true) {
            trades = ws.getAllTradeResponses(symbol);
            if (trades == null) {
                log.info("Wait new trade from WS");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
            } else {
                break;
            }
        }
        for (WsTradeResponse trade : trades) {
            if (trade.getData() != null) {
                for (TradeResponse tradeResponse : trade.getData()) {
                    ticks.add(BitmexCommon.tradeResponseToTick(tradeResponse));
                }
            } else {
                log.warn("Incorrect type of response: " + trade);
            }
        }
        //----------------- CHECK TIME ------------------
        if (!ticks.isEmpty()) {
            LocalDateTime wsDate = ticks.get(ticks.size() - 1).getDate();
            if (Duration.between(startTime, wsDate).toMinutes() < 2) {
                log.info("Got from ws: " + ticks.size());
                return ticks;
            } else {
                log.info("Skip ws " + wsDate + " dur:" + Duration.between(startTime, wsDate).toMinutes());
            }
        }
        return ticks;
    }

    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public void leverage(String symbol, double value) throws TIMRetryException {
        log.info("Leverage for " + symbol + " set to " + value);
        String requestUrl = prop.getUrl() + "position/leverage";

        BitmexQuery<LeverageRequest, LeverageResponse> query = buildQuery(requestUrl, HttpMethod.POST);
        query.setRequestModel(new LeverageRequest(symbol, value));
        query.setResponseClass(LeverageResponse.class);

        LeverageResponse response = post(query);
        log.info("Response for leverage: " + response.toString());
    }

    @Retryable(
            value = {TIMRetryException.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public List<Candle> candles(String symbol, String timeframe, LocalDateTime startTime, LocalDateTime endTime, int count) throws TIMRetryException {
        String requestUrl = prop.getUrlOpenData() + "trade/bucketed";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(requestUrl)
                .queryParam("binSize", timeframe)
                .queryParam("count", count)
                .queryParam("symbol", symbol);
        if (startTime != null) {
            builder.queryParam("startTime", startTime);
        }
        if (endTime != null) {
            builder.queryParam("endTime", endTime);
        }
        BitmexQuery<?, BucketedResponse[]> query = buildOpenQuery(builder);
        query.setResponseClass(BucketedResponse[].class);

        BucketedResponse[] response = get(query);
        if (response.length == 0) return null;
        List<Candle> candles = new ArrayList<>();
        for (BucketedResponse bucketedResponse : response) {
            candles.add(BitmexCommon.bucketedToCandle(bucketedResponse, pro.belbix.tim.utils.Common.minutesFromPeriod(timeframe)));
        }
        return candles;
    }

    private <T extends Request, N> BitmexQuery<T, N> buildOpenQuery(UriComponentsBuilder builder) {
        BitmexQuery<T, N> query = buildQuery(builder.toUriString(), HttpMethod.GET);
        if (prop.getUseAuthForOpenData()) {
            query.setApiKey(prop.getApiKeyOpenData());
            query.setApiSecret(prop.getApiSecretOpenData());
        } else {
            query.setUseAuth(false);
        }
        return query;
    }


    private <T extends Request, N extends Response> N post(BitmexQuery<T, N> query)
            throws TIMRetryException {
        waitRateLimit();
        try {
            String url = query.getFullUrl();
            HttpEntity<T> httpEntity = query.buildHttpEntity();
            ResponseEntity<N> response = restService.post(url, httpEntity, query.getResponseClass());
            currentRateLimit = BitmexCommon.parseRateLimit(response.getHeaders());
            log.debug("Rate Limit: " + currentRateLimit);
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private <T extends Request, N extends Response> N put(BitmexQuery<T, N> query)
            throws TIMRetryException {
        waitRateLimit();
        try {
            ResponseEntity<N> response =
                    restService.put(query.getFullUrl(), query.buildHttpEntity(), query.getResponseClass());
            currentRateLimit = BitmexCommon.parseRateLimit(response.getHeaders());
            log.debug("Rate Limit: " + currentRateLimit);
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private <T> T get(BitmexQuery<?, T> query) throws TIMRetryException {
        waitRateLimit();
        try {
            ResponseEntity<T> response =
                    restService.get(query.getFullUrl(), new HttpEntity<>(query.buildHeaders()), query.getResponseClass());
            currentRateLimit = BitmexCommon.parseRateLimit(response.getHeaders());
            log.debug("Rate Limit: " + currentRateLimit);
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private <T extends Request, N> N delete(BitmexQuery<T, N> query)
            throws TIMRetryException {
        waitRateLimit();
        try {
            ResponseEntity<N> response =
                    restService.delete(query.getFullUrl(), query.buildHttpEntity(), query.getResponseClass());
            currentRateLimit = BitmexCommon.parseRateLimit(response.getHeaders());
            log.debug("Rate Limit: " + currentRateLimit);
            return response.getBody();
        } catch (Exception e) {
            handleError(e);
            throw e;
        }
    }

    private void waitRateLimit() {
        if (rateLimitReset != null) {
            long wait = Duration.between(LocalDateTime.now(), rateLimitReset).toMillis();
            log.info("Wait rate limit: " + wait + " ms");
            if (wait < 1) return;
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ignored) {
            }
            rateLimitReset = null;
        }
    }

    @Override
    public void loadAmountAndPrice(Order order) throws TIMRetryException, TIMNothingToDo {
        Balance balance = getBalance(order.getSymbol());
        double actualBalanceInBTC = normalizeBalance(balance);

        Tick tick = lastPrice(order.getSymbol());
        double lastPrice = 0;
        if (tick.getPrice() != null && tick.getPrice() != 0) {
            lastPrice = tick.getPrice();
            log.info("Last price: " + lastPrice);
            if (order.getPrice() == null) order.setPrice(lastPrice); //we set order price in order service
        }
        double amountInUSD = actualBalanceInBTC * lastPrice;

        if (order.getAdditionalAmount() != 0) {
            log.warn("Additional Amount: " + order.getAdditionalAmount());
        }
        amountInUSD += order.getAdditionalAmount();

        amountInUSD = (double) Math.round(amountInUSD);
        log.info("Final Amount: " + amountInUSD);

        if (amountInUSD == 0) throw new TIMNothingToDo();

        order.setAmount(amountInUSD);
    }

    @Override
    public String normalizeSymbolPair(String orderSymbol, String separator) {
        return BitmexCommon.parseSymbol(orderSymbol);
    }

    public double normalizeBalance(Balance balance) {
        double actualBalance;
        if (balance.balance() != null && balance.balance() != 0) {
            double xBtBalance = balance.balance() / 100000000;
            actualBalance = xBtBalance * ((double) prop.getAmountPercent() / 100d) * (double) prop.getLeverage();
            actualBalance = roundDouble(actualBalance);
            log.info("Actual balance for trade: " + actualBalance);
        } else {
            throw new TIMRuntimeException("Balance is zero or invalid");
        }
        return actualBalance;
    }


    private void checkLeverage(Order order) {
        if (order.getOrderSide().openOrClose() == 1) return;
        try {
            leverage(order.getSymbol(), prop.getLeverage());
        } catch (Exception e) {
            log.error("Error set leverage for " + order.getSymbol() + " to " + prop.getLeverage());
            log.info("Continue creating order without change leverage...");
        }
    }


    private <T extends Request, N> BitmexQuery<T, N> buildQuery(String url, HttpMethod method) {
        BitmexQuery<T, N> query = new BitmexQuery<>();
        query.setApiKey(prop.getApiKey());
        query.setApiSecret(prop.getApiSecret());
        query.setFullUrl(url);
        query.setHttpMethod(method);
        return query;
    }

    private void handleError(Exception ex) throws TIMRetryException {
        if (ex instanceof HttpClientErrorException) {
            HttpClientErrorException httpEx = (HttpClientErrorException) ex;
            String errMessage = BitmexCommon.parseErrorMessage(httpEx.getResponseBodyAsString());
            log.error(httpEx.getStatusCode().value() + ": " + httpEx.getStatusCode().getReasonPhrase() + " " + errMessage);

            if (httpEx.getStatusCode().value() == 429) {
                tooManyRequest(httpEx.getResponseHeaders());
                throw new TIMRetryException(httpEx.getStatusCode().toString());
            }

            if (httpEx.getStatusCode().value() == 400
                    && httpEx.getStatusCode().getReasonPhrase().contains("Account has insufficient Available Balance")) {
                log.info("Try again, may be it is just another open order");
                throw new TIMRetryException(httpEx.getStatusCode().toString());
            }

            if ((httpEx.getStatusCode().is5xxServerError())
                    && !prop.getNoRetryableCodes().contains(httpEx.getStatusCode().value())) {
                log.info("Error for trying...");
                throw new TIMRetryException(httpEx.getStatusCode().toString());
            }
        } else {
            log.error(ex.getClass().getName() + " Unknown error: " + ex.getMessage());
            throw new TIMRetryException("Handle unknown error: " + ex.getMessage(), ex);
        }
    }

    private void tooManyRequest(HttpHeaders headers) {
        LocalDateTime limitReset = BitmexCommon.parseLimitReset(headers);
        log.warn("Too many request, rate Limit: " + currentRateLimit + " limitReset: " + limitReset);
        if (limitReset == null) {
            rateLimitReset = LocalDateTime.now().plus(60, ChronoUnit.SECONDS);
        } else {
            rateLimitReset = limitReset.plus(10, ChronoUnit.SECONDS);
        }
    }

    private void checkOrderType(Order order) {
        if (order.getOrderType() == null) {
            order.setOrderType(prop.getOrderTypeDefault());
        }
    }

    public String getExchangeName() {
        return EXCHANGE_NAME;
    }
}
