package knapsack;

public class Knapsack {
    //w weight
    //v value
    //n number
    //W Capacity
    int knapsackRec(int[] w, int[] v, int n, int W) {
        if (n <= 0) {
            return 0;
        } else if (w[n - 1] > W) {
            return knapsackRec(w, v, n - 1, W);
        } else {
            return Math.max(knapsackRec(w, v, n - 1, W), v[n - 1]
                    + knapsackRec(w, v, n - 1, W - w[n - 1]));
        }
    }
}
