package pro.belbix.tim;

import java.util.Set;

public class PropReader {
    private static final Set<String> excluded = Set.of(
            "useMarketOrders",
            "computeSrsi",
            "printProcessing"
    );

    public static void main(String[] args) {
        String p = "SrsiProperties(useMarketOrders=false, useLong=true, useShort=true, computeSrsi=false, printProcessing=false, oldRowNumber=2, secondRowNumber=1, firstRowNumber=0, rsiDeltaSecondOpen=1.9619261983764589, priceDiffLongPerc=-1.1231112181908167, priceDiffShortPerc=18.542848631389596, rsiDeltaFirstOpen=1.3108251637178663, kFirstMin=100.0, kmax=8.21889801841838, openPositionBigPriceChange=9999.0, rsiDeltaOldOpenLong=-2.077578344717161, rsiDeltaFirstCloseLong=-19.832062628498072, rsiDeltaFirstCloseLongAfterPump=-2.0, rsiDeltaOldOpenShort=-2.6829655715266703, rsiDeltaFirstCloseShort=11.797882244109907, rsiDeltaFirstCloseShortAfterDump=-2.0)";
        p = p.replace("SrsiProperties(", "");
        p = p.replace(")", "");
        StringBuilder sb = new StringBuilder();
        for (String s : p.split(",")) {
            if (excluded.contains(s.split("=")[0].trim())) continue;
            sb.append("      ")
                    .append(s.trim().replace("=", ": "))
                    .append("\n");
        }
        System.out.println(sb.toString());
    }

}
