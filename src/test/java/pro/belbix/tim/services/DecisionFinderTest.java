package pro.belbix.tim.services;

import pro.belbix.tim.models.SyntheticDecision;

import java.time.LocalDateTime;
import java.util.List;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SimpleApp.class)
public class DecisionFinderTest {
    //    @Autowired
    private DecisionFinder decisionFinder;

    //    @Test
    public void findDecisions() {
        List<SyntheticDecision> result = decisionFinder.findDecisions(
                LocalDateTime.parse("2019-04-01T00:00:00")
                , LocalDateTime.parse("2019-12-01T00:00:00")
                , 1.1
        );

        for (SyntheticDecision decision : result) {
            if (decision.getProfit() < 500) continue;
            System.out.println(decision);
        }
    }
}
