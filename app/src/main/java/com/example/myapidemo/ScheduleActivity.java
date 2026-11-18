package com.example.myapidemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScheduleActivity extends AppCompatActivity {

    private final List<ApiModels.FishStock> stocks = new ArrayList<>();
    private final List<ApiModels.FishSpecies> species = new ArrayList<>();
    private final List<ApiModels.FishTank> tanks = new ArrayList<>();
    private final List<ApiModels.FishGrowth> growth = new ArrayList<>();
    private final List<ApiModels.FeedingRequirement> requirements = new ArrayList<>();
    private final List<ApiModels.FeedingSchedule> schedules = new ArrayList<>();
    private final List<ApiModels.FeedingRecord> records = new ArrayList<>();
    private LinearLayout listContainer;
    private boolean showingRecords;
    private int loadedRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_page);

        NavigationHelper.setupBottomNavigation(this, 0);
        View headerProfile = findViewById(R.id.header_profile);
        if (headerProfile != null) {
            headerProfile.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
        listContainer = findViewById(R.id.schedule_list_container);
        listContainer.removeAllViews();
        addMessage("Loading feeding data...");

        Button btnUpcoming = findViewById(R.id.btn_upcoming);
        if (btnUpcoming != null) btnUpcoming.setOnClickListener(v -> {
            showingRecords = false;
            renderList();
        });
        Button btnPastRecords = findViewById(R.id.btn_past_records);
        if (btnPastRecords != null) btnPastRecords.setOnClickListener(v -> {
            showingRecords = true;
            renderList();
        });

        loadData();
    }

    private void loadData() {
        ApiService api = RetrofitClient.getApiService();
        load(api.getStocks(ApiUtils.listQuery("stocks")), stocks, "stocks");
        load(api.getSpecies(ApiUtils.listQuery("species")), species, "species");
        load(api.getTanks(ApiUtils.listQuery("tanks")), tanks, "tanks");
        load(api.getGrowth(ApiUtils.listQuery("growth")), growth, "growth");
        load(api.getFeedingRequirements(ApiUtils.listQuery("feeding_requirements")), requirements, "requirements");
        load(api.getSchedules(ApiUtils.listQuery("feeding_schedules")), schedules, "schedules");
        load(api.getFeedingRecords(ApiUtils.listQuery("feeding_records")), records, "records");
    }

    private <T> void load(Call<ApiEnvelope<List<T>>> call, List<T> destination, String resource) {
        call.enqueue(new Callback<ApiEnvelope<List<T>>>() {
            @Override
            public void onResponse(Call<ApiEnvelope<List<T>>> request, Response<ApiEnvelope<List<T>>> response) {
                ApiEnvelope<List<T>> result = validBody(response);
                if (result != null) {
                    destination.clear();
                    destination.addAll(result.data);
                }
                requestLoaded();
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<T>>> request, Throwable error) {
                ApiUtils.showFailure(ScheduleActivity.this, error);
                requestLoaded();
            }
        });
    }

    private void requestLoaded() {
        loadedRequests++;
        if (loadedRequests == 7) renderList();
    }

    private void renderList() {
        if (listContainer == null || loadedRequests < 7) return;
        listContainer.removeAllViews();
        if (showingRecords) {
            if (records.isEmpty()) {
                addMessage("No feeding records found.");
                return;
            }
            for (ApiModels.FeedingRecord record : records) {
                addRecordCard(record);
            }
        } else {
            if (schedules.isEmpty()) {
                addMessage("No feeding schedules found.");
                return;
            }
            for (ApiModels.FeedingSchedule schedule : schedules) {
                addScheduleCard(schedule);
            }
        }
    }

    private void addScheduleCard(ApiModels.FeedingSchedule schedule) {
        ApiModels.FishStock stock = findStock(schedule.stock_id);
        if (stock == null) return;
        ApiModels.FishSpecies fishSpecies = findSpecies(stock.species_id);
        ApiModels.FishTank tank = findTank(schedule.tank_id);
        ApiModels.FeedingRequirement requirement = findRequirement(stock.growth_id);

        LinearLayout card = createCard();
        card.setOnClickListener(v -> openStock(stock));
        card.addView(text(safe(schedule.sched_time, "Time not set"), 14, true));
        card.addView(text(safe(schedule.sched_name, stock.stock_name), 18, true));
        card.addView(text(safe(fishSpecies == null ? null : fishSpecies.species_name, stock.stock_name)
                + " • " + safe(tank == null ? null : tank.tank_name, "Tank " + schedule.tank_id), 14, false));
        card.addView(text((requirement == null ? "Feed requirement unavailable"
                : requirement.reqfeed_gramsperday + "g/day • " + requirement.reqfeed_type)
                + (schedule.sched_days == null ? "" : " • " + schedule.sched_days), 13, false));

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.END);
        Button recalculate = new Button(this);
        recalculate.setText("Recalculate");
        recalculate.setAllCaps(false);
        recalculate.setOnClickListener(v -> startActivity(new Intent(this, CalculatorActivity.class)));
        actions.addView(recalculate);

        Button done = new Button(this);
        done.setText("Mark Done");
        done.setAllCaps(false);
        done.setOnClickListener(v -> markDone(schedule));
        actions.addView(done);
        card.addView(actions);
        listContainer.addView(card);
    }

    private void addRecordCard(ApiModels.FeedingRecord record) {
        ApiModels.FishStock stock = findStock(record.stock_id);
        ApiModels.FeedingSchedule schedule = findSchedule(record.sched_id);
        ApiModels.FishSpecies fishSpecies = stock == null ? null : findSpecies(stock.species_id);
        LinearLayout card = createCard();
        card.addView(text(stock == null ? "Stock " + record.stock_id
                : safe(fishSpecies == null ? null : fishSpecies.species_name, stock.stock_name), 18, true));
        card.addView(text(safe(record.record_status, "Feeding recorded"), 14, true));
        card.addView(text(safe(record.record_date_time,
                schedule == null ? "" : safe(schedule.sched_time, "Time not set")), 13, false));
        if (stock != null) card.setOnClickListener(v -> openStock(stock));
        listContainer.addView(card);
    }

    private void markDone(ApiModels.FeedingSchedule schedule) {
        ApiModels.NewFeedingRecord record = new ApiModels.NewFeedingRecord();
        record.sched_id = schedule.sched_id;
        record.stock_id = schedule.stock_id;
        record.record_status = "DONE";
        record.record_date_time = new SimpleDateFormat("MMMM-dd-yyyy, hh:mm a", Locale.US)
                .format(Calendar.getInstance().getTime());
        RetrofitClient.getApiService().createFeedingRecord("feeding_records", record)
                .enqueue(new Callback<ApiEnvelope<ApiModels.FeedingRecord>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<ApiModels.FeedingRecord>> call,
                            Response<ApiEnvelope<ApiModels.FeedingRecord>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !"success".equalsIgnoreCase(response.body().status)
                                || response.body().data == null) {
                            ApiUtils.showFailure(ScheduleActivity.this, response);
                            return;
                        }
                        records.add(0, response.body().data);
                        Toast.makeText(ScheduleActivity.this, "Feeding recorded.", Toast.LENGTH_SHORT).show();
                        renderList();
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<ApiModels.FeedingRecord>> call, Throwable error) {
                        ApiUtils.showFailure(ScheduleActivity.this, error);
                    }
                });
    }

    private void openStock(ApiModels.FishStock stock) {
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

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.white_background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(16);
        card.setLayoutParams(params);
        return card;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.BLACK);
        if (bold) view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setPadding(0, dp(3), 0, dp(3));
        return view;
    }

    private void addMessage(String value) {
        TextView message = text(value, 14, false);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(16), dp(24), dp(16), dp(24));
        listContainer.addView(message);
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

    private ApiModels.FeedingRequirement findRequirement(int growthId) {
        for (ApiModels.FeedingRequirement item : requirements) if (item.growth_id == growthId) return item;
        return null;
    }

    private ApiModels.FeedingSchedule findSchedule(int id) {
        for (ApiModels.FeedingSchedule item : schedules) if (item.sched_id == id) return item;
        return null;
    }

    private String safe(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
