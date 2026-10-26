package com.stocktracker.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "StockTrackerPrefs";
    private static final String KEY_INTERVAL = "monitoring_interval";
    private static final String KEY_DROP_THRESHOLD = "drop_threshold";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_LAST_UPDATE = "last_update";

    private Switch switchNotifications;
    private SeekBar seekInterval, seekDropThreshold;
    private TextView tvIntervalValue, tvDropThresholdValue, tvStatus, tvLastUpdate;
    private Button btnStart, btnStop;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        switchNotifications = findViewById(R.id.switchNotifications);
        seekInterval = findViewById(R.id.seekInterval);
        seekDropThreshold = findViewById(R.id.seekDropThreshold);
        tvIntervalValue = findViewById(R.id.tvIntervalValue);
        tvDropThresholdValue = findViewById(R.id.tvDropThresholdValue);
        tvStatus = findViewById(R.id.tvMonitoringStatus);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        btnStart = findViewById(R.id.btnStartMonitoring);
        btnStop = findViewById(R.id.btnStopMonitoring);

        loadSettings();
        setupListeners();
        updateStatus();
    }

    private void loadSettings() {
        long interval = prefs.getLong(KEY_INTERVAL, 5);
        float threshold = prefs.getFloat(KEY_DROP_THRESHOLD, 5.0f);
        boolean notifEnabled = prefs.getBoolean(KEY_NOTIFICATIONS, true);

        seekInterval.setProgress((int) interval - 1);
        seekDropThreshold.setProgress((int) threshold - 1);
        switchNotifications.setChecked(notifEnabled);

        updateIntervalText(interval);
        updateThresholdText(threshold);
    }

    private void setupListeners() {
        seekInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long interval = progress + 1;
                updateIntervalText(interval);
                prefs.edit().putLong(KEY_INTERVAL, interval).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekDropThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float threshold = progress + 1;
                updateThresholdText(threshold);
                prefs.edit().putFloat(KEY_DROP_THRESHOLD, threshold).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_NOTIFICATIONS, isChecked).apply();
        });

        btnStart.setOnClickListener(v -> startMonitoring());
        btnStop.setOnClickListener(v -> stopMonitoring());
    }

    private void updateIntervalText(long minutes) {
        if (minutes < 60) {
            tvIntervalValue.setText(minutes + " минут");
        } else {
            long hours = minutes / 60;
            long mins = minutes % 60;
            if (mins == 0) {
                tvIntervalValue.setText(hours + " ч.");
            } else {
                tvIntervalValue.setText(hours + " ч. " + mins + " мин.");
            }
        }
    }

    private void updateThresholdText(float threshold) {
        tvDropThresholdValue.setText(String.format(Locale.US, "%.0f%%", threshold));
    }

    private void startMonitoring() {
        Intent serviceIntent = new Intent(this, StockMonitorService.class);

        // Для Android 8+ нужно использовать startForegroundService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        Toast.makeText(this, R.string.monitoring_active, Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void stopMonitoring() {
        Intent serviceIntent = new Intent(this, StockMonitorService.class);
        stopService(serviceIntent);
        Toast.makeText(this, R.string.monitoring_stopped, Toast.LENGTH_SHORT).show();
        updateStatus();
    }

    private void updateStatus() {
        // Check if service is running
        android.app.ActivityManager manager = (android.app.ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;
        for (android.app.ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StockMonitorService.class.getName().equals(service.service.getClassName())) {
                isRunning = true;
                break;
            }
        }

        tvStatus.setText("Статус: " + (isRunning ? "активен ✅" : "остановлен ❌"));

        long lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0);
        if (lastUpdate > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
            tvLastUpdate.setText("Последнее обновление: " + sdf.format(new Date(lastUpdate)));
        } else {
            tvLastUpdate.setText("Последнее обновление: —");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
