package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.entity.Order;
import pro.belbix.tim.exchanges.models.Position;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class PositionResponse implements Response, Position {
    private Double account;
    private String symbol;
    private String currency;
    private String underlying;
    private String quoteCurrency;
    private Double commission;
    private Double initMarginReq;
    private Double maintMarginReq;
    private Double riskLimit;
    private Double leverage;
    private Boolean crossMargin;
    private Double deleveragePercentile;
    private Double rebalancedPnl;
    private Double prevRealisedPnl;
    private Double prevUnrealisedPnl;
    private Double prevClosePrice;
    private String openingTimestamp;
    private Double openingQty;
    private Double openingCost;
    private Double openingComm;
    private Double openOrderBuyQty;
    private Double openOrderBuyCost;
    private Double openOrderBuyPremium;
    private Double openOrderSellQty;
    private Double openOrderSellCost;
    private Double openOrderSellPremium;
    private Double execBuyQty;
    private Double execBuyCost;
    private Double execSellQty;
    private Double execSellCost;
    private Double execQty;
    private Double execCost;
    private Double execComm;
    private String currentTimestamp;
    private Integer currentQty;
    private Double currentCost;
    private Double currentComm;
    private Double realisedCost;
    private Double unrealisedCost;
    private Double grossOpenCost;
    private Double grossOpenPremium;
    private Double grossExecCost;
    private Boolean isOpen;
    private Double markPrice;
    private Double markValue;
    private Double riskValue;
    private Double homeNotional;
    private Double foreignNotional;
    private String posState;
    private Double posCost;
    private Double posCost2;
    private Double posCross;
    private Double posInit;
    private Double posComm;
    private Double posLoss;
    private Double posMargin;
    private Double posMaint;
    private Double posAllowance;
    private Double taxableMargin;
    private Double initMargin;
    private Double maintMargin;
    private Double sessionMargin;
    private Double targetExcessMargin;
    private Double varMargin;
    private Double realisedGrossPnl;
    private Double realisedTax;
    private Double realisedPnl;
    private Double unrealisedGrossPnl;
    private Double longBankrupt;
    private Double shortBankrupt;
    private Double taxBase;
    private Double indicativeTaxRate;
    private Double indicativeTax;
    private Double unrealisedTax;
    private Double unrealisedPnl;
    private Double unrealisedPnlPcnt;
    private Double unrealisedRoePcnt;
    private Double simpleQty;
    private Double simpleCost;
    private Double simpleValue;
    private Double simplePnl;
    private Double simplePnlPcnt;
    private Double avgCostPrice;
    private Double avgEntryPrice;
    private Double breakEvenPrice;
    private Double marginCallPrice;
    private Double liquidationPrice;
    private Double bankruptPrice;
    private String timestamp;
    private Double lastPrice;
    private Double lastValue;

    @Override
    public Double currentQty() {
        if (currentQty != 0) {
            return currentQty.doubleValue();
        } else {
            return 0d; //TODO find another value
        }
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public String server() {
        return "bitmex";
    }

    @Override
    public boolean compareWithOrder(Order order) {
//        if (!order.getServer().equals(server())) return false;
        if (!order.getSymbol().equals(symbol)) return false;
        return true;
    }
}
