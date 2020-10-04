package pro.belbix.tim.exchanges;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMDuplicatePosition;
import pro.belbix.tim.exceptions.TIMNothingToDo;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.models.Position;

import java.util.List;

import static pro.belbix.tim.models.OrderSide.LONG_CLOSE;
import static pro.belbix.tim.models.OrderSide.SHORT_CLOSE;

public class ExchangesUtils {
    private static final Logger log = LoggerFactory.getLogger(ExchangesUtils.class);

    public static void setAmountBySide(Order order, Exchange exchange) throws TIMRetryException, TIMNothingToDo {
        if (order.getOrderSide() == null)
            throw new TIMRuntimeException("Order side is null: " + order);
        double openPosition = openPosition(order, exchange);
        log.info("Open position: " + openPosition + " for Order Side: " + order.getOrderSide());
        switch (order.getOrderSide()) {
            case SHORT_OPEN:
            case LONG_OPEN:
                if (order.getAdditionalAmount() == 0) {
                    if (openPosition != 0) {
                        log.info("Close all orders in confuse situation");
                        exchange.closeAllOrders(order.getSymbol());
                        throw new TIMDuplicatePosition("Trying to open new position when old not closed!");
                    }
                }//if have additional amount - we have position and revers it
                exchange.loadAmountAndPrice(order);
                return;
            case LONG_CLOSE:
            case SHORT_CLOSE:
                if (order.getAdditionalAmount() == 0) {
                    if (openPosition < 0 && order.getOrderSide().equals(LONG_CLOSE))
                        throw new TIMDuplicatePosition("Trying to close position with different side: " + openPosition);
                    if (openPosition > 0 && order.getOrderSide().equals(SHORT_CLOSE))
                        throw new TIMDuplicatePosition("Trying to close position with different side: " + openPosition);
                }
                if (openPosition == 0) {
                    log.info("Close all orders in confuse situation");
                    exchange.closeAllOrders(order.getSymbol());
                    throw new TIMDuplicatePosition("Trying to close position without position");
                }
                order.setAmount(openPosition);
                return;
        }
    }

    private static double openPosition(Order order, Exchange exchange) throws TIMRetryException {
        List<Position> positions = exchange.positions();
        if (positions == null || positions.isEmpty()) return 0;
        for (Position position : positions) {
            log.debug("Open position: " + position);
            String symbol = order.getSymbol();
            log.info("Find position " + symbol + " for " + position);
            if (position.compareWithOrder(order)) {
                return position.currentQty();
            }
        }
        return 0;
    }

    public static void checkLastPrice(Order order, Exchange exchange) throws TIMRetryException {
        log.info("checkLastPrice");
        List<Tick> ticks = exchange.getOrderBook(order.getSymbol(), 1);
        if (ticks == null || ticks.isEmpty()) {
            throw new TIMRuntimeException("Can't get actual price for " + order);
        }
        log.info("got ticks for checking: " + ticks.size());
        double buyTick = 0;
        double sellTick = Double.MAX_VALUE;
        for (Tick tick : ticks) {
            if (tick.getBuy()) {
                if (buyTick < tick.getPrice()) {
                    buyTick = tick.getPrice();
                }
            } else {
                if (sellTick > tick.getPrice()) {
                    sellTick = tick.getPrice();
                }
            }
        }

        if (order.isBuy()) {
            order.setActualPrice(buyTick);
            order.setPrice(buyTick);
            log.info("Last price for buy candle: " + buyTick);
        } else {
            order.setActualPrice(sellTick);
            order.setPrice(sellTick);
            log.info("Last price for sell candle: " + sellTick);
        }
    }

    public static void addDiffToPrice(Order order, double diffForOpenPricePerc) {
        log.info("addDiffToPrice");
        double price = order.getPrice();
        if (order.isBuy()) {
            if (diffForOpenPricePerc != 0) {
                double diff = (price * diffForOpenPricePerc) / 100;
                log.info("Add to price diff: " + diff);
                price = price - diff;
            }
        } else {
            if (diffForOpenPricePerc != 0) {
                double diff = (price * diffForOpenPricePerc) / 100;
                log.info("Add to price diff: " + diff);
                price = price + diff;
            }
        }
        order.setPrice(price);
        log.info("Set price with diff: " + price);
    }

}
