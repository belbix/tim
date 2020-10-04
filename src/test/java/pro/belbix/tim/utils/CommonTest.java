package pro.belbix.tim.utils;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class CommonTest {

    private static void incrementMap(Map<Integer, Integer> m, int key) {
        if (m.containsKey(key)) {
            m.put(key, m.get(key) + 1);
        } else {
            m.put(key, 1);
        }
    }

    @Test
    public void normalDistributionRandom() {
        Map<Integer, Integer> m = new HashMap<>();
        int count = 0;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        while (count < 1000000) {
            double d = Common.normalDistributionRandom();
            d = (d + 5) / 10;
            if (d < min) min = d;
            if (d > max) max = d;
            if (d < 0) {
                incrementMap(m, 0);
            } else if (d >= 0 && d < 0.1) {
                incrementMap(m, 1);
            } else if (d >= 0.1 && d < 0.2) {
                incrementMap(m, 2);
            } else if (d >= 0.2 && d < 0.3) {
                incrementMap(m, 3);
            } else if (d >= 0.3 && d < 0.4) {
                incrementMap(m, 4);
            } else if (d >= 0.4 && d < 0.5) {
                incrementMap(m, 5);
            } else if (d >= 0.5 && d < 0.6) {
                incrementMap(m, 6);
            } else if (d >= 0.6 && d < 0.7) {
                incrementMap(m, 7);
            } else if (d >= 0.7 && d < 0.8) {
                incrementMap(m, 8);
            } else if (d >= 0.8 && d < 0.9) {
                incrementMap(m, 9);
            } else if (d >= 0.9 && d <= 1) {
                incrementMap(m, 10);
            } else {
                incrementMap(m, 11);
            }
            count++;
        }

        for (int i = 0; i <= 11; i++) {
            System.out.println(i + " " + m.get(i));
        }
        System.out.println("max " + max);
        System.out.println("min " + min);
    }
}
