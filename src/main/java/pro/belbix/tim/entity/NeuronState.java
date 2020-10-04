package pro.belbix.tim.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.history.Common;
import pro.belbix.tim.properties.MutableProperties;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "neuron_states")
@Cacheable(false)
@Getter
@Setter
@ToString
public class NeuronState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private LocalDateTime created;
    @Column(columnDefinition = "json")
    private String state;
    private Double result;
    private String strategy;
    private String symbol;
    private double fee = 0;
    private int leverage = 1;
    private Integer windowProp;
    private Double compressionProp;
    private Integer countOfSublayer;
    @Column(columnDefinition = "json")
    private String props;
    private Integer timeframe;
    private Double result2;

    public static NeuronState create(MutableProperties mutableProperties,
                                     double result,
                                     String strategyName,
                                     String symbol,
                                     double fee,
                                     int leverage
    ) {
        NeuronState neuronState = new NeuronState();

        ObjectMapper mapper = new ObjectMapper();
        String propStr = null;
        try {
            Map<String, Object> propMap = Common.propToMap(mutableProperties);
            propStr = mapper.writeValueAsString(propMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        neuronState.setProps(propStr);
        neuronState.setResult(result);
        neuronState.setStrategy(strategyName);
        neuronState.setSymbol(symbol);
        neuronState.setFee(fee);
        neuronState.setLeverage(leverage);
        return neuronState;
    }
}
