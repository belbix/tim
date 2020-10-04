package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WSClient {
    private static final Logger log = LoggerFactory.getLogger(WSClient.class);
    private final AbstractWebSocketHandler socketHandler;

    public WSClient(AbstractWebSocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    public WebSocketSession connect(String url) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            return new StandardWebSocketClient().doHandshake(socketHandler,
                    new WebSocketHttpHeaders(), URI.create(url)).get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("Exception while accessing websockets", e);
            throw e;
        }
    }
}
