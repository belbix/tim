package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.exchanges.models.Balance;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class WalletHistoryResponse implements Response, Balance {
    private String transactID;
    private Double account;
    private String currency;
    private String transactType;
    private Double amount;
    private Double fee;
    private String transactStatus;
    private String address;
    private String tx;
    private String text;
    private String transactTime;
    private String timestamp;

    private Double walletBalance;


    @Override
    public String symbol() {
        return currency;
    }

    @Override
    public Double balance() {
        return walletBalance;
    }
}
