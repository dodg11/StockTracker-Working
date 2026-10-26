package com.stocktracker.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    public interface OnStockClickListener {
        void onStockClick(Stock stock);
    }

    private List<Stock> stocks;
    private final OnStockClickListener listener;
    private final boolean isWatchlist;

    public StockAdapter(List<Stock> stocks, OnStockClickListener listener, boolean isWatchlist) {
        this.stocks = stocks;
        this.listener = listener;
        this.isWatchlist = isWatchlist;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = stocks.get(position);

        holder.tvTicker.setText(stock.getTicker());
        holder.tvName.setText(stock.getName());

        if (isWatchlist) {
            holder.tvBuyInfo.setText("Наблюдение");
            holder.tvBuyInfo.setTextColor(holder.itemView.getContext().getColor(R.color.accent_orange));
        } else {
            holder.tvBuyInfo.setText(String.format(Locale.US,
                    "Куплено: $%.2f x %d", stock.getBuyPrice(), stock.getQuantity()));
            holder.tvBuyInfo.setTextColor(holder.itemView.getContext().getColor(R.color.text_secondary));
        }

        // Alert info
        StringBuilder alertInfo = new StringBuilder();
        if (stock.getTargetPrice() > 0) {
            alertInfo.append(String.format(Locale.US, "🎯 $%.0f  ", stock.getTargetPrice()));
        }
        if (stock.getStopLossPercent() > 0) {
            alertInfo.append(String.format(Locale.US, "🛑 -%.0f%%  ", stock.getStopLossPercent()));
        }
        if (stock.getAlertBelow() > 0) {
            alertInfo.append(String.format(Locale.US, "📉 $%.0f  ", stock.getAlertBelow()));
        }
        if (stock.getAlertAbove() > 0) {
            alertInfo.append(String.format(Locale.US, "📈 $%.0f", stock.getAlertAbove()));
        }
        holder.tvAlertInfo.setText(alertInfo.toString());

        // Price
        holder.tvPrice.setText(String.format(Locale.US, "$%.2f", stock.getCurrentPrice()));

        // Change
        double changePercent = stock.getChangePercent();
        double changeAbs = stock.getCurrentPrice() - stock.getPreviousPrice();

        if (changePercent >= 0) {
            holder.tvChange.setText(String.format(Locale.US, "+%.2f%% (+%.2f)", changePercent, changeAbs));
            holder.tvChange.setTextColor(Color.parseColor("#4CAF50"));
        } else {
            holder.tvChange.setText(String.format(Locale.US, "%.2f%% (%.2f)", changePercent, changeAbs));
            holder.tvChange.setTextColor(Color.parseColor("#E53935"));
        }

        // P/L for my stocks
        if (!isWatchlist && stock.getBuyPrice() > 0) {
            double pl = stock.getProfitLoss();
            double plPercent = stock.getChangeFromBuy();
            if (pl >= 0) {
                holder.tvProfitLoss.setText(String.format(Locale.US, "+%.2f (+%.1f%%)", pl, plPercent));
                holder.tvProfitLoss.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                holder.tvProfitLoss.setText(String.format(Locale.US, "%.2f (%.1f%%)", pl, plPercent));
                holder.tvProfitLoss.setTextColor(Color.parseColor("#E53935"));
            }
        } else {
            holder.tvProfitLoss.setText("");
        }

        // Critical drop highlight
        if (stock.isCriticalDrop(5.0)) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#3D1E1E"));
        } else if (stock.isStopLossTriggered()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#3D2E1E"));
        } else {
            holder.cardView.setCardBackgroundColor(holder.itemView.getContext().getColor(R.color.dark_card));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStockClick(stock);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stocks.size();
    }

    public void updateStocks(List<Stock> newStocks) {
        this.stocks = newStocks;
        notifyDataSetChanged();
    }

    public Stock getStockAt(int position) {
        return stocks.get(position);
    }

    public void removeStock(int position) {
        stocks.remove(position);
        notifyItemRemoved(position);
    }

    public List<Stock> getStocks() {
        return stocks;
    }

    static class StockViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTicker, tvName, tvBuyInfo, tvAlertInfo;
        TextView tvPrice, tvChange, tvProfitLoss;

        StockViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvTicker = itemView.findViewById(R.id.tvTicker);
            tvName = itemView.findViewById(R.id.tvName);
            tvBuyInfo = itemView.findViewById(R.id.tvBuyInfo);
            tvAlertInfo = itemView.findViewById(R.id.tvAlertInfo);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvChange = itemView.findViewById(R.id.tvChange);
            tvProfitLoss = itemView.findViewById(R.id.tvProfitLoss);
        }
    }
}
