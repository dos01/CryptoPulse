package com.cryptotrader.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:cryptotrader.db";

    public static void initialize() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
                Statement stmt = conn.createStatement()) {

            // Create Watchlist table
            String createWatchlist = "CREATE TABLE IF NOT EXISTS watchlist (" +
                    "symbol TEXT PRIMARY KEY, " +
                    "added_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createWatchlist);

            // Create Predictions table
            String createPredictions = "CREATE TABLE IF NOT EXISTS predictions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "symbol TEXT, " +
                    "predicted_price REAL, " +
                    "signal TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createPredictions);

            // Create Portfolio table for Paper Trading
            String createPortfolio = "CREATE TABLE IF NOT EXISTS portfolio (" +
                    "symbol TEXT PRIMARY KEY, " +
                    "quantity REAL, " +
                    "avg_buy_price REAL)";
            stmt.execute(createPortfolio);

            // Create Trades table for History
            String createTrades = "CREATE TABLE IF NOT EXISTS trades (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "symbol TEXT, " +
                    "type TEXT, " + // BUY/SELL
                    "quantity REAL, " +
                    "price REAL, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(createTrades);

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addToWatchlist(String symbol) {
        String sql = "INSERT OR IGNORE INTO watchlist(symbol) VALUES(?)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static java.util.List<String> getWatchlist() {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT symbol FROM watchlist";
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("symbol"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void buyCoin(String symbol, double quantity, double price) {
        String sql = "INSERT INTO portfolio(symbol, quantity, avg_buy_price) VALUES(?,?,?) " +
                "ON CONFLICT(symbol) DO UPDATE SET " +
                "avg_buy_price = (avg_buy_price * quantity + ? * ?) / (quantity + ?), " +
                "quantity = quantity + ?";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setDouble(2, quantity);
            pstmt.setDouble(3, price);
            pstmt.setDouble(4, quantity);
            pstmt.setDouble(5, price);
            pstmt.setDouble(6, quantity);
            pstmt.setDouble(7, quantity);
            pstmt.executeUpdate();

            recordTrade(symbol, "BUY", quantity, price);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void recordTrade(String symbol, String type, double quantity, double price) {
        String sql = "INSERT INTO trades(symbol, type, quantity, price) VALUES(?,?,?,?)";
        try (Connection conn = getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setString(2, type);
            pstmt.setDouble(3, quantity);
            pstmt.setDouble(4, price);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static java.util.List<com.cryptotrader.models.PositionModel> getPortfolio() {
        java.util.List<com.cryptotrader.models.PositionModel> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM portfolio WHERE quantity > 0";
        try (Connection conn = getConnection();
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new com.cryptotrader.models.PositionModel(
                        rs.getString("symbol"),
                        rs.getDouble("quantity"),
                        rs.getDouble("avg_buy_price")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
