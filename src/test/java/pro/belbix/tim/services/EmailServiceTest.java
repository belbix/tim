package pro.belbix.tim.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.properties.EmailProperties;
import pro.belbix.tim.strategies.common.Trade;
import pro.belbix.tim.strategies.common.TradeStatus;

import java.time.LocalDateTime;

import static pro.belbix.tim.models.OrderSide.LONG_OPEN;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String selfAdr;

    @Autowired
    private EmailProperties emailProperties;

    @Test
    public void sendEmail() {
        emailService.sendEmail(selfAdr, "test sbj", "test text");
    }

    @Test
    public void sendTradeStatus() {
        emailProperties.setTo(selfAdr);
        Candle candle = new Candle();
        candle.setDate(LocalDateTime.now());
        candle.setClose(0d);
        candle.setOrderSide(LONG_OPEN);

        TradeStatus tradeStatus = new TradeStatus();
        tradeStatus.setOpen(false);
        tradeStatus.setTrade(Trade.fromCandle(candle));

        emailService.sendTradeStatus(tradeStatus);
    }
}
