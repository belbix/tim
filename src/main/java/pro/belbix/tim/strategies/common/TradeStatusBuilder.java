package pro.belbix.tim.strategies.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.models.OrderSide;
import pro.belbix.tim.strategies.Strategy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static pro.belbix.tim.models.OrderSide.LONG_CLOSE;
import static pro.belbix.tim.models.OrderSide.SHORT_CLOSE;


public class TradeStatusBuilder {
    public static final int MAX_CANDLE_PER_TRADE = Integer.MAX_VALUE;
    static final Logger log = LoggerFactory.getLogger(Strategy.class);
    private final static int RESULT_MINUS_MULTIPLIER = 1;
    private final double fee;
    private final int leverage;
    private Trade lastTrade = null;
    private double deposit;
    private double effective = 0;
    private List<Double> results = new ArrayList<>();
    private List<TradeStatus> tradeStatuses = new ArrayList<>();

    public TradeStatusBuilder(double deposit, double fee, int leverage) {
        this.deposit = deposit;
        this.fee = fee;
        this.leverage = leverage;
    }

    public TradeStatus buildTrade(Candle candle) {
        if (candle == null || candle.getOrderSide() == null) {
            throw new TIMRuntimeException("Candle is null or invalid");
        }

        TradeStatus ts;

        if (!candle.getOrderSide().isClose()) {
            ts = openPosition(candle);
        } else {
            if (lastTrade == null) {
                throw new TIMRuntimeException("Last trade not set, " + candle);
            }
            ts = closePosition(candle);
            tradeStatuses.add(ts);
            double eff = calcLastResult(ts);
            results.add(eff);
            ts.setEff(eff);
            effective = effectiveResult();
        }
        return ts;
    }

    public boolean isOpen() {
        if (lastTrade == null) return false;
        return lastTrade.getCandle().getOrderSide().isOpen();
    }

    private TradeStatus openPosition(Candle candle) {
        if (lastTrade != null && !lastTrade.getCandle().getOrderSide().isClose()) {
            throw new TIMRuntimeException("Last trade not closed: " + lastTrade);
        }
        lastTrade = Trade.fromCandle(candle);

        TradeStatus tradeStatus = new TradeStatus();
        tradeStatus.setOpen(true);
        tradeStatus.setTrade(lastTrade);
        tradeStatus.setDeposit(deposit);
        tradeStatus.setAmountForBuy(deposit * leverage);
        tradeStatus.setClosePrice(candle.getClose());
        return tradeStatus;
    }

    private TradeStatus closePosition(Candle candle) {
        if (lastTrade.getCandle().getOrderSide().isClose()) {
            throw new TIMRuntimeException("Last trade is closed: " + lastTrade);
        }
        if (!candle.getOrderSide().isTheSameDirection(lastTrade.getCandle().getOrderSide())) {
            throw new TIMRuntimeException("Last trade is different closeDecisionTrade type: " + lastTrade.getCandle().getOrderSide()
                    + " candle type: " + candle.getOrderSide());
        }
        LocalDateTime openDate = lastTrade.getCandle().tickTime();
        double amountForBuy = deposit * leverage; //USD
        double openPrice = lastTrade.getCandle().getClose();  //BTC
        double bought = amountForBuy / openPrice;  //COUNT
        double openFee = amountForBuy * (fee / 100); //USD
        double closePrice = candle.getClose();  //BTC
        double amountAfterSold = bought * closePrice; //USD
        double closeFee = amountAfterSold * (fee / 100); //USD
        double fees = openFee + closeFee; //USD
        double profit = calcProfit(openPrice, closePrice, candle.getOrderSide(), true);

        deposit += profit;
        lastTrade = Trade.fromCandle(candle);

        TradeStatus tradeStatus = new TradeStatus();
        tradeStatus.setOpen(false);
        tradeStatus.setTrade(lastTrade);
        tradeStatus.setDeposit(deposit);
        tradeStatus.setAmountForBuy(amountForBuy);
        tradeStatus.setOpenPrice(openPrice);
        tradeStatus.setBought(bought);
        tradeStatus.setOpenFee(openFee);
        tradeStatus.setClosePrice(closePrice);
        tradeStatus.setAmountAfterSold(amountAfterSold);
        tradeStatus.setCloseFee(closeFee);
        tradeStatus.setFees(fees);
        tradeStatus.setFee(fee);
        tradeStatus.setProfit(profit);
        tradeStatus.setOrderSide(candle.getOrderSide());
        tradeStatus.setOpenDate(openDate);
        tradeStatus.setCloseDate(candle.tickTime());
        tradeStatus.setTimeframe(candle.getTime());
        return tradeStatus;
    }

