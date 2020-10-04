package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class WalletResponse implements Response {
    private double account;
    private String currency;
    private Double prevDeposited;
    private Double prevWithdrawn;
    private Double prevTransferIn;
    private Double prevTransferOut;
    private Double prevAmount;
    private String prevTimestamp;
    private Double deltaDeposited;
    private Double deltaWithdrawn;
    private Double deltaTransferIn;
    private Double deltaTransferOut;
    private Double deltaAmount;
    private Double deposited;
    private Double withdrawn;
    private Double transferIn;
    private Double transferOut;
    private Double amount;
    private Double pendingCredit;
    private Double pendingDebit;
    private Double confirmedDebit;
    private String timestamp;
    private String addr;
    private String script;
    private String[] withdrawalLock;
}
