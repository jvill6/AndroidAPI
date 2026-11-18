package com.example.myapidemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StocksActivity extends AppCompatActivity {

    private final List<ApiModels.FishStock> stocks = new ArrayList<>();
    private final List<ApiModels.FishSpecies> species = new ArrayList<>();
    private final List<ApiModels.FishTank> tanks = new ArrayList<>();
    private final List<ApiModels.FishGrowth> growth = new ArrayList<>();
    private LinearLayout stockList;
    private EditText searchField;
    private Integer selectedTankId;
    private String selectedStage;
    private boolean stocksLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stocks_page);

        NavigationHelper.setupBottomNavigation(this, R.id.tab_Stocks);
        View headerProfile = findViewById(R.id.header_profile);
        if (headerProfile != null) {
            headerProfile.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }

        stockList = findViewById(R.id.stock_list_container);
        searchField = findViewById(R.id.edt_search);
        showMessage("Loading stocks...");
        setupFilters();
        View manageTanksButton = findViewById(R.id.btn_manage_tanks);
        if (manageTanksButton != null) {
            manageTanksButton.setOnClickListener(v ->
                    startActivity(new Intent(this, TankManagementActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ApiService api = RetrofitClient.getApiService();
        api.getStocks(ApiUtils.listQuery("stocks")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishStock>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FishStock>>> call,
                    Response<ApiEnvelope<List<ApiModels.FishStock>>> response
            ) {
                ApiEnvelope<List<ApiModels.FishStock>> body = validBody(response);
                if (body != null) {
                    stocks.clear();
                    stocks.addAll(body.data);
                    stocksLoaded = true;
                    renderStocks();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishStock>>> call, Throwable error) {
                ApiUtils.showFailure(StocksActivity.this, error);
                showMessage("Stocks could not be loaded.");
            }
        });

        api.getSpecies(ApiUtils.listQuery("species")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishSpecies>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call,
                    Response<ApiEnvelope<List<ApiModels.FishSpecies>>> response
            ) {
                ApiEnvelope<List<ApiModels.FishSpecies>> body = validBody(response);
                if (body != null) {
                    species.clear();
                    species.addAll(body.data);
                    renderStocks();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call, Throwable error) {
                ApiUtils.showFailure(StocksActivity.this, error);
            }
        });

        api.getTanks(ApiUtils.listQuery("tanks")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishTank>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FishTank>>> call,
                    Response<ApiEnvelope<List<ApiModels.FishTank>>> response
            ) {
                ApiEnvelope<List<ApiModels.FishTank>> body = validBody(response);
                if (body != null) {
                    tanks.clear();
                    tanks.addAll(body.data);
                    renderStocks();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishTank>>> call, Throwable error) {
                ApiUtils.showFailure(StocksActivity.this, error);
            }
        });

        api.getGrowth(ApiUtils.listQuery("growth")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishGrowth>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call,
                    Response<ApiEnvelope<List<ApiModels.FishGrowth>>> response
            ) {
                ApiEnvelope<List<ApiModels.FishGrowth>> body = validBody(response);
                if (body != null) {
                    growth.clear();
                    growth.addAll(body.data);
                    renderStocks();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call, Throwable error) {
                ApiUtils.showFailure(StocksActivity.this, error);
            }
        });
    }

    private <T> ApiEnvelope<List<T>> validBody(Response<ApiEnvelope<List<T>>> response) {
        if (!response.isSuccessful() || response.body() == null) {
            ApiUtils.showFailure(this, response);
            return null;
        }
        ApiEnvelope<List<T>> body = response.body();
        if (!"success".equalsIgnoreCase(body.status) || body.data == null) {
            ApiUtils.showFailure(this, response);
            return null;
        }
        return body;
    }

    private void setupFilters() {
        if (searchField != null) {
            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    renderStocks();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        Button stageButton = findViewById(R.id.btn_filter_stage);
        if (stageButton != null) {
            stageButton.setOnClickListener(v -> {
                List<String> stages = new ArrayList<>();
                for (ApiModels.FishGrowth item : growth) {
                    if (item.growth_stage != null && !stages.contains(item.growth_stage)) {
                        stages.add(item.growth_stage);
                    }
                }
                if (stages.isEmpty()) {
                    showMessage("No growth stages are available.");
                    return;
                }
                stages.add(0, "All stages");
                String[] options = stages.toArray(new String[0]);
                new AlertDialog.Builder(this).setTitle("Growth Stage")
                        .setItems(options, (dialog, index) -> {
                            selectedStage = index == 0 ? null : options[index];
                            stageButton.setText(selectedStage == null ? "Growth Stage" : selectedStage);
                            renderStocks();
                        }).show();
            });
        }

        Button tankButton = findViewById(R.id.btn_filter_tank);
        if (tankButton != null) {
            tankButton.setOnClickListener(v -> {
                List<String> options = new ArrayList<>();
                options.add("All tanks");
                for (ApiModels.FishTank tank : tanks) {
                    options.add(tank.tank_name == null ? "Tank " + tank.tank_id : tank.tank_name);
                }
                new AlertDialog.Builder(this).setTitle("Tank")
                        .setItems(options.toArray(new String[0]), (dialog, index) -> {
                            selectedTankId = index == 0 ? null : tanks.get(index - 1).tank_id;
                            tankButton.setText(index == 0 ? "Tank" : options.get(index));
                            renderStocks();
                        }).show();
            });
        }
    }

    private void renderStocks() {
        if (stockList == null) {
            return;
        }
        if (!stocksLoaded) {
            return;
        }
        if (stocks.isEmpty()) {
            showMessage("No stocks found.");
            return;
        }
        stockList.removeAllViews();
        int displayed = 0;
        String query = searchField == null ? "" : searchField.getText().toString().trim().toLowerCase(Locale.ROOT);
        for (ApiModels.FishStock stock : stocks) {
            ApiModels.FishSpecies fishSpecies = findSpecies(stock.species_id);
            ApiModels.FishTank tank = findTank(stock.tank_id);
            ApiModels.FishGrowth stage = findGrowth(stock.growth_id);
            String speciesName = fishSpecies == null ? "Species " + stock.species_id : safe(fishSpecies.species_name, fishSpecies.scientific_name);
            String tankName = tank == null ? "Tank " + stock.tank_id : safe(tank.tank_name, "Tank " + tank.tank_id);
            String stageName = stage == null ? "Growth stage unavailable" : safe(stage.growth_stage, "");
            String stockName = safe(stock.stock_name, speciesName);

            if (selectedTankId != null && stock.tank_id != selectedTankId) {
                continue;
            }
            if (selectedStage != null && !selectedStage.equalsIgnoreCase(stageName)) {
                continue;
            }
            String searchable = (stockName + " " + speciesName + " " + tankName + " " + stageName)
                    .toLowerCase(Locale.ROOT);
            if (!query.isEmpty() && !searchable.contains(query)) {
                continue;
            }
            stockList.addView(createStockCard(stock, stockName, speciesName, stageName, tankName));
            displayed++;
        }
        if (displayed == 0) {
            showMessage("No stocks match your filters.");
        }
    }

    private View createStockCard(
            ApiModels.FishStock stock,
            String stockName,
            String speciesName,
            String stageName,
            String tankName
    ) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.white_background);
        card.setClickable(true);
        card.setFocusable(true);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.topMargin = dp(12);
        card.setLayoutParams(cardParams);

        TextView title = makeText(stockName, 18, true, Color.BLACK);
        card.addView(title);
        card.addView(makeText(speciesName + "  •  " + stock.stock_quantity + " pcs", 14, false, Color.DKGRAY));
        card.addView(makeText(stageName + "  •  " + tankName, 13, false, Color.DKGRAY));
        Button removeButton = new Button(this);
        removeButton.setText("Remove Stock");
        removeButton.setAllCaps(false);
        removeButton.setOnClickListener(v -> confirmRemoveStock(stock));
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        removeParams.gravity = Gravity.END;
        card.addView(removeButton, removeParams);
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, StockDisplayActivity.class);
            intent.putExtra("stock_id", stock.stock_id);
            intent.putExtra("growth_id", stock.growth_id);
            intent.putExtra("species_id", stock.species_id);
            intent.putExtra("tank_id", stock.tank_id);
            intent.putExtra("stock_name", stockName);
            intent.putExtra("species_name", speciesName);
            intent.putExtra("growth_stage", stageName);
            intent.putExtra("tank_name", tankName);
            intent.putExtra("date_added", safe(stock.date_added, ""));
            intent.putExtra("stock_quantity", stock.stock_quantity);
            startActivity(intent);
        });
        return card;
    }

    private void confirmRemoveStock(ApiModels.FishStock stock) {
        new AlertDialog.Builder(this)
                .setTitle("Remove stock?")
                .setMessage("Remove \"" + stock.stock_name
                        + "\"? Stocks linked to feeding schedules or records may not be removable.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove", (dialog, which) -> removeStock(stock))
                .show();
    }

    private void removeStock(ApiModels.FishStock stock) {
        RetrofitClient.getApiService().deleteStock("stocks", stock.stock_id)
                .enqueue(new Callback<ApiEnvelope<Void>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<Void>> call,
                            Response<ApiEnvelope<Void>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !"success".equalsIgnoreCase(response.body().status)) {
                            ApiUtils.showFailure(StocksActivity.this, response);
                            return;
                        }
                        stocks.remove(stock);
                        renderStocks();
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<Void>> call, Throwable error) {
                        ApiUtils.showFailure(StocksActivity.this, error);
                    }
                });
    }

    private TextView makeText(String text, int sizeSp, boolean bold, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        view.setPadding(0, dp(2), 0, dp(2));
        return view;
    }

    private ApiModels.FishSpecies findSpecies(int id) {
        for (ApiModels.FishSpecies item : species) {
            if (item.species_id == id) return item;
        }
        return null;
    }

    private ApiModels.FishTank findTank(int id) {
        for (ApiModels.FishTank item : tanks) {
            if (item.tank_id == id) return item;
        }
        return null;
    }

    private ApiModels.FishGrowth findGrowth(int id) {
        for (ApiModels.FishGrowth item : growth) {
            if (item.growth_id == id) return item;
        }
        return null;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void showMessage(String message) {
        if (stockList == null) return;
        stockList.removeAllViews();
        TextView view = makeText(message, 14, false, Color.DKGRAY);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(16), dp(24), dp(16), dp(24));
        stockList.addView(view);
    }
}
