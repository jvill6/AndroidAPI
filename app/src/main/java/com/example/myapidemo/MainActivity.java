package com.example.myapidemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private final List<ApiModels.FishStock> stocks = new ArrayList<>();
    private final List<ApiModels.FishTank> tanks = new ArrayList<>();
    private final List<ApiModels.FishSpecies> species = new ArrayList<>();
    private final List<ApiModels.FishGrowth> growth = new ArrayList<>();
    private final List<ApiModels.FeedingSchedule> schedules = new ArrayList<>();
    private final List<ApiModels.FeedingRecord> records = new ArrayList<>();
    private TextView stockName;
    private TextView population;
    private TextView nextFeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationHelper.setupBottomNavigation(this, R.id.tab_Dashboard);
        View headerProfile = findViewById(R.id.header_profile);
        if (headerProfile != null) {
            headerProfile.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
        TextView tvViewAll = findViewById(R.id.tv_view_all);
        if (tvViewAll != null) {
            tvViewAll.setOnClickListener(v -> startActivity(new Intent(this, StocksActivity.class)));
        }

        stockName = findViewById(R.id.dashboard_stock_name);
        population = findViewById(R.id.dashboard_population);
        nextFeed = findViewById(R.id.dashboard_next_feed);
        stockName.setText("Loading stocks...");
        population.setText("--");
        nextFeed.setText("Loading...");
        ((TextView) findViewById(R.id.dashboard_active_tank_count)).setText("--");
        findViewById(R.id.card_koi_group).setOnClickListener(v -> openFirstStock());
        findViewById(R.id.recent_activity_1).setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class)));
        findViewById(R.id.recent_activity_2).setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class)));

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }

    private void loadDashboardData() {
        ApiService api = RetrofitClient.getApiService();
        api.getStocks(ApiUtils.listQuery("stocks")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishStock>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ApiModels.FishStock>>> call,
                                   Response<ApiEnvelope<List<ApiModels.FishStock>>> response) {
                ApiEnvelope<List<ApiModels.FishStock>> result = validBody(response);
                if (result != null) {
                    stocks.clear();
                    stocks.addAll(result.data);
                    renderStockCard();
                    loadSchedules();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishStock>>> call, Throwable error) {
                ApiUtils.showFailure(MainActivity.this, error);
            }
        });

        api.getTanks(ApiUtils.listQuery("tanks")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishTank>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ApiModels.FishTank>>> call,
                                   Response<ApiEnvelope<List<ApiModels.FishTank>>> response) {
                ApiEnvelope<List<ApiModels.FishTank>> result = validBody(response);
                if (result != null) {
                    tanks.clear();
                    tanks.addAll(result.data);
                    int active = 0;
                    for (ApiModels.FishTank tank : tanks) {
                        if (tank.tank_status == 1) active++;
                    }
                    ((TextView) findViewById(R.id.dashboard_active_tank_count)).setText(String.valueOf(active));
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishTank>>> call, Throwable error) {
                ApiUtils.showFailure(MainActivity.this, error);
            }
        });

        api.getSpecies(ApiUtils.listQuery("species")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishSpecies>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call,
                                   Response<ApiEnvelope<List<ApiModels.FishSpecies>>> response) {
                ApiEnvelope<List<ApiModels.FishSpecies>> result = validBody(response);
                if (result != null) {
                    species.clear();
                    species.addAll(result.data);
                    renderStockCard();
                    renderRecentRecords();
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call, Throwable error) {
                ApiUtils.showFailure(MainActivity.this, error);
            }
        });

        api.getGrowth(ApiUtils.listQuery("growth")).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishGrowth>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call,
                                   Response<ApiEnvelope<List<ApiModels.FishGrowth>>> response) {
                ApiEnvelope<List<ApiModels.FishGrowth>> result = validBody(response);
                if (result != null) {
                    growth.clear();
                    growth.addAll(result.data);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call, Throwable error) {
                ApiUtils.showFailure(MainActivity.this, error);
            }
        });

        api.getFeedingRecords(ApiUtils.listQuery("feeding_records"))
                .enqueue(new Callback<ApiEnvelope<List<ApiModels.FeedingRecord>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<List<ApiModels.FeedingRecord>>> call,
                            Response<ApiEnvelope<List<ApiModels.FeedingRecord>>> response
                    ) {
                        ApiEnvelope<List<ApiModels.FeedingRecord>> result = validBody(response);
                        if (result != null) {
                            records.clear();
                            records.addAll(result.data);
                            renderRecentRecords();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiEnvelope<List<ApiModels.FeedingRecord>>> call,
                            Throwable error
                    ) {
                        ApiUtils.showFailure(MainActivity.this, error);
                    }
                });
    }

    private void loadSchedules() {
        RetrofitClient.getApiService().getSchedules(ApiUtils.listQuery("feeding_schedules"))
                .enqueue(new Callback<ApiEnvelope<List<ApiModels.FeedingSchedule>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<List<ApiModels.FeedingSchedule>>> call,
                            Response<ApiEnvelope<List<ApiModels.FeedingSchedule>>> response
                    ) {
                        ApiEnvelope<List<ApiModels.FeedingSchedule>> result = validBody(response);
                        if (result != null) {
                            schedules.clear();
                            for (ApiModels.FeedingSchedule item : result.data) {
                                if (findStock(item.stock_id) != null) schedules.add(item);
                            }
                            renderStockCard();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiEnvelope<List<ApiModels.FeedingSchedule>>> call,
                            Throwable error
                    ) {
                        ApiUtils.showFailure(MainActivity.this, error);
                    }
                });
    }

    private void renderStockCard() {
        if (stocks.isEmpty()) {
            stockName.setText("No fish stocks yet");
            population.setText("0");
            nextFeed.setText("No feeding schedule");
            return;
        }
        ApiModels.FishStock stock = stocks.get(0);
        stockName.setText(stock.stock_name);
        population.setText(String.valueOf(stock.stock_quantity));
        ApiModels.FeedingSchedule schedule = findSchedule(stock.stock_id);
        nextFeed.setText(schedule == null ? "No schedule" : safe(schedule.sched_time, "Time not set"));
    }

    private void renderRecentRecords() {
        for (int i = 0; i < 2; i++) {
            int nameId = i == 0 ? R.id.dashboard_recent_name_1 : R.id.dashboard_recent_name_2;
            int detailId = i == 0 ? R.id.dashboard_recent_detail_1 : R.id.dashboard_recent_detail_2;
            int timeId = i == 0 ? R.id.dashboard_recent_time_1 : R.id.dashboard_recent_time_2;
            TextView name = findViewById(nameId);
            TextView detail = findViewById(detailId);
            TextView time = findViewById(timeId);
            if (i >= records.size()) {
                name.setText("No recent feeding");
                detail.setText("");
                time.setText("");
                continue;
            }
            ApiModels.FeedingRecord record = records.get(i);
            ApiModels.FishStock stock = findStock(record.stock_id);
            ApiModels.FeedingSchedule schedule = findScheduleById(record.sched_id);
            name.setText(stock == null ? "Stock " + record.stock_id : stock.stock_name);
            detail.setText(safe(record.record_status, "Feeding recorded"));
            time.setText(safe(record.record_date_time, schedule == null ? "" : schedule.sched_time));
        }
    }

    private void openFirstStock() {
        if (stocks.isEmpty()) {
            startActivity(new Intent(this, StocksActivity.class));
            return;
        }
        ApiModels.FishStock stock = stocks.get(0);
        ApiModels.FishSpecies fishSpecies = findSpecies(stock.species_id);
        ApiModels.FishTank tank = findTank(stock.tank_id);
        ApiModels.FishGrowth fishGrowth = findGrowth(stock.growth_id);
        Intent intent = new Intent(this, StockDisplayActivity.class);
        intent.putExtra("stock_id", stock.stock_id);
        intent.putExtra("growth_id", stock.growth_id);
        intent.putExtra("species_id", stock.species_id);
        intent.putExtra("tank_id", stock.tank_id);
        intent.putExtra("stock_name", stock.stock_name);
        intent.putExtra("species_name", fishSpecies == null ? "" : safe(fishSpecies.species_name, fishSpecies.scientific_name));
        intent.putExtra("tank_name", tank == null ? "Tank " + stock.tank_id : tank.tank_name);
        intent.putExtra("growth_stage", fishGrowth == null ? "" : fishGrowth.growth_stage);
        intent.putExtra("stock_quantity", stock.stock_quantity);
        intent.putExtra("date_added", stock.date_added);
        startActivity(intent);
    }

    private ApiModels.FishStock findStock(int id) {
        for (ApiModels.FishStock item : stocks) if (item.stock_id == id) return item;
        return null;
    }

    private ApiModels.FishSpecies findSpecies(int id) {
        for (ApiModels.FishSpecies item : species) if (item.species_id == id) return item;
        return null;
    }

    private ApiModels.FishTank findTank(int id) {
        for (ApiModels.FishTank item : tanks) if (item.tank_id == id) return item;
        return null;
    }

    private ApiModels.FishGrowth findGrowth(int id) {
        for (ApiModels.FishGrowth item : growth) if (item.growth_id == id) return item;
        return null;
    }

    private ApiModels.FeedingSchedule findSchedule(int stockId) {
        for (ApiModels.FeedingSchedule item : schedules) if (item.stock_id == stockId) return item;
        return null;
    }

    private ApiModels.FeedingSchedule findScheduleById(int scheduleId) {
        for (ApiModels.FeedingSchedule item : schedules) if (item.sched_id == scheduleId) return item;
        return null;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private <T> ApiEnvelope<List<T>> validBody(Response<ApiEnvelope<List<T>>> response) {
        if (!response.isSuccessful() || response.body() == null
                || !"success".equalsIgnoreCase(response.body().status) || response.body().data == null) {
            ApiUtils.showFailure(this, response);
            return null;
        }
        return response.body();
    }
}
