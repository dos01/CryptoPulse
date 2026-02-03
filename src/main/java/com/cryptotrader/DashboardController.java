package com.cryptotrader;

import com.cryptotrader.models.CoinModel;
import com.cryptotrader.services.MarketDataService;
import com.cryptotrader.services.PredictionService;
import com.cryptotrader.services.DatabaseService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DashboardController {

    @FXML
    private TableView<CoinModel> priceTable;
    @FXML
    private TableColumn<CoinModel, String> coinColumn;
    @FXML
    private TableColumn<CoinModel, Number> priceColumn;
    @FXML
    private TableColumn<CoinModel, String> signalColumn;
    @FXML
    private javafx.scene.control.Label statusLabel;

    private final MarketDataService marketDataService = new MarketDataService();
    private final PredictionService predictionService = new PredictionService();
    private final ObservableList<CoinModel> coinList = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {
        coinColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        signalColumn.setCellValueFactory(cellData -> cellData.getValue().signalProperty());

        priceTable.setItems(coinList);

        // Add double click listener to open chart
        priceTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && priceTable.getSelectionModel().getSelectedItem() != null) {
                CoinModel selectedCoin = priceTable.getSelectionModel().getSelectedItem();
                openChart(selectedCoin.getName());
            }
        });

        // Load initial data
        handleRefresh();

        // Background refresh system every 2 minutes
        scheduler.scheduleAtFixedRate(this::handleRefresh, 2, 2, TimeUnit.MINUTES);
    }

    private void openChart(String coinId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chart.fxml"));
            Parent root = loader.load();

            ChartController controller = loader.getController();
            controller.setCoinId(coinId);

            Stage stage = new Stage();
            stage.setTitle(coinId + " - Price History");
            stage.setScene(new Scene(root, 800, 600));
            // Ensure scheduler stops when window is closed
            stage.setOnCloseRequest(event -> controller.stop());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        if (MarketDataService.isRateLimited()) {
            Platform.runLater(() -> statusLabel.setText("API Status: Rate Limited (Cooldown)"));
            return;
        }

        Platform.runLater(() -> statusLabel.setText("API Status: Refreshing..."));

        List<String> coins = DatabaseService.getWatchlist();
        if (coins.isEmpty()) {
            coins = Arrays.asList("bitcoin", "ethereum", "ripple", "cardano", "solana");
            for (String c : coins)
                DatabaseService.addToWatchlist(c);
        }

        final List<String> fCoins = coins;
        CompletableFuture.runAsync(() -> {
            Map<String, Double> prices = marketDataService.getPrices(fCoins);

            Platform.runLater(() -> {
                if (MarketDataService.isRateLimited()) {
                    statusLabel.setText("API Status: Rate Limited (429)");
                    return;
                }
                statusLabel.setText("API Status: Updated");

                prices.forEach((name, price) -> {
                    // Find existing coin or add new one
                    CoinModel existing = coinList.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(name))
                            .findFirst()
                            .orElse(null);

                    if (existing != null) {
                        existing.priceProperty().set(price);
                    } else {
                        CoinModel coin = new CoinModel(name, price);
                        coinList.add(coin);
                    }

                    predictionService.analyze(name).thenAccept(signal -> {
                        Platform.runLater(() -> {
                            coinList.stream()
                                    .filter(c -> c.getName().equalsIgnoreCase(name))
                                    .findFirst()
                                    .ifPresent(c -> c.setSignal(signal.toString()));
                        });
                    });
                });
            });
        });
    }

    @FXML
    private void handleAddToWatchlist() {
        // For simplicity, add a hardcoded one or implement a dialog later
        // Let's add 'polkadot' as an example for now
        DatabaseService.addToWatchlist("polkadot");
        handleRefresh();
    }

    @FXML
    private void handleSimulateBuy() {
        com.cryptotrader.models.CoinModel selected = priceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Simulating BUY for " + selected.getName() + " at $" + selected.getPrice());
            DatabaseService.buyCoin(selected.getName(), 1.0, selected.getPrice());
        }
    }

    @FXML
    private void handleViewPortfolio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("portfolio.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("My Portfolio");
            stage.setScene(new Scene(root, 1000, 700));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
