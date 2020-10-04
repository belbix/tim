package pro.belbix.tim.utils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SubArraysUtilTest {

    @Test
    public void findMaxSubArrays() {
        double[] arr = {0.0, 17.0, -0.5, -0.5, -6.0, -2.5, -2.0, -0.5, -7.0, -1.0, -1.0, -1.0, -5.5, -2.0, -2.0, -12.0,
                -8.0, 7.0, -10.0, -0.5, -2.0, 6.0, -1.5, -1.0, 1.5, -1.0, 4.5};
        List<int[]> r = SubArraysUtil.findMaxSubArrays(arr, 1.);

        for (int[] p : r) {
            double sum = (arr[p[1]] - arr[p[0]]);
            System.out.println(Arrays.toString(p) + " " + sum);
        }
    }
}
