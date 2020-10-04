package pro.belbix.tim.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.models.OrderSide;
import pro.belbix.tim.properties.OrderServiceProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.services.TickService;
import pro.belbix.tim.strategies.Strategy;

import java.time.LocalDateTime;

public class DecisionService {
    private static final Logger log = LoggerFactory.getLogger(DecisionService.class);
    private final Strategy strategy;
    private final TickService tickService;
    private final OrderServiceProperties orderServiceProperties;

    private Position position;

    public DecisionService(Strategy strategy, TickService tickService, OrderServiceProperties orderServiceProperties) {
        this.strategy = strategy;
        this.tickService = tickService;
        this.orderServiceProperties = orderServiceProperties;
    }

    private static double priceToPosition(Candle candle, double price, double difForOpenPricePerc) {
        if (difForOpenPricePerc == 0) {
            return price;
        }

        if (candle.isBuy()) {
            double diff = (price * difForOpenPricePerc) / 100;
            return price - diff;

        } else {
            double diff = (price * difForOpenPricePerc) / 100;
            return price + diff;
        }
    }

    public Tick executeDecision(Candle candleDecision, LocalDateTime dateStart) {
        if (position == null && candleDecision == null) return null;
        StrategyProperties prop = strategy.getStrategyProperties();
        Tick tick = tickService.getTickAtDate(prop.getServer(), prop.getSymbol(), dateStart);
        if (tick == null) {
            if (position == null) {
                return null;
            } else {
                Tick t = new Tick();
                t.setPrice(position.getPrice());
                return t;
            }
        }

        if (checkNewPosition(candleDecision, tick)) {
            if (position.isImmediatelyExecute()) {
                return tick;
            }
            return null;
        }

        if (checkReversePosition(candleDecision, tick)) {
            return null;
        }

        if (checkExecutionPosition(tick)) {
            return tick;
        }

        if (checkUpdatePosition(tick)) {
            if (checkMaxUpdateCountPosition(tick)) {
                return tick;
            } else {
                return null;
            }
        }

        return null;
    }

    private boolean checkNewPosition(Candle candleDecision, Tick tick) {
        boolean r = false;
        if (position == null) {
            if (candleDecision == null) {
                throw new IllegalStateException("Null candle decision for open position");
            }
            if (checkClosePosition(candleDecision, tick)) {
                r = true;
            } else {
                createNew(candleDecision, tick);
                log.warn("new " + position);
                r = true;
            }
        }
        return r;
    }

    private void deleteReversePosition() {
        if (position.getReverse() != null) {
            log.warn("Set reverse position as main when position removed");
            position = position.getReverse(); //because this trade not include in trade statuses
        } else {
            position = null; //because this trade not include in trade statuses
        }
    }

    private boolean checkExecutionPosition(Tick tick) {
        if (position.getCandle().isBuy()) {
            if (position.getPrice() > tick.getPrice()) {
                log.warn("Execute buy " + position);
                tick.setPrice(position.getPrice());
                return true;
            }
        } else {
            if (position.getPrice() < tick.getPrice()) {
                log.warn("Execute sell " + position);
                tick.setPrice(position.getPrice());
                return true;
            }
        }
        return false;
    }

    private boolean checkUpdatePosition(Tick tick) {
        double difForPricePerc = orderServiceProperties.getDifForPricePerc();
        double priceChangeForUpdatePerc = orderServiceProperties.getPriceChangeForUpdatePerc();
        double delta;
        if (position.getCandle().isBuy()) {
            delta = tick.getPrice() - position.getDecisionPrice();
        } else {
            delta = position.getDecisionPrice() - tick.getPrice();
        }
        double priceChange = (delta / tick.getPrice()) * 100;

        if (priceChange > priceChangeForUpdatePerc) {
            Position newPosReverse = null;
            if (position.getReverse() != null) {
                newPosReverse = new Position(new Candle(position.getReverse().getCandle()),
                        priceToPosition(position.getReverse().getCandle(), tick.getPrice(), difForPricePerc),
                        tick.getPrice(),
                        tick.getDate());
            }
            position = new Position(new Candle(position.getCandle()),
                    priceToPosition(position.getCandle(), tick.getPrice(), difForPricePerc),
                    tick.getPrice(),
                    tick.getDate(),
                    position.getUpdateCount() + 1,
                    newPosReverse);
            log.info("Update " + position);
            return true;
        }
        return false;
    }

    private boolean checkMaxUpdateCountPosition(Tick tick) {
        int maxUpdateCount = (int) Math.round(orderServiceProperties.getMaxUpdateCount());
        int updateCount = position.getUpdateCount();
        if (updateCount >= maxUpdateCount) {
            if (maxUpdateCount > 0) {
                log.warn("Max update count, Execute " + position);
            }
            position = new Position(new Candle(position.getCandle()),
                    tick.getPrice(),
                    tick.getPrice(),
                    tick.getDate(),
                    position.getUpdateCount(),
                    position.getReverse());
            position.setImmediatelyExecute(true);
            return true;
        }
        return false;
    }

