package pro.belbix.tim.properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import pro.belbix.tim.protobuf.neuron.Nsrsi;

@Configuration
@Validated
@ConfigurationProperties(prefix = "tim.strategies.nsrsi")
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class NsrsiProperties implements StrategyPropertiesI {
    //COMMON
    private boolean useMarketOrders = false;
    private boolean useLong = true;
    private boolean useShort = true;
    private boolean computeSrsi = true;
    private boolean printProcessing = true;

    private String model = null;


    public Nsrsi toNsrsi() {
        try {
            return Nsrsi.parseFrom(Hex.decodeHex(getModel()));
        } catch (InvalidProtocolBufferException | DecoderException e) {
            System.out.println("Error decode srsi" + e.getMessage());
            return null;
        }
    }
}
