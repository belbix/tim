package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.rest.Response;

@Getter
@Setter
@ToString
public class WalletBalance implements Response {
    private Double amount = 0d;
}
