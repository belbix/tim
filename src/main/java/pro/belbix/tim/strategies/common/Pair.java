package pro.belbix.tim.strategies.common;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.exchanges.bitmax.BitmaxCommon;

@Getter
@Setter
@ToString
public class Pair {
    private String symbol;

    private String firstSymbol;
    private String secondSymbol;

    private Double highestPrice;
    private Double lowestPrice;

    private Double balanceFirst;
    private Double balanceSecond;


    public Pair(String symbol) {
        this.symbol = BitmaxCommon.normalizeSymbolPair(symbol, "-");
        firstSymbol = this.symbol.split("-")[0];
        secondSymbol = this.symbol.split("-")[1];
    }

    public String report() {
        String msg = "";

        msg += symbol;
        if (balanceFirst != 0) msg += String.format(":%.8f", balanceFirst);
        else msg += ":0";
        if (getBalanceSecond() != 0) msg += String.format("/%.8f", getBalanceSecond());
        else msg += "/0";
        msg += String.format(" > %.8f", getHighestPrice());
        return msg;
    }
}
