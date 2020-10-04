package pro.belbix.tim.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pro.belbix.tim.entity.Srsi2Tick;
import pro.belbix.tim.entity.SrsiTickI;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyntheticDecision {
    private LocalDateTime from;
    private LocalDateTime to;
    private double profit;
    private int countOfTicks;
    private Srsi2Tick srsiOpen;
    private Srsi2Tick srsiClose;


    public static SyntheticDecision fromRange(int[] range, List<SrsiTickI> srsi2Ticks, double[] arr) {
        SyntheticDecision d = new SyntheticDecision();
        double profit = 0;
        for (int i = range[0]; i <= range[1]; i++) {
            profit += arr[i];
        }
        d.setProfit(profit);

        SrsiTickI s1 = srsi2Ticks.get(range[0]);
        SrsiTickI s2 = srsi2Ticks.get(range[1]);
        d.setFrom(s1.getDate());
        d.setTo(s2.getDate());
        d.setCountOfTicks(range[1] - range[0]);
        if (s1 instanceof Srsi2Tick) {
            d.setSrsiOpen((Srsi2Tick) s1);
            d.setSrsiClose((Srsi2Tick) s2);
        }
        return d;
    }
}
