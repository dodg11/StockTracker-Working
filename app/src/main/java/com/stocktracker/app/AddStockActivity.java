package com.stocktracker.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class AddStockActivity extends AppCompatActivity {

    private TextInputEditText etTicker, etName, etBuyPrice, etQuantity;
    private TextInputEditText etTargetPrice, etStopLoss, etAlertBelow, etAlertAbove;
    private RadioButton rbMyStock, rbWatchlist;
    private Button btnSave, btnCancel;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        db = new DatabaseHelper(this);

        etTicker = findViewById(R.id.etTicker);
        etName = findViewById(R.id.etName);
        etBuyPrice = findViewById(R.id.etBuyPrice);
        etQuantity = findViewById(R.id.etQuantity);
        etTargetPrice = findViewById(R.id.etTargetPrice);
        etStopLoss = findViewById(R.id.etStopLoss);
        etAlertBelow = findViewById(R.id.etAlertBelow);
        etAlertAbove = findViewById(R.id.etAlertAbove);
        rbMyStock = findViewById(R.id.rbMyStock);
        rbWatchlist = findViewById(R.id.rbWatchlist);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);

        btnSave.setOnClickListener(v -> saveStock());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void saveStock() {
        String ticker = etTicker.getText().toString().trim().toUpperCase();
        String name = etName.getText().toString().trim();

        if (ticker.isEmpty()) {
            Toast.makeText(this, "Введите тикер!", Toast.LENGTH_SHORT).show();
            return;
        }

        Stock stock = new Stock();
        stock.setTicker(ticker);
        stock.setName(name.isEmpty() ? ticker : name);
        stock.setWatchlist(rbWatchlist.isChecked());

        try {
            String buyPriceStr = etBuyPrice.getText().toString().trim();
            if (!buyPriceStr.isEmpty()) {
                stock.setBuyPrice(Double.parseDouble(buyPriceStr));
            }

            String qtyStr = etQuantity.getText().toString().trim();
            if (!qtyStr.isEmpty()) {
                stock.setQuantity(Integer.parseInt(qtyStr));
            }

            String targetStr = etTargetPrice.getText().toString().trim();
            if (!targetStr.isEmpty()) {
                stock.setTargetPrice(Double.parseDouble(targetStr));
            }

            String stopLossStr = etStopLoss.getText().toString().trim();
            if (!stopLossStr.isEmpty()) {
                stock.setStopLossPercent(Double.parseDouble(stopLossStr));
            }

            String alertBelowStr = etAlertBelow.getText().toString().trim();
            if (!alertBelowStr.isEmpty()) {
                stock.setAlertBelow(Double.parseDouble(alertBelowStr));
            }

            String alertAboveStr = etAlertAbove.getText().toString().trim();
            if (!alertAboveStr.isEmpty()) {
                stock.setAlertAbove(Double.parseDouble(alertAboveStr));
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }

        long id = db.addStock(stock);
        if (id > 0) {
            Toast.makeText(this, R.string.stock_added, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }
}
