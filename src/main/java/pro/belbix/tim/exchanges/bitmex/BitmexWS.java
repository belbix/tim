package pro.belbix.tim.exchanges.bitmex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exchanges.bitmex.dto.WSCommand;
import pro.belbix.tim.exchanges.bitmex.dto.WsPositionResponse;
import pro.belbix.tim.exchanges.bitmex.dto.WsResponse;
import pro.belbix.tim.exchanges.bitmex.dto.WsTradeResponse;
import pro.belbix.tim.services.WSClient;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Component
public class BitmexWS {
    private static final Logger log = LoggerFactory.getLogger(BitmexWS.class);
    private static final String URL = "wss://www.bitmex.com/realtime";
    private static final int QUEUE_SIZE = 100_000;
    private final BlockingQueue<WsResponse> queueResponses = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private final Map<String, BlockingQueue<WsResponse>> queues = new HashMap<>();
    private WebSocketSession session;
    private boolean validOpen = false;
    private Instant lastData = Instant.now();

    public void connect() throws InterruptedException, ExecutionException, TimeoutException {
        log.info("Starting Bitmex Web Socket client...");
        WSClient wsClient = new WSClient(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                log.debug("in - " + message.getPayload());
                lastData = Instant.now();
                try {
                    while (true) {
                        boolean success = parsePayload(message.getPayload());
//                        boolean success = queue.offer(message.getPayload(), 10, TimeUnit.SECONDS);
                        if (!success) {
                            log.warn("WS Bitmex Queue is full!");
                        } else {
                            break;
                        }
                    }
                } catch (InterruptedException e) {
                    log.error("Handle ws message error", e);
                }
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                log.info("established connection - " + session);
            }

            @Override
            protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
                log.info("WS Pong: " + message);
                super.handlePongMessage(session, message);
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                log.error("WS transport error", exception);
                super.handleTransportError(session, exception);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
                super.afterConnectionClosed(session, status);
            }
        });

        session = wsClient.connect(URL);
    }

    public void validOpen() throws TIMRetryException {
        if (isOpen()) {
            log.info("Try open already opened connection, disconnect");
            disconnect();
        }
        log.info("Open valid WS connection");
        try {
            connect();
            WsResponse greetings = getWsResponse(60);
            if (greetings != null && greetings.getInfo() != null
                    && !greetings.getInfo().equals("Welcome to the BitMEX Realtime API.")) {
                log.warn("Wait greetings, but was: " + greetings);
                disconnect();
                throw new TIMRetryException("Error connect to ws");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("Error open ws", e);
            return;
        }
        log.info("WS Connection successful opened");
        validOpen = true;
    }

    private boolean parsePayload(String payload) throws InterruptedException {
        WsResponse response = WsResponse.fromString(payload);
        if (response.getTable() == null) {
            return queueResponses.offer(response, 10, TimeUnit.SECONDS);
        }

        if (response.getTable() != null) {
            if (response.getTable().startsWith("trade")) {
                WsTradeResponse wsTradeResponse = WsTradeResponse.fromString(payload);
                if (!wsTradeResponse.getData().isEmpty()) {
                    String symbol = wsTradeResponse.getData().get(0).getSymbol();
                    BlockingQueue<WsResponse> queue = queues.get(symbol);
                    if (queue == null) {
                        throw new IllegalStateException("Queue not found for: " + payload);
                    }
                    return queue.offer(wsTradeResponse, 10, TimeUnit.SECONDS);
                } else {
                    log.info("Skip trade response without data: " + payload);
                }

            } else if (response.getTable().startsWith("position")) {
                BlockingQueue<WsResponse> queue = queues.get("position");
                return queue.offer(WsPositionResponse.fromString(payload), 10, TimeUnit.SECONDS);
            }
        }
        throw new IllegalStateException("Unknown payload: " + payload);
    }

    public void disconnect() {
        log.info("WS gentle disconnect");
        validOpen = false;
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("Error close ws", e);
            }
        }
        session = null;
        queues.clear();
    }

    public void sendMessage(String text) throws TIMRetryException {
        try {
            TextMessage message = new TextMessage(text);
            session.sendMessage(message);
        } catch (IOException e) {
            log.error("Error send: " + text, e);
            throw new TIMRetryException(e.getMessage());
        }
    }

    public WsResponse getWsResponse(int timeout) {
        return getFromQueue(queueResponses, timeout);
    }

    public WsTradeResponse getWsTrades(String name, int timeout) {
        BlockingQueue<WsResponse> queue = queues.get(name);
        if (queue == null) {
            log.error("Queue not found " + name);
            return null;
        }
        return (WsTradeResponse) getFromQueue(queue, timeout);
    }

    private <T extends WsResponse> T getFromQueue(BlockingQueue<T> queue, int timeout) {
        if (timeout < 1) {
            return queue.poll();
        } else {
            try {
                return queue.poll(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Error while wait poll msg from WS queue");
            }
        }
        return null;
    }

    public List<WsTradeResponse> getAllTradeResponses(String name) {
        List<WsTradeResponse> wsResponses = new ArrayList<>();
        while (true) {
            WsTradeResponse response = getWsTrades(name, 0);
            if (response != null) {
                wsResponses.add(response);
            } else {
                break;
            }
        }
        return wsResponses;
    }

    public void subscribeToTrade(String name) throws TIMRetryException {
        if (queues.containsKey(name)) {
            throw new IllegalStateException("Ticks already subscribed");
        }
        queues.put(name, new ArrayBlockingQueue<>(QUEUE_SIZE));
        log.info("Subscribe for " + name + ". Queues size:" + queues.size());

        WSCommand command = new WSCommand();
        command.setOp("subscribe");
        command.setArgs(Collections.singletonList("trade:" + name));
        sendMessage(command.toJson());
        //------------ RESPONSE ON COMMAND --------------
        while (true) {
            WsResponse subscribe = getWsResponse(60);
            if (subscribe != null
                    && subscribe.getSubscribe() != null
                    && subscribe.getSubscribe().equals("trade:" + name)) {
                break;
            } else {
                log.info("Wait subscription on " + name);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        //-------------- PARTIAL RESPONSE --------------
        while (true) {
            WsResponse partial = getWsTrades(name, 60);
            if (partial != null && partial.getAction().equals("partial")) {
                break;
            } else {
                log.info("Wait partial on " + name);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        log.info("Subscribed on " + name);
    }

    public boolean isTickSubscribed(String name) {
        return queues.containsKey(name);
    }

    public boolean isOpen() {
        if (session == null) return false;
        return session.isOpen();
    }

    public boolean isValidOpen() {
        return validOpen && isOpen();
    }

    public Instant getLastData() {
        return lastData;
    }
}
