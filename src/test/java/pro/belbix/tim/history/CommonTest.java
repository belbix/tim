package pro.belbix.tim.history;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Evolution;
import pro.belbix.tim.properties.MutableProperties;
import pro.belbix.tim.properties.Srsi2Properties;
import pro.belbix.tim.properties.SrsiProperties;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class CommonTest {
    @Autowired
    SrsiProperties srsiProperties;
    @Autowired
    Srsi2Properties srsi2Properties;

    @Test
    public void propToMapSrsi() {
        Map<String, Object> map = Common.propToMap(srsiProperties);
        Assert.assertTrue(map.size() != 0);
    }

    @Test
    public void propToMapSrsi2() {
        Map<String, Object> map = Common.propToMap(srsi2Properties);
        Assert.assertTrue(map.size() != 0);
    }

    @Test
    public void evaluateSrsi() {
        Map<String, Object> map = Common.propToMap(srsiProperties);
        Assert.assertTrue(map.size() != 0);
        String str = Evolution.mapToStr(map);
        MutableProperties evoProp = EvolutionHistory.jsonToProperty(str, srsiProperties);
        Common.evoluteMutate(evoProp, srsiProperties, 10);
    }

    @Test
    public void evaluateSrsi2() {
        Map<String, Object> map = Common.propToMap(srsi2Properties);
        Assert.assertTrue(map.size() != 0);
        String str = Evolution.mapToStr(map);
        MutableProperties evoProp = EvolutionHistory.jsonToProperty(str, srsi2Properties);
        Common.evoluteMutate(evoProp, srsiProperties, 10);
    }
}
