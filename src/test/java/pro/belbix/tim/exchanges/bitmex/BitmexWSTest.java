package pro.belbix.tim.exchanges.bitmex;

import org.junit.Test;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exchanges.bitmex.dto.WsResponse;
import pro.belbix.tim.exchanges.bitmex.dto.WsTradeResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BitmexWSTest {
    private static final String SUBSCRIPTION_NAME = "XBTUSD";

    @Test
    public void wsSmokeTest() throws InterruptedException, TimeoutException, ExecutionException, TIMRetryException {
        BitmexWS ws = new BitmexWS();
        ws.connect();
        assertTrue("Session should be connected after 60 sec", ws.isOpen());

        ws.sendMessage("\"help\"");

        WsResponse greetings = ws.getWsResponse(60);
        assertEquals("Greetings response", "Welcome to the BitMEX Realtime API.", greetings.getInfo());

        WsResponse response = ws.getWsResponse(60);
        assertEquals("Help response", "See https://www.bitmex.com/app/wsAPI and https://www.bitmex.com/explorer for more documentation.",
                response.getInfo());
    }

    @Test
    public void wsSubscriptionTest() throws InterruptedException, TimeoutException, ExecutionException, TIMRetryException {
        BitmexWS ws = new BitmexWS();
        ws.connect();
        assertTrue("Session should be connected after 60 sec", ws.isOpen());

        ws.subscribeToTrade(SUBSCRIPTION_NAME);

        WsTradeResponse insert = ws.getWsTrades(SUBSCRIPTION_NAME, 60);
        assertEquals("Insert response action", "insert", insert.getAction());
        assertEquals("Insert response symbol", SUBSCRIPTION_NAME, insert.getData().get(0).getSymbol());
    }
}
