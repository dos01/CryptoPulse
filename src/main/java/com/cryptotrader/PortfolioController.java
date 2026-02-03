package com.cryptotrader;

import com.cryptotrader.models.PositionModel;
import com.cryptotrader.services.DatabaseService;
import com.cryptotrader.services.MarketDataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PortfolioController {

    @FXML
    private TableView<PositionModel> portfolioTable;
    @FXML
    private TableColumn<PositionModel, String> symbolColumn;
    @FXML
    private TableColumn<PositionModel, Number> quantityColumn;
    @FXML
    private TableColumn<PositionModel, Number> avgPriceColumn;
    @FXML
    private TableColumn<PositionModel, Number> currentPriceColumn;
    @FXML
    private TableColumn<PositionModel, Number> plColumn;
    @FXML
    private Label totalPLValue;

    private final MarketDataService marketDataService = new MarketDataService();
    private final ObservableList<PositionModel> positionList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        symbolColumn.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty());
        avgPriceColumn.setCellValueFactory(cellData -> cellData.getValue().avgBuyPriceProperty());
        currentPriceColumn.setCellValueFactory(cellData -> cellData.getValue().currentPriceProperty());
        plColumn.setCellValueFactory(cellData -> cellData.getValue().profitLossProperty());

        portfolioTable.setItems(positionList);
        loadPortfolio();
    }

    @FXML
    private void handleRefresh() {
        loadPortfolio();
    }

    private void loadPortfolio() {
        List<PositionModel> positions = DatabaseService.getPortfolio();
        positionList.setAll(positions);

        if (positions.isEmpty())
            return;

        List<String> symbols = positions.stream().map(PositionModel::getSymbol).collect(Collectors.toList());

        CompletableFuture.runAsync(() -> {
            Map<String, Double> prices = marketDataService.getPrices(symbols);
            Platform.runLater(() -> {
                double totalPL = 0;
                for (PositionModel pos : positionList) {
                    if (prices.containsKey(pos.getSymbol())) {
                        pos.setCurrentPrice(prices.get(pos.getSymbol()));
                        totalPL += pos.profitLossProperty().get();
                    }
                }
                totalPLValue.setText(String.format("$%.2f", totalPL));
                if (totalPL >= 0)
                    totalPLValue.setStyle("-fx-text-fill: #a6e3a1; -fx-font-weight: bold;");
                else
                    totalPLValue.setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
            });
        });
    }
}
