package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import pro.belbix.tim.properties.EmailProperties;
import pro.belbix.tim.strategies.common.TradeStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int MAX_ATTEMPTS = 3;
    private static final int ATTEMPT_DELAY = 1000;
    private final JavaMailSender mailSender;
    private final EmailProperties prop;

    @Autowired
    public EmailService(JavaMailSender mailSender, EmailProperties prop) {
        this.mailSender = mailSender;
        this.prop = prop;
    }

    public void sendEmail(String to, String subject, String text) {
        if (!prop.isEnable()) return;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error send email:" + e.getMessage());
            throw e;
        }

    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public void sendTradeStatus(TradeStatus tradeStatus) {
        String sbj = tradeStatus.getTrade().getCandle().getOrderSide().name();
        sbj += " " + tradeStatus.getTrade().getCandle().getServer();
        sbj += " " + tradeStatus.getTrade().getCandle().getSymbol();
        sbj += " Blnc: " + tradeStatus.getDeposit();
        sbj += " Amount: " + tradeStatus.getAmountForBuy();
        sbj += " Price: " + tradeStatus.getClosePrice();
        sendEmail(prop.getTo(), sbj, tradeStatus.toString());
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = MAX_ATTEMPTS,
            backoff = @Backoff(delay = ATTEMPT_DELAY))
    public void sendError(String sbj, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString();
        sendEmail(prop.getTo(), sbj, e.getMessage() + "\n" + sStackTrace);
    }
}
