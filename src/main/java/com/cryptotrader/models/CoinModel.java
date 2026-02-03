package com.cryptotrader.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CoinModel {
    private final StringProperty name;
    private final DoubleProperty price;
    private final DoubleProperty change24h;
    private final StringProperty marketCap;

    public CoinModel(String name, double price, double change24h, String marketCap) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.change24h = new SimpleDoubleProperty(change24h);
        this.marketCap = new SimpleStringProperty(marketCap);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public double getChange24h() {
        return change24h.get();
    }

    public DoubleProperty change24hProperty() {
        return change24h;
    }

    public String getMarketCap() {
        return marketCap.get();
    }

    public StringProperty marketCapProperty() {
        return marketCap;
    }
}
