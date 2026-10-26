package com.stocktracker.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.Map;

public class StockMonitorService extends Service {
    private static final String TAG = "StockMonitorService";
    private static final String PREFS_NAME = "StockTrackerPrefs";
    private static final String KEY_INTERVAL = "monitoring_interval";
    private static final String KEY_DROP_THRESHOLD = "drop_threshold";
    private static final String KEY_LAST_UPDATE = "last_update";
    private static final String CHANNEL_ID = "monitoring_service";
    private static final int FOREGROUND_ID = 1;

    private Handler handler;
    private DatabaseHelper db;
    private PriceFetcher priceFetcher;
    private NotificationHelper notificationHelper;
    private boolean isMonitoring = false;
    private long intervalMinutes = 5;
    private double defaultDropThreshold = 5.0;

    private final Runnable monitoringRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isMonitoring) return;
            checkAllStocks();
            handler.postDelayed(this, intervalMinutes * 60 * 1000);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        handler = new Handler(Looper.getMainLooper());
        db = new DatabaseHelper(this);
        priceFetcher = new PriceFetcher();
        notificationHelper = new NotificationHelper(this);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        intervalMinutes = prefs.getLong(KEY_INTERVAL, 5);
        defaultDropThreshold = prefs.getFloat(KEY_DROP_THRESHOLD, 5.0f);

        createServiceChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // Для Android 14+ нужно указывать foregroundServiceType
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(FOREGROUND_ID, createServiceNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(FOREGROUND_ID, createServiceNotification());
        }

        startMonitoring();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMonitoring();
        if (priceFetcher != null) {
            priceFetcher.shutdown();
        }
        Log.d(TAG, "Service destroyed");
    }

    private void startMonitoring() {
        if (!isMonitoring) {
            isMonitoring = true;
            handler.post(monitoringRunnable);
            Log.d(TAG, "Monitoring started with interval: " + intervalMinutes + " min");
        }
    }

    private void stopMonitoring() {
        isMonitoring = false;
        handler.removeCallbacks(monitoringRunnable);
        Log.d(TAG, "Monitoring stopped");
    }

    private void checkAllStocks() {
        List<Stock> allStocks = db.getAllStocks();
        if (allStocks.isEmpty()) {
            Log.d(TAG, "No stocks to monitor");
            return;
        }

        String[] tickers = new String[allStocks.size()];
        for (int i = 0; i < allStocks.size(); i++) {
            tickers[i] = allStocks.get(i).getTicker();
        }

        priceFetcher.fetchBatchPrices(tickers, new PriceFetcher.BatchPriceCallback() {
            @Override
            public void onPricesFetched(Map<String, Double> prices) {
                int alertsTriggered = 0;

                for (Stock stock : allStocks) {
                    Double newPrice = prices.get(stock.getTicker());
                    if (newPrice == null || newPrice <= 0) continue;

                    double oldPrice = stock.getCurrentPrice();
                    stock.setPreviousPrice(oldPrice);
                    stock.setCurrentPrice(newPrice);

                    // Update in DB
                    db.updatePrice(stock.getId(), newPrice, oldPrice);

                    // Check alerts
                    if (shouldCheckAlerts(stock)) {
                        alertsTriggered += checkAlerts(stock);
                    }
                }

                // Save last update time
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit().putLong(KEY_LAST_UPDATE, System.currentTimeMillis()).apply();

                if (alertsTriggered > 0) {
                    notificationHelper.showPriceUpdateSummary(allStocks.size(), alertsTriggered);
                }

                Log.d(TAG, "Checked " + allStocks.size() + " stocks, " + alertsTriggered + " alerts");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching prices: " + error);
            }
        });
    }

    private boolean shouldCheckAlerts(Stock stock) {
        // Don't alert more than once per hour for same stock
        long now = System.currentTimeMillis();
        long hourMs = 60 * 60 * 1000;
        return (now - stock.getLastAlertTime()) > hourMs;
    }

    private int checkAlerts(Stock stock) {
        int alerts = 0;
        long now = System.currentTimeMillis();

        if (stock.isWatchlist()) {
            // Watchlist alerts - buying/selling opportunities
            if (stock.isAlertBelowTriggered()) {
                notificationHelper.showBuyOpportunity(stock);
                db.updateAlertTime(stock.getId(), now);
                alerts++;
            }
            if (stock.isAlertAboveTriggered()) {
                notificationHelper.showSellOpportunity(stock);
                db.updateAlertTime(stock.getId(), now);
                alerts++;
            }
        } else {
            // My stocks alerts - critical drops, stop loss, targets
            double changePercent = stock.getChangePercent();

            // Critical drop
            if (stock.isCriticalDrop(defaultDropThreshold)) {
                notificationHelper.showCriticalDropAlert(stock, Math.abs(changePercent));
                db.updateAlertTime(stock.getId(), now);
                alerts++;
            }

            // Stop loss
            if (stock.isStopLossTriggered()) {
                notificationHelper.showStopLossAlert(stock);
                db.updateAlertTime(stock.getId(), now);
                alerts++;
            }

            // Target reached
            if (stock.isTargetReached()) {
                notificationHelper.showTargetReachedAlert(stock);
                db.updateAlertTime(stock.getId(), now);
                alerts++;
            }

            // Significant rise
            if (changePercent >= defaultDropThreshold) {
                notificationHelper.showRiseAlert(stock, changePercent);
                db.updateAlertTime(stock.getId(), now);
                alerts++;
            }
        }

        return alerts;
    }

    private void createServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Мониторинг акций",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Фоновый мониторинг цен акций");
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }

    private Notification createServiceNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("StockTracker")
                .setContentText("Мониторинг акций активен")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
