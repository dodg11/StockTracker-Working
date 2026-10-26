package com.stocktracker.app;

public class Stock {
    private long id;
    private String ticker;
    private String name;
    private double buyPrice;
    private int quantity;
    private double targetPrice;
    private double stopLossPercent;
    private double alertBelow;
    private double alertAbove;
    private boolean isWatchlist;
    private double currentPrice;
    private double previousPrice;
    private long lastAlertTime;
    private long lastUpdateTime;

    public Stock() {
        this.lastAlertTime = 0;
        this.lastUpdateTime = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(double targetPrice) { this.targetPrice = targetPrice; }

    public double getStopLossPercent() { return stopLossPercent; }
    public void setStopLossPercent(double stopLossPercent) { this.stopLossPercent = stopLossPercent; }

    public double getAlertBelow() { return alertBelow; }
    public void setAlertBelow(double alertBelow) { this.alertBelow = alertBelow; }

    public double getAlertAbove() { return alertAbove; }
    public void setAlertAbove(double alertAbove) { this.alertAbove = alertAbove; }

    public boolean isWatchlist() { return isWatchlist; }
    public void setWatchlist(boolean watchlist) { isWatchlist = watchlist; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) {
        this.previousPrice = this.currentPrice;
        this.currentPrice = currentPrice;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public double getPreviousPrice() { return previousPrice; }
    public void setPreviousPrice(double previousPrice) { this.previousPrice = previousPrice; }

    public long getLastAlertTime() { return lastAlertTime; }
    public void setLastAlertTime(long lastAlertTime) { this.lastAlertTime = lastAlertTime; }

    public long getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    public double getChangePercent() {
        if (previousPrice > 0) {
            return ((currentPrice - previousPrice) / previousPrice) * 100;
        }
        return 0;
    }

    public double getChangeFromBuy() {
        if (buyPrice > 0) {
            return ((currentPrice - buyPrice) / buyPrice) * 100;
        }
        return 0;
    }

    public double getProfitLoss() {
        return (currentPrice - buyPrice) * quantity;
    }

    public double getTotalValue() {
        return currentPrice * quantity;
    }

    public double getTotalInvested() {
        return buyPrice * quantity;
    }

    public boolean isStopLossTriggered() {
        if (stopLossPercent > 0 && buyPrice > 0) {
            double stopPrice = buyPrice * (1 - stopLossPercent / 100);
            return currentPrice <= stopPrice;
        }
        return false;
    }

    public boolean isTargetReached() {
        return targetPrice > 0 && currentPrice >= targetPrice;
    }

    public boolean isAlertBelowTriggered() {
        return alertBelow > 0 && currentPrice <= alertBelow;
    }

    public boolean isAlertAboveTriggered() {
        return alertAbove > 0 && currentPrice >= alertAbove;
    }

    public boolean isCriticalDrop(double thresholdPercent) {
        double change = getChangePercent();
        return change <= -thresholdPercent;
    }
}
