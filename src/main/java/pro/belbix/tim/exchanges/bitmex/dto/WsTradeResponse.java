package pro.belbix.tim.exchanges.bitmex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.utils.Common;

import java.io.IOException;
import java.util.List;

@Getter
@Setter
@ToString
@Validated
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WsTradeResponse extends WsResponse {
    private List<TradeResponse> data;

    public static WsTradeResponse fromString(String s) {
        try {
            return Common.MAPPER.readValue(s, WsTradeResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
