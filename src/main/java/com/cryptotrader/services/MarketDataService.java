package com.cryptotrader.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketDataService {

    private static final String API_URL = "https://api.coingecko.com/api/v3/simple/price";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Cache for historical data: Key = coinId, Value = PriceHistoryCache object
    private final Map<String, PriceHistoryCache> historyCache = new HashMap<>();
    private static final long CACHE_DURATION_MS = 30 * 60 * 1000; // 30 minutes

    // Cooldown mechanism for 429 errors
    private static long cooldownUntil = 0;
    private static final long COOLDOWN_DURATION_MS = 60 * 1000; // 1 minute

    public MarketDataService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    private static class PriceHistoryCache {
        final List<Double> data;
        final long timestamp;

        PriceHistoryCache(List<Double> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION_MS;
        }
    }

    public static boolean isRateLimited() {
        return System.currentTimeMillis() < cooldownUntil;
    }

    private synchronized void setCooldown() {
        cooldownUntil = System.currentTimeMillis() + COOLDOWN_DURATION_MS;
    }

    /**
     * Fetches current prices for the given coin IDs.
     * 
     * @param coinIds List of coin IDs (e.g., "bitcoin", "ethereum")
     * @return Map of coin ID to current price in USD
     */
    public Map<String, Double> getPrices(List<String> coinIds) {
        Map<String, Double> prices = new HashMap<>();
        if (isRateLimited()) {
            System.err.println("Skipping price fetch: API Rate Limited");
            return prices;
        }

        String ids = String.join(",", coinIds);
        String uri = API_URL + "?ids=" + ids + "&vs_currencies=usd";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                for (String id : coinIds) {
                    if (root.has(id) && root.get(id).has("usd")) {
                        prices.put(id, root.get(id).get("usd").asDouble());
                    }
                }
            } else if (response.statusCode() == 429) {
                setCooldown();
                System.err.println("API Error 429: Entering Cooldown");
            } else {
                System.err.println("API Error: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return prices;
    }

    /**
     * Fetches historical price data for a coin.
     * 
     * @param coinId Coin ID (e.g., "bitcoin")
     * @param days   Number of days of data
     * @return List of prices (double)
     */
    public List<Double> getPriceHistory(String coinId, int days) {
        // Check Cache first
        PriceHistoryCache cached = historyCache.get(coinId);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }

        if (isRateLimited()) {
            System.err.println("Skipping history fetch: API Rate Limited");
            return cached != null ? cached.data : new java.util.ArrayList<>();
        }

        String uri = "https://api.coingecko.com/api/v3/coins/" + coinId + "/market_chart?vs_currency=usd&days=" + days;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Accept", "application/json")
                .GET()
                .build();

        List<Double> priceHistory = new java.util.ArrayList<>();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                if (root.has("prices")) {
                    for (JsonNode priceNode : root.get("prices")) {
                        priceHistory.add(priceNode.get(1).asDouble());
                    }
                    // Update cache
                    historyCache.put(coinId, new PriceHistoryCache(priceHistory));
                }
            } else if (response.statusCode() == 429) {
                setCooldown();
                System.err.println("API Error 429 (History): Entering Cooldown");
                return cached != null ? cached.data : new java.util.ArrayList<>();
            } else {
                System.err.println("API Error history: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return priceHistory;
    }
}
