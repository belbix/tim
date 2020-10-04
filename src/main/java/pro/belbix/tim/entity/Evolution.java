package pro.belbix.tim.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.history.Common;
import pro.belbix.tim.history.HistoryProcessor;
import pro.belbix.tim.properties.HistoryProperties;
import pro.belbix.tim.properties.MutableProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.strategies.Strategy;
import pro.belbix.tim.strategies.common.TradeStatusBuilder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "evolute_properties", indexes = {
        @Index(name = "idx_evo_prop", columnList = "id, created, result, evo_prop_key_id, station")
})
@Cacheable(false)
@Getter
@Setter
@ToString
public class Evolution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime created;
    private Double result;
    @Column(columnDefinition = "json")
    private String properties;
    @Column(columnDefinition = "json")
    private String orderProperties;
    @Column(columnDefinition = "json")
    private String strategyProperties;
    private int count = 0;
    private int countLong = 0;
    private int countShort = 0;
    private double rate = 0;
    private double maxLose = 0;
    private double maxEff = 0;
    @Column(columnDefinition = "longblob")
    private String history;
    private Long prevId;
    private String station;

    @ManyToOne(fetch = FetchType.EAGER)
    private EvoPropKey evoPropKey;

    public static Evolution createEvoProp(Strategy strategy,
                                          StrategyProperties strategyProperties,
                                          TradeStatusBuilder tradeStatusBuilder,
                                          HistoryProperties historyProperties,
                                          MutableProperties orderProperties,
                                          HistoryProcessor historyProcessor,
                                          EvoPropKey evoPropKey,
                                          long prevId) {

        double result = tradeStatusBuilder.getEffective();
        Evolution evolution = new Evolution();
        evolution.setEvoPropKey(evoPropKey);

        evolution.setProperties(mapToStr(Common.propToMap(strategy.getStrategyPropertiesI())));
        evolution.setOrderProperties(mapToStr(Common.propToMap(orderProperties)));
        evolution.setStrategyProperties(mapToStr(Common.propToMap(strategyProperties)));
        evolution.setResult(result);

        evolution.setCountLong(tradeStatusBuilder.countOfLong());
        evolution.setCountShort(tradeStatusBuilder.countOfShort());
        evolution.setRate(tradeStatusBuilder.rate());

        evolution.setCreated(LocalDateTime.now());
        evolution.setCount(tradeStatusBuilder.getCount());
//        evolution.setHistory(historyProcessor.createResult());
        evolution.setMaxLose(tradeStatusBuilder.maxLose());
        evolution.setMaxEff(tradeStatusBuilder.maxEff());
        evolution.setResult(historyProcessor.calcFinalEff(tradeStatusBuilder));
        evolution.setPrevId(prevId);
        evolution.setStation(historyProperties.getStation());
        return evolution;
    }

    public static String mapToStr(Map<String, Object> propMap) {
        String propStr = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            propStr = mapper.writeValueAsString(propMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return propStr;
    }

    public void merge(Evolution e) {

    }

}
