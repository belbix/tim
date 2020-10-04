package pro.belbix.tim.exchanges.bitmex.dto;

import pro.belbix.tim.utils.Common;

import java.io.IOException;
import java.util.List;

public class WsPositionResponse extends WsResponse {
    private List<PositionResponse> data;

    public static WsPositionResponse fromString(String s) {
        try {
            return Common.MAPPER.readValue(s, WsPositionResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
