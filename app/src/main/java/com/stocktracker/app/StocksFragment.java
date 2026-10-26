package com.stocktracker.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StocksFragment extends Fragment implements StockAdapter.OnStockClickListener {

    private static final String ARG_IS_WATCHLIST = "is_watchlist";

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private DatabaseHelper db;
    private PriceFetcher priceFetcher;
    private boolean isWatchlist;

    public static StocksFragment newInstance(boolean isWatchlist) {
        StocksFragment fragment = new StocksFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_WATCHLIST, isWatchlist);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isWatchlist = getArguments().getBoolean(ARG_IS_WATCHLIST);
        }
        db = new DatabaseHelper(requireContext());
        priceFetcher = new PriceFetcher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stocks, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new StockAdapter(new ArrayList<>(), this, isWatchlist);
        recyclerView.setAdapter(adapter);

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Stock stock = adapter.getStockAt(position);
                db.deleteStock(stock.getId());
                adapter.removeStock(position);
                Toast.makeText(getContext(), R.string.stock_deleted, Toast.LENGTH_SHORT).show();
                checkEmpty();
            }
        };
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);

        swipeRefresh.setOnRefreshListener(this::refreshPrices);
        swipeRefresh.setColorSchemeResources(R.color.purple_500);

        loadStocks();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStocks();
    }

    private void loadStocks() {
        List<Stock> stocks;
        if (isWatchlist) {
            stocks = db.getWatchlist();
        } else {
            stocks = db.getMyStocks();
        }
        adapter.updateStocks(stocks);
        checkEmpty();
    }

    private void checkEmpty() {
        if (adapter.getItemCount() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(isWatchlist ? R.string.empty_watchlist : R.string.empty_portfolio);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void refreshPrices() {
        List<Stock> stocks = adapter.getStocks();
        if (stocks.isEmpty()) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        String[] tickers = new String[stocks.size()];
        for (int i = 0; i < stocks.size(); i++) {
            tickers[i] = stocks.get(i).getTicker();
        }

        priceFetcher.fetchBatchPrices(tickers, new PriceFetcher.BatchPriceCallback() {
            @Override
            public void onPricesFetched(Map<String, Double> prices) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    for (Stock stock : stocks) {
                        Double price = prices.get(stock.getTicker());
                        if (price != null && price > 0) {
                            double oldPrice = stock.getCurrentPrice();
                            stock.setPreviousPrice(oldPrice);
                            stock.setCurrentPrice(price);
                            db.updatePrice(stock.getId(), price, oldPrice);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onError(String error) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), "Ошибка: " + error, Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
            }
        });
    }

    @Override
    public void onStockClick(Stock stock) {
        Intent intent = new Intent(getContext(), StockDetailActivity.class);
        intent.putExtra("stock_id", stock.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (priceFetcher != null) {
            priceFetcher.shutdown();
        }
    }
}
