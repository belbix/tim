package pro.belbix.tim.exchanges.bitmex;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.bitmex.dto.BucketedResponse;
import pro.belbix.tim.exchanges.bitmex.dto.Error;
import pro.belbix.tim.exchanges.bitmex.dto.InstrumentResponse;
import pro.belbix.tim.exchanges.bitmex.dto.TradeResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class BitmexCommon {
    private static final Logger log = LoggerFactory.getLogger(BitmexCommon.class);

    public static String parseErrorMessage(String err) {
        ObjectMapper mapper = new ObjectMapper();
        Error bitmexError;
        try {
            bitmexError = mapper.readValue(err, Error.class);
        } catch (IOException e1) {
            log.error("Error parse error: " + e1.getMessage());
            bitmexError = new Error();
        }
        return bitmexError.toString();
    }

    public static String parseSymbol(String symbol) {
        if (symbol == null || symbol.length() < 3 || symbol.length() > 6) {
            throw new TIMRuntimeException("Order symbol not valid");
        }
        if (symbol.length() > 3) {
            symbol = symbol.substring(0, 3);
        }

        return symbol.replace("BTC", "XBt").replace("XBT", "XBt");
    }

    public static String parseSymbolPair(String orderSymbol) {
        if (orderSymbol == null || orderSymbol.length() < 3 || orderSymbol.length() > 6) {
            throw new TIMRuntimeException("Order symbol not valid");
        }
        if (orderSymbol.length() == 3) {
            orderSymbol += "USD";
        }

        return orderSymbol.replace("BTC", "XBT");
    }

    public static int parseRateLimit(HttpHeaders headers) {
        List<String> rates = headers.get("x-ratelimit-limit");
        if (rates == null || rates.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(rates.get(0));
    }

    public static LocalDateTime parseLimitReset(HttpHeaders headers) {
        List<String> values = headers.get("x-ratelimit-reset");
        if (values == null || values.isEmpty()) {
            return null;
        }
        return LocalDateTime.ofEpochSecond(Integer.parseInt(values.get(0)), 0, UTC);
    }

    public static Tick instrumentResponseToTick(InstrumentResponse instrument) {
        Tick tick = new Tick();
        tick.setStrId(UUID.randomUUID().toString());
        tick.setServer("bitmex");
        tick.setSymbol(instrument.getSymbol());
        tick.setPrice(instrument.getLastPrice());
        tick.setDate(LocalDateTime.ofInstant(Instant.parse(instrument.getTimestamp()), UTC));
        return tick;
    }

    public static Tick tradeResponseToTick(TradeResponse trade) {
        Tick tick = new Tick();
        tick.setStrId(UUID.randomUUID().toString());
        tick.setDate(LocalDateTime.ofInstant(Instant.parse(trade.getTimestamp()), UTC));
        tick.setSymbol(trade.getSymbol());
        tick.setServer("bitmex");
        tick.setBuy(trade.getSide().equals("Buy"));
        tick.setAmount(trade.getSize());
        tick.setPrice(trade.getPrice());
        tick.setStrId(trade.getTrdMatchID());
        return tick;
    }

    public static Candle bucketedToCandle(BucketedResponse r, int time) {
        Candle candle = new Candle();
        candle.setServer("bitmex");
        candle.setDate(LocalDateTime.ofInstant(Instant.parse(r.getTimestamp()), UTC));
        candle.setSymbol(r.getSymbol());
        candle.setTime(time);
        candle.setOpen(r.getOpen());
        candle.setHigh(r.getHigh());
        candle.setLow(r.getLow());
        candle.setClose(r.getClose());
        candle.setAmount(r.getTrades());
        candle.setVolume(r.getVolume());
        return candle;
    }

}
