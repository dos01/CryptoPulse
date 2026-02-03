package com.cryptotrader.services;

import com.cryptotrader.analysis.TechnicalIndicators;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PredictionService {

    private final MarketDataService marketDataService;

    public PredictionService() {
        this.marketDataService = new MarketDataService();
    }

    public enum Signal {
        BUY, SELL, HOLD, UNKNOWN
    }

    /**
     * Analyzes a coin and returns a trading signal.
     * Uses RSI and SMA cross strategy (simplified).
     */
    public CompletableFuture<Signal> analyze(String coinId) {
        return CompletableFuture.supplyAsync(() -> {
            // Fetch 30 days of data
            List<Double> history = marketDataService.getPriceHistory(coinId, 30);
            if (history.size() < 14) {
                return Signal.UNKNOWN;
            }

            double rsi = TechnicalIndicators.calculateRSI(history, 14);
            double currentPrice = history.get(history.size() - 1);
            double sma20 = TechnicalIndicators.calculateSMA(history, 20);

            if (rsi < 30 && currentPrice > sma20) {
                // Oversold but price above trend
                return Signal.BUY;
            } else if (rsi > 70 && currentPrice < sma20) {
                // Overbought but price below trend
                return Signal.SELL;
            } else if (rsi < 30) {
                return Signal.BUY; // Aggressive buy
            } else if (rsi > 70) {
                return Signal.SELL; // Aggressive sell
            }

            return Signal.HOLD;
        });
    }
}
