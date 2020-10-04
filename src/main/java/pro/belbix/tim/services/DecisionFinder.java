package pro.belbix.tim.services;

import org.springframework.stereotype.Service;
import pro.belbix.tim.entity.SrsiTickI;
import pro.belbix.tim.models.SyntheticDecision;
import pro.belbix.tim.repositories.Srsi2TickRepository;
import pro.belbix.tim.utils.SubArraysUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DecisionFinder {

    private final Srsi2TickRepository srsi2TickRepository;

    public DecisionFinder(Srsi2TickRepository srsi2TickRepository) {
        this.srsi2TickRepository = srsi2TickRepository;
    }

    public List<SyntheticDecision> findDecisions(LocalDateTime from, LocalDateTime end, double factor) {
        List<SrsiTickI> srsi2Ticks = srsi2TickRepository.load(from, end);
        double[] arr = new double[srsi2Ticks.size()];
        for (int i = 1; i < arr.length; i++) {
            arr[i] = srsi2Ticks.get(i).getFirstClose() - srsi2Ticks.get(i - 1).getFirstClose();
        }

        List<int[]> ranges = SubArraysUtil.findMaxSubArrays(arr, factor);
        List<SyntheticDecision> decisions = new ArrayList<>(ranges.size());
        for (int[] r : ranges) {
            decisions.add(SyntheticDecision.fromRange(r, srsi2Ticks, arr));
        }
        return decisions;
    }

}
