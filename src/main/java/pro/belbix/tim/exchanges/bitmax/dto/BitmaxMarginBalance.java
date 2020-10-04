package pro.belbix.tim.exchanges.bitmax.dto;

import lombok.Getter;
import lombok.Setter;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exchanges.bitmax.BitmaxCommon;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.exchanges.models.Position;

import java.util.Map;

@Getter
@Setter
public class BitmaxMarginBalance implements Balance, Position {

    private static final Map<String, Double> minValue = Map.of("BTC", 0.00130,
            "USDT", 4.0,
            "BTMX", 30.0,
            "PAX", 4.0
    );

    private String assetCode;
    private String totalAmount; //     "0",
    private String availableAmount; // "0",
    private String borrowedAmount; //  "0",       // Amount borrowed
    private String interest; //        "0",       // Interest owed
    private String interestRate; //    "0.0003",  // Interest rate
    private String maxBorrowable; //   "0",       // maximum amount the user can borrow the current asset (in addition to the current borrowed amount)
    private String maxSellable; //     "0",       // maximum amount the user can sell the current asset
    private String maxTransferable; // "0"        // maximum amount the user can transfer to his/her cash account

    @Override
    public Double balance() {
        Double minValue = BitmaxMarginBalance.minValue.get(assetCode);
        if (maxBorrowable != null) {
            double a = Double.parseDouble(maxBorrowable);
            if (a < minValue) return 0d;
            return a;
        }
        double a = Double.parseDouble(availableAmount);
        if (a < minValue) return 0d;
        return a;
    }

    @Override
    public Double currentQty() {
        double amount;
        if (borrowedAmount == null) {
            amount = Double.parseDouble(availableAmount);
        } else {
            amount = Double.parseDouble(borrowedAmount);
        }
        Double minValue = BitmaxMarginBalance.minValue.get(assetCode);
        if (minValue == null) return 0d;

        double dAmount = Math.abs(amount);
        if (dAmount > minValue) {
            if (borrowedAmount == null) {
                return dAmount;
            } else {
                return -dAmount;
            }
        }
        return 0d;
    }

    @Override
    public String symbol() {
        return assetCode;
    }

    @Override
    public String server() {
        return "bitmax";
    }

    @Override
    public boolean compareWithOrder(Order order) {
//        if (!order.getServer().equals(server())) return false;
        String orderSymbol = order.getSymbol();
        String assetSymbol = assetCode;
        orderSymbol = BitmaxCommon.normalizeSymbolPair(orderSymbol, "/");
        assetSymbol = BitmaxCommon.normalizeSymbolPair(assetSymbol, "/");
        if (!orderSymbol.equals(assetSymbol)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "BitmaxMarginBalance{" +
                (assetCode != null ? "assetCode='" + assetCode + '\'' + "," : "") +
                (totalAmount != null ? "totalAmount='" + totalAmount + '\'' + "," : "") +
                (availableAmount != null ? "availableAmount='" + availableAmount + '\'' + "," : "") +
                (borrowedAmount != null ? "borrowedAmount='" + borrowedAmount + '\'' + "," : "") +
                (interest != null ? "interest='" + interest + '\'' + "," : "") +
                (interestRate != null ? "interestRate='" + interestRate + '\'' + "," : "") +
                (maxBorrowable != null ? "maxBorrowable='" + maxBorrowable + '\'' + "," : "") +
                (maxSellable != null ? "maxSellable='" + maxSellable + '\'' + "," : "") +
                (maxTransferable != null ? "maxTransferable='" + maxTransferable + '\'' + "," : "") +
                (balance() != null ? "balance=" + balance() + "," : "") +
                (currentQty() != null ? "currentQty=" + currentQty() + "," : "") +
                (symbol() != null ? "symbol='" + symbol() + '\'' + "," : "") +
                (server() != null ? "server='" + server() + '\'' + "," : "") +
                '}';
    }
}
