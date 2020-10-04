package pro.belbix.tim.exchanges.bitfinex;

import com.github.jnidzwetzki.bitfinex.v2.BitfinexClientFactory;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketClient;
import com.github.jnidzwetzki.bitfinex.v2.BitfinexWebsocketConfiguration;
import org.springframework.stereotype.Component;

@Component
public class BitfinexWS {
    private final String apiKey = "";
    private final String apiSecret = "";
    private BitfinexWebsocketClient bitfinexClient;

    private void connect() {
        final BitfinexWebsocketConfiguration config = new BitfinexWebsocketConfiguration();
        config.setApiCredentials(apiKey, apiSecret);
        bitfinexClient = BitfinexClientFactory.newSimpleClient(config);
        bitfinexClient.connect();
    }

}
