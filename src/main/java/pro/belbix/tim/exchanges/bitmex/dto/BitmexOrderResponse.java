package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
@Validated
public class BitmexOrderResponse implements Response {
    private String orderID;
    private String clOrdID;
    private String clOrdLinkID;
    private String account;
    private String symbol;
    private String side;
    private Double simpleOrderQty;
    private Double orderQty;
    private Double price;
    private Double displayQty;
    private Double stopPx;
    private Double pegOffsetValue;
    private String pegPriceType;
    private String currency;
    private String settlCurrency;
    private String ordType;
    private String timeInForce;
    private String execInst;
    private String contingencyType;
    private String exDestination;
    private String ordStatus;
    private String triggered;
    private Boolean workingIndicator;
    private String ordRejReason;
    private Double simpleLeavesQty;
    private Double leavesQty;
    private Double simpleCumQty;
    private Double cumQty;
    private Double avgPx;
    private String multiLegReportingType;
    private String text;
    private String transactTime;
    private String timestamp;
}
