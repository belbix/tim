package pro.belbix.tim.exchanges.bitmax;

import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.bitmax.dto.BitmaxTrade;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;

public class BitmaxCommon {
    private static final String SECOND_SYMBOL = "USDT";

    public static String normalizeSymbolPair(String orderSymbol, String separator) {
        if (orderSymbol == null || orderSymbol.length() < 3 || orderSymbol.length() >= 10) {
            throw new TIMRuntimeException("Order symbol not valid: " + orderSymbol);
        }

        int firstSize = 3;
        if (orderSymbol.length() > 4) {
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

    public static Tick bitmaxTradeToTick(BitmaxTrade bitmaxTrade, String symbol) {
        Tick tick = new Tick();
        tick.setStrId(UUID.randomUUID().toString());
        tick.setServer("bitmax");
        tick.setSymbol(symbol);
        tick.setPrice(Double.parseDouble(bitmaxTrade.getP()));
        tick.setAmount(Double.parseDouble(bitmaxTrade.getQ()));
        tick.setBuy(bitmaxTrade.isBm());
        tick.setDate(LocalDateTime.ofEpochSecond(bitmaxTrade.getT() / 1000,
                Math.round(bitmaxTrade.getT() % 1000) * 1000000, UTC));
        return tick;
    }
}
