package com.stocktracker.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "stock_tracker.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_STOCKS = "stocks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TICKER = "ticker";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BUY_PRICE = "buy_price";
    private static final String COLUMN_QUANTITY = "quantity";
    private static final String COLUMN_TARGET_PRICE = "target_price";
    private static final String COLUMN_STOP_LOSS = "stop_loss_percent";
    private static final String COLUMN_ALERT_BELOW = "alert_below";
    private static final String COLUMN_ALERT_ABOVE = "alert_above";
    private static final String COLUMN_IS_WATCHLIST = "is_watchlist";
    private static final String COLUMN_CURRENT_PRICE = "current_price";
    private static final String COLUMN_PREVIOUS_PRICE = "previous_price";
    private static final String COLUMN_LAST_ALERT = "last_alert_time";
    private static final String COLUMN_LAST_UPDATE = "last_update_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_STOCKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TICKER + " TEXT NOT NULL,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_BUY_PRICE + " REAL DEFAULT 0,"
                + COLUMN_QUANTITY + " INTEGER DEFAULT 0,"
                + COLUMN_TARGET_PRICE + " REAL DEFAULT 0,"
                + COLUMN_STOP_LOSS + " REAL DEFAULT 0,"
                + COLUMN_ALERT_BELOW + " REAL DEFAULT 0,"
                + COLUMN_ALERT_ABOVE + " REAL DEFAULT 0,"
                + COLUMN_IS_WATCHLIST + " INTEGER DEFAULT 0,"
                + COLUMN_CURRENT_PRICE + " REAL DEFAULT 0,"
                + COLUMN_PREVIOUS_PRICE + " REAL DEFAULT 0,"
                + COLUMN_LAST_ALERT + " INTEGER DEFAULT 0,"
                + COLUMN_LAST_UPDATE + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        onCreate(db);
    }

    public long addStock(Stock stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TICKER, stock.getTicker());
        values.put(COLUMN_NAME, stock.getName());
        values.put(COLUMN_BUY_PRICE, stock.getBuyPrice());
        values.put(COLUMN_QUANTITY, stock.getQuantity());
        values.put(COLUMN_TARGET_PRICE, stock.getTargetPrice());
        values.put(COLUMN_STOP_LOSS, stock.getStopLossPercent());
        values.put(COLUMN_ALERT_BELOW, stock.getAlertBelow());
        values.put(COLUMN_ALERT_ABOVE, stock.getAlertAbove());
        values.put(COLUMN_IS_WATCHLIST, stock.isWatchlist() ? 1 : 0);
        values.put(COLUMN_CURRENT_PRICE, stock.getCurrentPrice());
        values.put(COLUMN_PREVIOUS_PRICE, stock.getPreviousPrice());
        values.put(COLUMN_LAST_ALERT, stock.getLastAlertTime());
        values.put(COLUMN_LAST_UPDATE, stock.getLastUpdateTime());

        long id = db.insert(TABLE_STOCKS, null, values);
        db.close();
        return id;
    }

    public Stock getStock(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STOCKS, null, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) cursor.moveToFirst();

        Stock stock = cursorToStock(cursor);
        if (cursor != null) cursor.close();
        db.close();
        return stock;
    }

    public List<Stock> getAllStocks() {
        List<Stock> stockList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS + " ORDER BY " + COLUMN_TICKER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                stockList.add(cursorToStock(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stockList;
    }

    public List<Stock> getMyStocks() {
        List<Stock> stockList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS + " WHERE " + COLUMN_IS_WATCHLIST + "=0 ORDER BY " + COLUMN_TICKER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                stockList.add(cursorToStock(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stockList;
    }

    public List<Stock> getWatchlist() {
        List<Stock> stockList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STOCKS + " WHERE " + COLUMN_IS_WATCHLIST + "=1 ORDER BY " + COLUMN_TICKER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                stockList.add(cursorToStock(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return stockList;
    }

    public int updateStock(Stock stock) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TICKER, stock.getTicker());
        values.put(COLUMN_NAME, stock.getName());
        values.put(COLUMN_BUY_PRICE, stock.getBuyPrice());
        values.put(COLUMN_QUANTITY, stock.getQuantity());
        values.put(COLUMN_TARGET_PRICE, stock.getTargetPrice());
        values.put(COLUMN_STOP_LOSS, stock.getStopLossPercent());
        values.put(COLUMN_ALERT_BELOW, stock.getAlertBelow());
        values.put(COLUMN_ALERT_ABOVE, stock.getAlertAbove());
        values.put(COLUMN_IS_WATCHLIST, stock.isWatchlist() ? 1 : 0);
        values.put(COLUMN_CURRENT_PRICE, stock.getCurrentPrice());
        values.put(COLUMN_PREVIOUS_PRICE, stock.getPreviousPrice());
        values.put(COLUMN_LAST_ALERT, stock.getLastAlertTime());
        values.put(COLUMN_LAST_UPDATE, stock.getLastUpdateTime());

        int result = db.update(TABLE_STOCKS, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(stock.getId())});
        db.close();
        return result;
    }

    public void deleteStock(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STOCKS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updatePrice(long id, double currentPrice, double previousPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CURRENT_PRICE, currentPrice);
        values.put(COLUMN_PREVIOUS_PRICE, previousPrice);
        values.put(COLUMN_LAST_UPDATE, System.currentTimeMillis());
        db.update(TABLE_STOCKS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateAlertTime(long id, long alertTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_ALERT, alertTime);
        db.update(TABLE_STOCKS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private Stock cursorToStock(Cursor cursor) {
        Stock stock = new Stock();
        stock.setId(cursor.getLong(0));
        stock.setTicker(cursor.getString(1));
        stock.setName(cursor.getString(2));
        stock.setBuyPrice(cursor.getDouble(3));
        stock.setQuantity(cursor.getInt(4));
        stock.setTargetPrice(cursor.getDouble(5));
        stock.setStopLossPercent(cursor.getDouble(6));
        stock.setAlertBelow(cursor.getDouble(7));
        stock.setAlertAbove(cursor.getDouble(8));
        stock.setWatchlist(cursor.getInt(9) == 1);
        stock.setCurrentPrice(cursor.getDouble(10));
        stock.setPreviousPrice(cursor.getDouble(11));
        stock.setLastAlertTime(cursor.getLong(12));
        stock.setLastUpdateTime(cursor.getLong(13));
        return stock;
    }

    public int getStockCount() {
        String countQuery = "SELECT * FROM " + TABLE_STOCKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
}
