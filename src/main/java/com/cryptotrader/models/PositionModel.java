package com.cryptotrader.models;

import javafx.beans.property.*;

public class PositionModel {
    private final StringProperty symbol;
    private final DoubleProperty quantity;
    private final DoubleProperty avgBuyPrice;
    private final DoubleProperty currentPrice;
    private final DoubleProperty profitLoss;

    public PositionModel(String symbol, double quantity, double avgBuyPrice) {
        this.symbol = new SimpleStringProperty(symbol);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.avgBuyPrice = new SimpleDoubleProperty(avgBuyPrice);
        this.currentPrice = new SimpleDoubleProperty(0.0);
        this.profitLoss = new SimpleDoubleProperty(0.0);
    }

    public StringProperty symbolProperty() {
        return symbol;
    }

    public String getSymbol() {
        return symbol.get();
    }

    public DoubleProperty quantityProperty() {
        return quantity;
    }

    public double getQuantity() {
        return quantity.get();
    }

    public DoubleProperty avgBuyPriceProperty() {
        return avgBuyPrice;
    }

    public double getAvgBuyPrice() {
        return avgBuyPrice.get();
    }

    public DoubleProperty currentPriceProperty() {
        return currentPrice;
    }

    public void setCurrentPrice(double price) {
        this.currentPrice.set(price);
        calculatePL();
    }

    public DoubleProperty profitLossProperty() {
        return profitLoss;
    }

    private void calculatePL() {
        double pl = (currentPrice.get() - avgBuyPrice.get()) * quantity.get();
        this.profitLoss.set(pl);
    }
}
