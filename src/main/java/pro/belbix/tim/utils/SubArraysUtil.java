package pro.belbix.tim.utils;

import java.util.ArrayList;
import java.util.List;

public class SubArraysUtil {

    /**
     * O(n) complexity<br><br>
     * <p>
     * idea is that for EACH element of array there are 2 options:<br>
     * 1) we can take element only with its left neighbour<br>
     * (if we want right than it'll happen on next iteration)<br>
     * 2) we do not take element and its neighbour at all<br><br>
     * <p>
     * dp = dynatic programming<br><br>
     * <p>
     * dp[i][0] - result after processing through i+1 first numbers<br>
     * IF we do not take i-th element and its left neighbour<br><br>
     * <p>
     * dp[i][1] - result after processing through i+1 first numbers <br>
     * IF we take i-th element and its left neighbour<br><br>
     * <p>
     * if we do not take i-th element (and left nei) then dp[i][0] is just max(dp[i-1][0], dp[i-1][1])<br><br>
     * <p>
     * if we take i-th element(and left nei) then dp[i][1] is:<br><br>
     * either dp[i-1][0] + i-th element + left neighbour<br>
     * or dp[i-1][1] + i-th element<br>
     * ^ there we do not add left neighbour because we already added it to dp[i-1][1]<br><br>
     */
    public static List<int[]> findMaxSubArrays(double[] arr, double factor) {
        double[][] dp = new double[arr.length][2];
        dp[0] = new double[]{0, Double.MIN_VALUE};
//        Arrays.fill(dp, new double[]{Double.MIN_VALUE, Double.MIN_VALUE});

        int[][] par = new int[arr.length][2];
        par[0] = new int[]{-1, -1};

        for (int i = 1; i < arr.length; i++) {
            for (int j = 0; j < 2; j++) {
                if (dp[i][0] < dp[i - 1][j]) {
                    dp[i][0] = dp[i - 1][j];
                    par[i][0] = j;
                }
            }
            if (dp[i][1] < (dp[i - 1][0] + arr[i] + arr[i - 1]) * factor) {
                dp[i][1] = dp[i - 1][0] + arr[i] + arr[i - 1];
                par[i][1] = 0;
            }
            if (dp[i][1] < (dp[i - 1][1] + arr[i]) * factor) {
                dp[i][1] = dp[i - 1][1] + arr[i];
                par[i][1] = 1;
            }
        }

        //now we restore what indexes we have used
        int[] mask = new int[arr.length + 1];
        int curj;
        if (dp[dp.length - 1][0] > dp[dp.length - 1][1]) {
            curj = 0;
        } else {
            curj = 1;
        }
        for (int i = arr.length - 1; i > 0; i--) {
            if (curj != 0) {
                mask[i] = mask[i - 1] = 1;
            }
            curj = par[i][curj];
        }

        mask[mask.length - 1] = 0; //just for convenience
        List<int[]> result = new ArrayList<>();

        //now we restore all the [l, r]
        for (int i = 0; i < mask.length; ++i) {
            if (mask[i] == 0) {
                continue;
            }
            int j;
            for (j = i; j < mask.length; ++j) {
                if (mask[j] == 1) {
                    mask[j] = 0;
                    continue;
                } else {
                    result.add(new int[]{i, j - 1});
                    break;
                }
            }
        }
        return result;
    }
}
