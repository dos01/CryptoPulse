package com.cryptotrader.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    public static List<String> getWatchlist() {
        List<String> list = new ArrayList<>();
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

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