    private void createNew(Candle candleDecision, Tick tick) {
        double difForPricePerc = orderServiceProperties.getDifForPricePerc();

        position = new Position(new Candle(candleDecision),
                priceToPosition(new Candle(candleDecision), tick.getPrice(), difForPricePerc),
                tick.getPrice(),
                tick.getDate());
        int maxUpdateCount = (int) Math.round(orderServiceProperties.getMaxUpdateCount());
        if (maxUpdateCount < 1) {
            position.setImmediatelyExecute(true);
        }
    }

    private void reverse(Candle candleDecision, Tick tick) {
        if (position.getReverse() != null) {
            position = position.getReverse();
            log.warn("double reverse " + position);
            return;
        }
        double difForPricePerc = orderServiceProperties.getDifForPricePerc();
        position = new Position(new Candle(candleDecision),
                priceToPosition(new Candle(candleDecision), tick.getPrice(), difForPricePerc),
                tick.getPrice(),
                tick.getDate(),
                0,
                position);
        log.warn("reverse " + position);
    }

    private boolean checkClosePosition(Candle candleDecision, Tick tick) {
        if (candleDecision == null || position == null) {
            return false; //without new decision or position it is not valid
        }
        OrderSide posSide = position.getCandle().getOrderSide();
        OrderSide newSide = candleDecision.getOrderSide();
        if (posSide.isOpen() && newSide.isClose()) {
            log.warn("Close position without execute");
            position = null;
            return true;
        }
        return false;
    }

    private boolean checkReversePosition(Candle candleDecision, Tick tick) {
        if (candleDecision == null || position == null) {
            return false; //without new decision or position it is not valid
        }
        OrderSide posSide = position.getCandle().getOrderSide();
        OrderSide newSide = candleDecision.getOrderSide();

        if (posSide.isLong()) {
            if (posSide.isOpen()) { //current open long
                if (newSide.isLong()) {
                    if (newSide.isOpen()) { //new long open (current open long)
                        throw new IllegalStateException("Trying to open position with same direction | posSide: "
                                + posSide + " newSide: " + newSide);
                    } else { //new long close (current open long)
                        log.warn("Delete open long position");
                        deleteReversePosition();
                        return true;
                    }
                } else { //new short
                    if (newSide.isOpen()) { //new short open (current open long)
                        log.warn("Delete open long order because not in TS, and open short");
                        createNew(candleDecision, tick);
                        return true;
                    } else { //new short close (current open long)
                        throw new IllegalStateException("Trying to close short when long open | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    }
                }
            } else { //current close long
                if (newSide.isLong()) {
                    if (newSide.isOpen()) { //new long open (current close long, open long in TS)
                        log.warn("Delete close long position");
                        deleteReversePosition();
                        return true;
                    } else { //new long close(current close long, open long in TS)
                        throw new IllegalStateException("Trying to add the same position | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    }
                } else { //new short
                    if (newSide.isOpen()) { //new short open (current close long, open long in TS)
                        reverse(candleDecision, tick);
                        return true;
                    } else { //new short close (current close long, open long in TS)
                        throw new IllegalStateException("Trying to close short when long close | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    }
                }
            }
        } else { //current pos is short
            if (posSide.isOpen()) { //current short open
                if (newSide.isLong()) {
                    if (newSide.isOpen()) { //new long open (current short open)
                        log.warn("Delete open short order because not in TS, and open long");
                        createNew(candleDecision, tick);
                        return true;
                    } else { //new long close (current short open)
                        throw new IllegalStateException("Trying to close long when short open | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    }
                } else { //new short
                    if (newSide.isOpen()) { //new short open (current short open)
                        throw new IllegalStateException("Trying to add the same position | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    } else { //new short close (current short open)
                        log.warn("Delete open short position");
                        deleteReversePosition();
                        return true;
                    }
                }
            } else { //current short close
                if (newSide.isLong()) {
                    if (newSide.isOpen()) { //new long open (current short close, short open in TS)
                        reverse(candleDecision, tick);
                        return true;
                    } else { //new long close (current short close, short open in TS)
                        throw new IllegalStateException("Trying to close long when short close | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    }
                } else { //new short
                    if (newSide.isOpen()) { //new short open (current short close, short open in TS)
                        log.warn("Delete open close position");
                        deleteReversePosition();
                        return true;
                    } else { //new short close (current short close, short open in TS)
                        throw new IllegalStateException("Trying to close short when short close | posSide: "
                                + posSide + " newSide: " + newSide);
                        //if it is real case just nothing to do
                    }
                }
            }
        }
    }

    public Position getPosition() {
        return position;
    }

    public void deletePosition() {
        position = null;
    }
}
