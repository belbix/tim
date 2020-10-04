package pro.belbix.tim.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pro.belbix.tim.properties.HistoryProperties;
import pro.belbix.tim.properties.StrategyProperties;

import javax.persistence.*;

@Entity
@Table(name = "evo_key")
@Cacheable(false)
@Getter
@Setter
@ToString
public class EvoPropKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String strategy;
    private String symbol;
    private String server;
    private String dateStart;
    private String dateEnd;
    private int tfLong;
    private int tfShort;
    private int ticktime;
    private double fee = 0;
    private int leverage = 1;
    private int batch = 100;

    public static EvoPropKey createEvoPropKey(StrategyProperties strategyProp, HistoryProperties historyProp) {
        String strategyName = historyProp.getStrategyName();
        String symbol = strategyProp.getSymbol();
        String server = strategyProp.getServer();
        double fee = historyProp.getFee();
        int leverage = historyProp.getLeverage();
        int batch = historyProp.getBatch();

        EvoPropKey evoPropKey = new EvoPropKey();

        evoPropKey.setStrategy(strategyName);
        evoPropKey.setSymbol(symbol);
        evoPropKey.setServer(server);
        evoPropKey.setFee(fee);
        evoPropKey.setLeverage(leverage);
        evoPropKey.setBatch(batch);
        evoPropKey.setDateStart(historyProp.getDateStart());
        evoPropKey.setDateEnd(historyProp.getDateEnd());
        evoPropKey.setTfLong(strategyProp.getTimeframeLong());
        evoPropKey.setTfShort(strategyProp.getTimeframeShort());
        evoPropKey.setTicktime(historyProp.getTicktime());

        return evoPropKey;
    }
}
