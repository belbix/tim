package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorMessage {
    private String message = "";
    private String name = "";
}
