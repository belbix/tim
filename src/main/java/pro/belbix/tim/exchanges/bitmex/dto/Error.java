package pro.belbix.tim.exchanges.bitmex.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Error {
    private ErrorMessage error = new ErrorMessage();

    @Override
    public String toString() {
        return error.getMessage();
    }
}
