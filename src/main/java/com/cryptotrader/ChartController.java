package com.cryptotrader;

import com.cryptotrader.services.MarketDataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChartController {

    @FXML
    private LineChart<String, Number> priceChart;

    private final MarketDataService marketDataService = new MarketDataService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private String currentCoinId;

    public void setCoinId(String coinId) {
        this.currentCoinId = coinId;
        priceChart.setTitle(coinId.toUpperCase() + " Price History");
        loadData(coinId);

        // Refresh chart every 2 minutes
        scheduler.scheduleAtFixedRate(() -> loadData(currentCoinId), 2, 2, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void loadData(String coinId) {
        CompletableFuture.runAsync(() -> {
            try {
                List<Double> history = marketDataService.getPriceHistory(coinId, 30);

                Platform.runLater(() -> {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName("Price (USD)");

                    for (int i = 0; i < history.size(); i++) {
                        series.getData().add(new XYChart.Data<>(String.valueOf(i), history.get(i)));
                    }

                    priceChart.getData().clear();
                    priceChart.getData().add(series);
                });
            } catch (Exception e) {
                System.err.println("Error loading chart data: " + e.getMessage());
            }
        });
    }
}
