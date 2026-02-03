package com.cryptotrader;

import com.cryptotrader.models.CoinModel;
import com.cryptotrader.services.MarketDataService;
import com.cryptotrader.services.DatabaseService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;

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
    private TableColumn<CoinModel, Number> changeColumn;
    @FXML
    private TableColumn<CoinModel, String> marketCapColumn;
    @FXML
    private Label statusLabel;

    private final MarketDataService marketDataService = new MarketDataService();
    private final ObservableList<CoinModel> coinList = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {
        coinColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        changeColumn.setCellValueFactory(cellData -> cellData.getValue().change24hProperty());
        marketCapColumn.setCellValueFactory(cellData -> cellData.getValue().marketCapProperty());

        // Custom cell factory for price coloring
        changeColumn.setCellFactory(column -> new TableCell<CoinModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    double val = item.doubleValue();
                    setText(String.format("%.2f%%", val));
                    if (val > 0) {
                        getStyleClass().removeAll("price-down");
                        getStyleClass().add("price-up");
                    } else if (val < 0) {
                        getStyleClass().removeAll("price-up");
                        getStyleClass().add("price-down");
                    }
                }
            }
        });

        priceTable.setItems(coinList);
        handleRefresh();
        scheduler.scheduleAtFixedRate(this::handleRefresh, 1, 1, TimeUnit.MINUTES);
    }

    @FXML
    private void handleRefresh() {
        if (MarketDataService.isRateLimited()) {
            Platform.runLater(() -> statusLabel.setText("API Status: Rate Limited (Cooldown)"));
            return;
        }

        Platform.runLater(() -> statusLabel.setText("API Status: Syncing..."));

        List<String> coins = DatabaseService.getWatchlist();
        if (coins.isEmpty()) {
            coins = Arrays.asList("bitcoin", "ethereum", "binancecoin", "solana", "ripple", "cardano", "polkadot",
                    "dogecoin");
            for (String c : coins)
                DatabaseService.addToWatchlist(c);
        }

        final List<String> fCoins = coins;
        CompletableFuture.runAsync(() -> {
            // In a real app we'd fetch actual change and market cap.
            // For now, let's enhance MarketDataService or simulate for visual impact.
            Map<String, Double> prices = marketDataService.getPrices(fCoins);

            Platform.runLater(() -> {
                if (MarketDataService.isRateLimited()) {
                    statusLabel.setText("API Status: Rate Limited");
                    return;
                }
                statusLabel.setText("Last Synced: "
                        + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));

                prices.forEach((name, price) -> {
                    CoinModel existing = coinList.stream()
                            .filter(c -> c.getName().equalsIgnoreCase(name))
                            .findFirst()
                            .orElse(null);

                    double mockChange = (Math.random() * 10) - 5; // Simulating for visual feedback
                    String mockCap = "$" + (int) (Math.random() * 500) + "B";

                    if (existing != null) {
                        existing.priceProperty().set(price);
                        existing.change24hProperty().set(mockChange);
                    } else {
                        coinList.add(new CoinModel(name, price, mockChange, mockCap));
                    }
                });
            });
        });
    }

    @FXML
    private void handleAddToWatchlist() {
        // Just a placeholder for now to keep it simple
        handleRefresh();
    }
}
