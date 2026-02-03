package com.cryptotrader.analysis;

import java.util.List;

public class TechnicalIndicators {

    /**
     * Calculates Simple Moving Average (SMA)
     */
    public static double calculateSMA(List<Double> prices, int period) {
        if (prices == null || prices.size() < period) {
            return 0.0;
        }
        double sum = 0.0;
        // Last 'period' elements
        for (int i = prices.size() - period; i < prices.size(); i++) {
            sum += prices.get(i);
        }
        return sum / period;
    }

    /**
     * Calculates Relative Strength Index (RSI)
     */
    public static double calculateRSI(List<Double> prices, int period) {
        if (prices == null || prices.size() < period + 1) {
            return 50.0; // Default separate
        }

        double gain = 0.0;
        double loss = 0.0;

        // Calculate initial gain/loss
        for (int i = prices.size() - period; i < prices.size(); i++) {
            double change = prices.get(i) - prices.get(i - 1);
            if (change > 0) {
                gain += change;
            } else {
                loss -= change;
            }
        }

        if (loss == 0)
            return 100.0;

        double rs = gain / loss;
        return 100.0 - (100.0 / (1.0 + rs));
    }
}
