package uk.co.automatictester.lightning.utils;

public class Percentile {

    public static boolean isPercentile(int percentile) {
        return (percentile > 0) && (percentile <= 100);
    }
}