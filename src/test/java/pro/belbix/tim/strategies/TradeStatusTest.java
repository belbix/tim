package pro.belbix.tim.strategies;

import org.junit.Test;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.models.OrderSide;
import pro.belbix.tim.strategies.common.Trade;
import pro.belbix.tim.strategies.common.TradeStatus;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class TradeStatusTest {

    @Test
    public void writeTradeStatusInFile() {
        Candle candle = new Candle();
        candle.setDate(LocalDateTime.now());
        candle.setClose(0d);
        List<TradeStatus> tradeStatuses = new ArrayList<>();
        TradeStatus tradeStatus = new TradeStatus();
        tradeStatus.setOpen(false);
        tradeStatus.setTrade(Trade.fromCandle(candle));
        tradeStatus.setOrderSide(OrderSide.LONG_OPEN);
        tradeStatuses.add(tradeStatus);
        String fileName = "histories/hist_test.txt";
        TradeStatus.writeTradeStatusInFile(null, tradeStatuses, fileName);
        new File(fileName).delete();
    }
}
