package com.cryptotrader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.cryptotrader.services.DatabaseService;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("App starting...");
        try {
            DatabaseService.initialize();
            System.out.println("DB initialized.");

            Parent root = loadFXML("dashboard");
            System.out.println("FXML loaded.");

            scene = new Scene(root, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("Crypto Trader - Market Monitor");
            stage.show();
            System.out.println("Stage shown.");
        } catch (Exception e) {
            System.err.println("CRASH in start(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    @Override
    public void stop() {
        System.out.println("App stopping...");
        // In a real app, you'd find a way to shutdown the scheduler
        // But for this simple app, JVM shutdown will handle it.
    }

    public static void main(String[] args) {
        launch();
    }
}
