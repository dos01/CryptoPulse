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
    @FXML
    private Label topGainerLabel;

    private final MarketDataService marketDataService = new MarketDataService();
    private final ObservableList<CoinModel> coinList = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @FXML
    public void initialize() {
        coinColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty());
        priceColumn.setCellFactory(column -> new TableCell<CoinModel, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%,.2f", item.doubleValue()));
                }
            }
        });

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
                    setText(String.format("%s%.2f%%", (val > 0 ? "+" : ""), val));
                    getStyleClass().removeAll("text-success", "text-danger", "text-neutral");
                    if (val > 0) {
                        getStyleClass().add("text-success");
                    } else if (val < 0) {
                        getStyleClass().add("text-danger");
                    } else {
                        getStyleClass().add("text-neutral");
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

                    double mockChange = (Math.random() * 10) - 5;
                    String mockCap = "$" + (100 + (int) (Math.random() * 400)) + "B";

                    if (existing != null) {
                        existing.priceProperty().set(price);
                        existing.change24hProperty().set(mockChange);
                    } else {
                        coinList.add(new CoinModel(name, price, mockChange, mockCap));
                    }
                });

                // Update Top Gainer Card
                coinList.stream()
                        .max((c1, c2) -> Double.compare(c1.getChange24h(), c2.getChange24h()))
                        .ifPresent(top -> {
                            topGainerLabel.setText(String.format("%s (+%.2f%%)",
                                    top.getName().substring(0, 1).toUpperCase() + top.getName().substring(1),
                                    top.getChange24h()));
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
