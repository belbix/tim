package pro.belbix.tim.strategies;

import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.protobuf.srsi.SrsiNode;

import java.util.List;

public class SrsiDecisionMaker {

    public static boolean make(List<Candle> candles, List<SrsiNode> nodes) {
        for (SrsiNode node : nodes) {

            Candle currentCandle = candles.get(node.getIndex());

            if (currentCandle.calcDeltaK() < node.getSrsiDiffMin().getValue()) return false;
            if (currentCandle.calcDeltaK() > node.getSrsiDiffMax().getValue()) return false;

            if (currentCandle.getSlowk() < node.getSrsiMin().getValue()) return false;
            if (currentCandle.getSlowk() > node.getSrsiMax().getValue()) return false;

            if (node.getIndex() + 1 < candles.size()) {
                Candle prevCandle = candles.get(node.getIndex() + 1);
                double priceDiff = currentCandle.calcDiffWithPrev(prevCandle);
                if (priceDiff > node.getPriceDiffMax().getValue()) return false;
                if (priceDiff < node.getPriceDiffMin().getValue()) return false;
            }
        }
        return true;
    }
}
