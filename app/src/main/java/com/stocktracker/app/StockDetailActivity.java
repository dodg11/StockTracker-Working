package com.stocktracker.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StockDetailActivity extends AppCompatActivity {

    private long stockId;
    private DatabaseHelper db;
    private Stock stock;

    private TextView tvTicker, tvName, tvPrice, tvChange;
    private TextView tvBuyPrice, tvQuantity, tvInvested, tvCurrentValue, tvPL;
    private TextView tvTarget, tvStopLoss, tvAlertBelow, tvAlertAbove;
    private Button btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        db = new DatabaseHelper(this);
        stockId = getIntent().getLongExtra("stock_id", -1);

        initViews();
        loadStock();
    }

    private void initViews() {
        tvTicker = findViewById(R.id.tvDetailTicker);
        tvName = findViewById(R.id.tvDetailName);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvChange = findViewById(R.id.tvDetailChange);
        tvBuyPrice = findViewById(R.id.tvDetailBuyPrice);
        tvQuantity = findViewById(R.id.tvDetailQuantity);
        tvInvested = findViewById(R.id.tvDetailInvested);
        tvCurrentValue = findViewById(R.id.tvDetailCurrentValue);
        tvPL = findViewById(R.id.tvDetailPL);
        tvTarget = findViewById(R.id.tvDetailTarget);
        tvStopLoss = findViewById(R.id.tvDetailStopLoss);
        tvAlertBelow = findViewById(R.id.tvDetailAlertBelow);
        tvAlertAbove = findViewById(R.id.tvDetailAlertAbove);
        btnDelete = findViewById(R.id.btnDeleteStock);
    }

    private void loadStock() {
        stock = db.getStock(stockId);
        if (stock == null) {
            finish();
            return;
        }

        tvTicker.setText(stock.getTicker());
        tvName.setText(stock.getName());
        tvPrice.setText(String.format(Locale.US, "$%.2f", stock.getCurrentPrice()));

        double changePercent = stock.getChangePercent();
        double changeAbs = stock.getCurrentPrice() - stock.getPreviousPrice();
        if (changePercent >= 0) {
            tvChange.setText(String.format(Locale.US, "+%.2f%% (+%.2f)", changePercent, changeAbs));
            tvChange.setTextColor(getColor(R.color.green));
        } else {
            tvChange.setText(String.format(Locale.US, "%.2f%% (%.2f)", changePercent, changeAbs));
            tvChange.setTextColor(getColor(R.color.red));
        }

        tvBuyPrice.setText(String.format(Locale.US, "$%.2f", stock.getBuyPrice()));
        tvQuantity.setText(String.valueOf(stock.getQuantity()));
        tvInvested.setText(String.format(Locale.US, "$%.2f", stock.getTotalInvested()));
        tvCurrentValue.setText(String.format(Locale.US, "$%.2f", stock.getTotalValue()));

        double pl = stock.getProfitLoss();
        double plPercent = stock.getChangeFromBuy();
        if (pl >= 0) {
            tvPL.setText(String.format(Locale.US, "+%.2f (+%.1f%%)", pl, plPercent));
            tvPL.setTextColor(getColor(R.color.green));
        } else {
            tvPL.setText(String.format(Locale.US, "%.2f (%.1f%%)", pl, plPercent));
            tvPL.setTextColor(getColor(R.color.red));
        }

        // Alerts info
        if (stock.getTargetPrice() > 0) {
            tvTarget.setText(String.format(Locale.US, "🎯 Целевая цена: $%.2f", stock.getTargetPrice()));
        } else {
            tvTarget.setText("🎯 Целевая цена: не установлена");
        }

        if (stock.getStopLossPercent() > 0) {
            double stopPrice = stock.getBuyPrice() * (1 - stock.getStopLossPercent() / 100);
            tvStopLoss.setText(String.format(Locale.US, "🛑 Стоп-лосс: -%.1f%% ($%.2f)",
                    stock.getStopLossPercent(), stopPrice));
        } else {
            tvStopLoss.setText("🛑 Стоп-лосс: не установлен");
        }

        if (stock.getAlertBelow() > 0) {
            tvAlertBelow.setText(String.format(Locale.US, "📉 Уведомить ниже: $%.2f", stock.getAlertBelow()));
        } else {
            tvAlertBelow.setText("📉 Уведомить ниже: не установлено");
        }

        if (stock.getAlertAbove() > 0) {
            tvAlertAbove.setText(String.format(Locale.US, "📈 Уведомить выше: $%.2f", stock.getAlertAbove()));
        } else {
            tvAlertAbove.setText("📈 Уведомить выше: не установлено");
        }

        btnDelete.setOnClickListener(v -> showDeleteDialog());
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Удалить акцию")
                .setMessage("Удалить " + stock.getTicker() + " из списка?")
                .setPositiveButton("Удалить", (dialog, which) -> {
                    db.deleteStock(stockId);
                    Toast.makeText(this, R.string.stock_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Отмена", null)
                .show();
    }
}