    public double calcProfit(double openPrice, double closePrice, OrderSide orderSide, boolean withLeverage) {
        double amountForBuy;
        if (withLeverage) {
            amountForBuy = deposit * leverage; //USD
        } else {
            amountForBuy = deposit; //USD
        }
        double bought = amountForBuy / openPrice;  //COUNT
        double openFee = amountForBuy * (fee / 100); //USD
        double amountAfterSold = bought * closePrice; //USD
        double closeFee = amountAfterSold * (fee / 100); //USD
        double fees = openFee + closeFee; //USD
        double profit;
        if (orderSide.equals(LONG_CLOSE)) {
            profit = amountAfterSold - amountForBuy;
        } else if (orderSide.equals(SHORT_CLOSE)) {
            profit = amountForBuy - amountAfterSold;
        } else {
            throw new TIMRuntimeException("OrderSide not found " + orderSide);
        }
        profit -= fees;
        return profit;
    }

    private double effectiveResult() {
        double r = 0;
        for (double d : results) {
            if (d < 0) r += d * RESULT_MINUS_MULTIPLIER;
            else r += d;
        }
        return r;
    }

    public double getEffective() {
        return effective;
    }

    public int getCount() {
        return results.size();
    }

    public double maxLose() {
        return results.stream()
                .mapToDouble(a -> a)
                .min().orElse(0);
    }

    public double maxEff() {
        return results.stream()
                .mapToDouble(a -> a)
                .max().orElse(0);
    }

    private double calcLastResult(TradeStatus ts) {
        Duration d = Duration.between(ts.getOpenDate(), ts.getCloseDate());
        int countOfCandle = 0;
        if (ts.getTimeframe().longValue() != 0) {
            countOfCandle = Math.round(d.toMinutes() / ts.getTimeframe().longValue());
        }
        double r = ts.calcResult();
        if (countOfCandle > MAX_CANDLE_PER_TRADE && r > 0) {
            log.error("Too much candle count for calculate result: " + countOfCandle);
            return 0;
        }
        return r;
    }

    public int countOfLong() {
        int c = 0;
        for (TradeStatus tradeStatus : tradeStatuses) {
            if (tradeStatus.getOrderSide().isLong()) {
                c++;
            }
        }
        return c;
    }

    public int countOfShort() {
        int c = 0;
        for (TradeStatus tradeStatus : tradeStatuses) {
            if (tradeStatus.getOrderSide().isShort()) {
                c++;
            }
        }
        return c;
    }

    public double rate() {
        if (tradeStatuses.isEmpty()) {
            return 0;
        }
        int c = 0;
        for (TradeStatus tradeStatus : tradeStatuses) {
            if (tradeStatus.getEff() > 0) {
                c++;
            }
        }
        return (double) (100 * c) / (double) tradeStatuses.size();
    }

    public double getDeposit() {
        return deposit;
    }

    public double getFee() {
        return fee;
    }

    public List<TradeStatus> getTradeStatuses() {
        return tradeStatuses;
    }

    @Override
    public String toString() {
        String msg = "";
        msg += String.format("d:%.1f", deposit) + " | ";
        msg += String.format("eff:%.1f", effective);
        if (lastTrade != null && lastTrade.getCandle() != null) {
            msg += " | " + lastTrade.getCandle().getOrderSide().name();
            msg += " " + lastTrade.getCandle().getClose();
//            msg += " " + closeDecisionTrade.getCreateDate();
        }
        return msg;
    }
}
