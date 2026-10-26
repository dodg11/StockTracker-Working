package com.stocktracker.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "stock_alerts_channel";
    private static final String CHANNEL_NAME = "Оповещения об акциях";
    private static final String CHANNEL_DESC = "Уведомления о критических изменениях цен акций";
    private static final int NOTIFICATION_ID_BASE = 1000;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            channel.setShowBadge(true);
            // Для Android 8.0+
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showCriticalDropAlert(Stock stock, double dropPercent) {
        String title = String.format("⚠️ Критическое падение: %s", stock.getTicker());
        String body = String.format("%s упала на %.2f%%! Текущая цена: $%.2f",
                stock.getName(), dropPercent, stock.getCurrentPrice());

        showNotification(title, body, stock.getId(), NotificationCompat.PRIORITY_HIGH, true);
    }

    public void showStopLossAlert(Stock stock) {
        String title = String.format("🛑 Стоп-лосс: %s", stock.getTicker());
        String body = String.format("%s упала ниже стоп-лосса! Цена: $%.2f (покупка: $%.2f)",
                stock.getName(), stock.getCurrentPrice(), stock.getBuyPrice());

        showNotification(title, body, stock.getId() + 10000, NotificationCompat.PRIORITY_HIGH, true);
    }

    public void showTargetReachedAlert(Stock stock) {
        String title = String.format("🎯 Цель достигнута: %s", stock.getTicker());
        String body = String.format("%s достигла $%.2f (цель: $%.2f)",
                stock.getName(), stock.getCurrentPrice(), stock.getTargetPrice());

        showNotification(title, body, stock.getId() + 20000, NotificationCompat.PRIORITY_HIGH, false);
    }

    public void showRiseAlert(Stock stock, double risePercent) {
        String title = String.format("📈 Рост: %s", stock.getTicker());
        String body = String.format("%s выросла на %.2f%%! Текущая цена: $%.2f",
                stock.getName(), risePercent, stock.getCurrentPrice());

        showNotification(title, body, stock.getId() + 30000, NotificationCompat.PRIORITY_DEFAULT, false);
    }

    public void showBuyOpportunity(Stock stock) {
        String title = String.format("💡 Возможность покупки: %s", stock.getTicker());
        String body = String.format("%s опустилась до $%.2f — ниже целевой $%.2f",
                stock.getName(), stock.getCurrentPrice(), stock.getAlertBelow());

        showNotification(title, body, stock.getId() + 40000, NotificationCompat.PRIORITY_HIGH, true);
    }

    public void showSellOpportunity(Stock stock) {
        String title = String.format("💰 Возможность продажи: %s", stock.getTicker());
        String body = String.format("%s поднялась до $%.2f — выше целевой $%.2f",
                stock.getName(), stock.getCurrentPrice(), stock.getAlertAbove());

        showNotification(title, body, stock.getId() + 50000, NotificationCompat.PRIORITY_HIGH, true);
    }

    public void showPriceUpdateSummary(int stocksUpdated, int alertsTriggered) {
        if (alertsTriggered == 0) return;

        String title = "📊 Обновление портфеля";
        String body = String.format("Обновлено %d акций, %d сигналов", stocksUpdated, alertsTriggered);

        showNotification(title, body, 999999, NotificationCompat.PRIORITY_DEFAULT, false);
    }

    private void showNotification(String title, String body, int id, int priority, boolean critical) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (critical) {
            builder.setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVibrate(new long[]{0, 500, 200, 500, 200, 500});
        }

        notificationManager.notify(NOTIFICATION_ID_BASE + (int) id, builder.build());
    }

    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
