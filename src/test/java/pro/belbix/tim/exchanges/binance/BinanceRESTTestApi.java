package pro.belbix.tim.exchanges.binance;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.exceptions.TIMRetryException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class BinanceRESTTestApi {
    @Autowired
    private BinanceREST binanceREST;

    //    @Test
    public void getBalanceTest() throws TIMRetryException {
        binanceREST.getBalance("");
    }
}
