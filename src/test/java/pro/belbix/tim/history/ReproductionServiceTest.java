package pro.belbix.tim.history;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pro.belbix.tim.history.ReproductionService.calcChance;
import static pro.belbix.tim.history.ReproductionService.isChance;

public class ReproductionServiceTest {

    @Test
    public void testCalcChance() {
        assertEquals("chance", 50.0, calcChance(0), 0.0);
        assertEquals("chance", 45.0, calcChance(10), 0.0);
        assertEquals("chance", 35.0, calcChance(30), 0.0);
        assertEquals("chance", 20.0, calcChance(60), 0.0);
        assertEquals("chance", 0.5, calcChance(120), 0.0);
        assertEquals("chance", 0.5, calcChance(240), 0.0);
        assertEquals("chance", 0.5, calcChance(500), 0.0);
        assertEquals("chance", 0.5, calcChance(1000), 0.0);
        assertEquals("chance", 0.5, calcChance(10000), 0.0);
    }

    //    @Test
    public void testIsChance() {
        assertTrue("chance", isChance(0.5));
    }
}
