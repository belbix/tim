package pro.belbix.tim.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.repositories.TickRepository;
import pro.belbix.tim.validators.TickMother;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class JpaServiceTest {

    @Autowired
    private JpaService jpaService;

    @Autowired
    private TickRepository tickRepository;

    @Test
    public void batchNativeTickSave() {
        List<Tick> ticks = buildTicks();
        int count = jpaService.batchNativeTickSave(ticks);

        System.out.println("Count: " + count);

        List<String> ids = new ArrayList<>();
        for (Tick tick : ticks) {
            ids.add(tick.getStrId());
        }

        List<Tick> savedTicks = tickRepository.findAllById(ids);
        Assert.assertNotNull(savedTicks);
        Assert.assertEquals(savedTicks.size(), count);
        for (Tick tick : savedTicks) {
            System.out.println("Saved tick: " + tick);
            tickRepository.delete(tick);
        }
    }

    @Test
    public void nativeQueryFromTicks() {
        List<Tick> ticks = buildTicks();
        String r = jpaService.nativeQueryFromTicks(ticks);
        Assert.assertNotNull(r);
        System.out.println(r.length());
        System.out.println(r);
    }

    private List<Tick> buildTicks() {
        return TickMother.buildTicksForCandle(
                LocalDateTime.now().minus(10, ChronoUnit.MINUTES),
                LocalDateTime.now(),
                60);
    }
}
