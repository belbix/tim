package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class InstrumentResponse implements Response {
    private String symbol;
    private String rootSymbol;
    private String state;
    private String typ;
    private String listing;
    private String front;
    private String expiry;
    private String settle;
    private String relistInterval;
    private String inverseLeg;
    private String sellLeg;
    private String buyLeg;
    private Double optionStrikePcnt;
    private Double optionStrikeRound;
    private Double optionStrikePrice;
    private Double optionMultiplier;
    private String positionCurrency;
    private String underlying;
    private String quoteCurrency;
    private String underlyingSymbol;
    private String reference;
    private String referenceSymbol;
    private String calcInterval;
    private String publishInterval;
    private String publishTime;
    private Double maxOrderQty;
    private Double maxPrice;
    private Double lotSize;
    private Double tickSize;
    private Double multiplier;
    private String settlCurrency;
    private Double underlyingToPositionMultiplier;
    private Double underlyingToSettleMultiplier;
    private Double quoteToSettleMultiplier;
    private Boolean isQuanto;
    private Boolean isInverse;
    private Double initMargin;
    private Double maintMargin;
    private Double riskLimit;
    private Double riskStep;
    private Double limit;
    private Boolean capped;
    private Boolean taxed;
    private Boolean deleverage;
    private Double makerFee;
    private Double takerFee;
    private Double settlementFee;
    private Double insuranceFee;
    private String fundingBaseSymbol;
    private String fundingQuoteSymbol;
    private String fundingPremiumSymbol;
    private String fundingTimestamp;
    private String fundingInterval;
    private Double fundingRate;
    private Double indicativeFundingRate;
    private String rebalanceTimestamp;
    private String rebalanceInterval;
    private String openingTimestamp;
    private String closingTimestamp;
    private String sessionInterval;
    private Double prevClosePrice;
    private Double limitDownPrice;
    private Double limitUpPrice;
    private Double bankruptLimitDownPrice;
    private Double bankruptLimitUpPrice;
    private Double prevTotalVolume;
    private Double totalVolume;
    private Double volume;
    private Double volume24h;
    private Double prevTotalTurnover;
    private Double totalTurnover;
    private Double turnover;
    private Double turnover24h;
    private Double homeNotional24h;
    private Double foreignNotional24h;
    private Double prevPrice24h;
    private Double vwap;
    private Double highPrice;
    private Double lowPrice;
    private Double lastPrice;
    private Double lastPriceProtected;
    private String lastTickDirection;
    private Double lastChangePcnt;
    private Double bidPrice;
    private Double midPrice;
    private Double askPrice;
    private Double impactBidPrice;
    private Double impactMidPrice;
    private Double impactAskPrice;
    private Boolean hasLiquidity;
    private Double openInterest;
    private Double openValue;
    private String fairMethod;
    private Double fairBasisRate;
    private Double fairBasis;
    private Double fairPrice;
    private String markMethod;
    private Double markPrice;
    private Double indicativeTaxRate;
    private Double indicativeSettlePrice;
    private Double optionUnderlyingPrice;
    private Double settledPrice;
    private String timestamp;
}
