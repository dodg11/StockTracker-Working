package com.stocktracker.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PriceFetcher {
    private static final String TAG = "PriceFetcher";
    private static final String PREFS_NAME = "StockTrackerPrefs";
    private static final String KEY_API_KEY = "api_key";

    // Free APIs that don't require API key
    private static final String API_YAHOO = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String API_YAHOO_QUOTE = "https://query1.finance.yahoo.com/v7/finance/quote?symbols=";

    public interface PriceCallback {
        void onPriceFetched(String ticker, double price);
        void onError(String ticker, String error);
    }

    public interface BatchPriceCallback {
        void onPricesFetched(Map<String, Double> prices);
        void onError(String error);
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    public void fetchPrice(String ticker, PriceCallback callback) {
        executor.execute(() -> {
            try {
                double price = fetchFromYahoo(ticker);
                if (price > 0) {
                    callback.onPriceFetched(ticker, price);
                } else {
                    callback.onError(ticker, "Цена не найдена");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching price for " + ticker, e);
                callback.onError(ticker, e.getMessage());
            }
        });
    }

    public void fetchBatchPrices(String[] tickers, BatchPriceCallback callback) {
        executor.execute(() -> {
            Map<String, Double> prices = new HashMap<>();
            try {
                StringBuilder symbols = new StringBuilder();
                for (int i = 0; i < tickers.length; i++) {
                    if (i > 0) symbols.append(",");
                    symbols.append(tickers[i]);
                }

                String urlStr = API_YAHOO_QUOTE + symbols.toString();
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject json = new JSONObject(response.toString());
                    JSONObject quoteResponse = json.getJSONObject("quoteResponse");
                    org.json.JSONArray results = quoteResponse.getJSONArray("result");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject item = results.getJSONObject(i);
                        String symbol = item.getString("symbol");
                        double price = item.optDouble("regularMarketPrice", 0);
                        if (price > 0) {
                            prices.put(symbol, price);
                        }
                    }
                }
                conn.disconnect();

                // For tickers not found in batch, try individual fetch
                for (String ticker : tickers) {
                    if (!prices.containsKey(ticker)) {
                        try {
                            double price = fetchFromYahoo(ticker);
                            if (price > 0) {
                                prices.put(ticker, price);
                            }
                        } catch (Exception ignored) {}
                    }
                }

                callback.onPricesFetched(prices);
            } catch (Exception e) {
                Log.e(TAG, "Batch fetch error", e);
                callback.onError(e.getMessage());
            }
        });
    }

    private double fetchFromYahoo(String ticker) throws Exception {
        String urlStr = API_YAHOO + ticker + "?interval=1d&range=1d";
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject chart = json.getJSONObject("chart");
            org.json.JSONArray results = chart.getJSONArray("result");
            if (results.length() > 0) {
                JSONObject result = results.getJSONObject(0);
                JSONObject meta = result.getJSONObject("meta");
                return meta.optDouble("regularMarketPrice", 0);
            }
        }
        conn.disconnect();
        return 0;
    }

    // Alternative: simulate price for testing/demo purposes
    public double getSimulatedPrice(String ticker, double basePrice) {
        // In real app, this would be replaced with actual API call
        // This is a fallback for demo purposes
        double variation = (Math.random() - 0.5) * 0.04; // ±2% random
        return basePrice * (1 + variation);
    }

    public void shutdown() {
        executor.shutdown();
    }
}
