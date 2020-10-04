package pro.belbix.tim.exchanges.bitmex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.utils.Common;

import java.io.IOException;

@Getter
@Setter
@ToString
@Validated
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WsResponse {
    private String info;
    private String success;
    private String subscribe;
    private String table;
    private String action;
    private String error;

    public static WsResponse fromString(String s) {
        try {
            return Common.MAPPER.readValue(s, WsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
