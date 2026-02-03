package com.cryptotrader.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CoinModel {
    private final StringProperty name;
    private final DoubleProperty price;

    public CoinModel(String name, double price) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public double getPrice() {
        return price.get();
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    private final StringProperty signal = new SimpleStringProperty("WAIT");

    public String getSignal() {
        return signal.get();
    }

    public StringProperty signalProperty() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal.set(signal);
    }
}
