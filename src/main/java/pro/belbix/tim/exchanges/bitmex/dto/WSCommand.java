package pro.belbix.tim.exchanges.bitmex.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.utils.Common;

import java.util.List;

@Getter
@Setter
@ToString
@Validated
@NoArgsConstructor
public class WSCommand {
    private String op;
    private List<String> args;

    public String toJson() {
        try {
            return Common.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
