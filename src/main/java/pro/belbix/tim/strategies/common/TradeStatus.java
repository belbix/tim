package pro.belbix.tim.strategies.common;

import lombok.Getter;
import lombok.Setter;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.models.OrderSide;
import pro.belbix.tim.strategies.Strategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

import static pro.belbix.tim.models.OrderSide.LONG_CLOSE;
import static pro.belbix.tim.models.OrderSide.SHORT_CLOSE;

@Getter
@Setter
public class TradeStatus {
    private Trade trade;
    private Boolean open;
    private Double deposit;
    private Double amountForBuy;
    private Double openPrice;
    private Double bought;
    private Double openFee;
    private Double closePrice;
    private Double amountAfterSold;
    private Double closeFee;
    private Double fees;
    private Double fee;
    private Double profit;
    private OrderSide orderSide;
    private LocalDateTime openDate;
    private LocalDateTime closeDate;
    private Integer timeframe;
    private Double eff;

    public static void writeTradeStatusInFile(Strategy strategy, List<TradeStatus> tradeStatuses, String fileName) {
        File f = new File(fileName);
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("File " + fileName + ": " + e.getMessage());
            return;
        }
        try (FileWriter fileWriter = new FileWriter(f)) {
            PrintWriter printWriter = new PrintWriter(fileWriter);
            if (strategy != null) {
                printWriter.println(strategy.getStrategyProperties().toString());
                printWriter.println(strategy.getStrategyPropertiesI().toString());
            }
            printWriter.println();
            printWriter.println(TradeStatus.csvHeaders());
            for (TradeStatus ts : tradeStatuses) {
                if (ts.getOpen() == null || ts.getOpen()) continue;
                String csv = ts.toCsvString();
                printWriter.println(csv);

            }
        } catch (IOException e) {
            System.out.println("File " + fileName + ": " + e.getMessage());
        }
    }

    public static String csvHeaders() {
        return "date;"
                + "price;"
                + "deposit;"
                + "profit;"
                + "eff;"
                + "type;"
                ;
    }

    public double calcResult() {
        double amountForBuy = 1000; //USD
        double bought = amountForBuy / openPrice;  //INSTRUMENT
        double openFee = amountForBuy * (fee / 100); //USD
        double amountAfterSold = bought * closePrice; //USD
        double closeFee = amountAfterSold * (fee / 100); //USD
        double fees = openFee + closeFee; //USD
        double profit; //USD
        if (orderSide.equals(LONG_CLOSE)) {
            profit = amountAfterSold - amountForBuy;
        } else if (orderSide.equals(SHORT_CLOSE)) {
            profit = amountForBuy - amountAfterSold;
        } else {
            throw new TIMRuntimeException("OrderSide not found " + orderSide);
        }
        profit -= fees;
        return (profit / amountForBuy) * 100;
    }

    @Override
    public String toString() {
        if (open) {
            return "Open position. Deposit: " + deposit;
        } else {
            return "Close position with profit: " + profit
                    + " with fees: " + fees
                    + ". Deposit: " + deposit;
        }
    }

    public String toCsvString() {
        if (trade == null || trade.getCandle() == null || trade.getCandle().getDate() == null
                || trade.getCandle().getClose() == null)
            return "";
        return
                trade.getCandle().getDate() + ";" +
                        trade.getCandle().getClose() + ";" +
                        deposit + ";" +
                        profit + ";" +
                        eff + ";" +
                        orderSide.isLong() + ";";
    }
}
