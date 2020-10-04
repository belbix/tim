package pro.belbix.tim;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.models.SyntheticDecision;
import pro.belbix.tim.services.DecisionFinder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class DecisionFinderApp {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        ConfigurableApplicationContext context = SpringApplication.run(SimpleApp.class, args);
        DecisionFinder decisionFinder = context.getBean(DecisionFinder.class);

        List<SyntheticDecision> result = decisionFinder.findDecisions(
                LocalDateTime.parse("2019-04-01T00:00:00")
                , LocalDateTime.parse("2019-12-01T00:00:00")
                , 1.01
        );

        int count = 0;
        List<Double> deltaOldOpenLong = new ArrayList<>();
        List<Double> deltaFirstCloseLong = new ArrayList<>();
        for (SyntheticDecision decision : result) {
            if (decision.getProfit() < 100) continue;
            count++;

            List<Candle> openCandles = decision.getSrsiOpen().toCandles(360);
            List<Candle> closeCandles = decision.getSrsiClose().toCandles(360);
            double srsiOpen = openCandles.get(2).calcDeltaK();
            deltaOldOpenLong.add(srsiOpen);
            double srsiClose = closeCandles.get(0).calcDeltaK();
            deltaFirstCloseLong.add(srsiClose);

            System.out.println(decision);
        }
        System.out.println("Total decision: " + result.size() + " / " + count);


        double openMax = Collections.max(deltaOldOpenLong);
        double openMin = Collections.min(deltaOldOpenLong);
        double openAvg = deltaOldOpenLong.stream().mapToDouble(a -> a).average().orElse(0.0);

        double closeMax = Collections.max(deltaFirstCloseLong);
        double closeMin = Collections.min(deltaFirstCloseLong);
        double closeAvg = deltaFirstCloseLong.stream().mapToDouble(a -> a).average().orElse(0.0);

        System.out.println(
                "openMax: " + openMax
                        + " openMin" + openMin
                        + " openAvg" + openAvg
                        + " closeMax" + closeMax
                        + " closeMin" + closeMin
                        + " closeAvg" + closeAvg
        );

        String fileName = System.getProperty("user.dir") + File.separator + "histories"
                + File.separator + "decisions_" + System.currentTimeMillis() + ".txt";

        File f = new File(fileName);
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("File " + fileName + ": " + e.getMessage());
            return;
        }
        try (PrintWriter printWriter = new PrintWriter(new FileWriter(f))) {
            for (int i = 0; i < deltaFirstCloseLong.size(); i++) {
                printWriter.println(deltaOldOpenLong.get(i) + ";" + deltaFirstCloseLong.get(i));
            }

        } catch (IOException e) {
            System.out.println("File " + fileName + ": " + e.getMessage());
        }

        System.exit(1);
    }

}
