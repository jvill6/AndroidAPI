package com.example.myapidemo;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StockDisplayActivity extends AppCompatActivity {

    private final List<ApiModels.FeedingSchedule> schedules = new ArrayList<>();
    private final List<ApiModels.FeedingRecord> records = new ArrayList<>();
    private LinearLayout scheduleContainer;
    private int stockId;
    private int growthId;
    private int loadedRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stock_display);

        Intent stockIntent = getIntent();
        stockId = stockIntent.getIntExtra("stock_id", 0);
        growthId = stockIntent.getIntExtra("growth_id", 0);
        if (stockId > 0) {
            ((TextView) findViewById(R.id.stock_id_value)).setText(String.valueOf(stockId));
            setText(R.id.stock_name_value, stockIntent.getStringExtra("stock_name"));
            setText(R.id.stock_species_value, stockIntent.getStringExtra("species_name"));
            setText(R.id.stock_stage_value, stockIntent.getStringExtra("growth_stage"));
            setText(R.id.stock_tank_value, stockIntent.getStringExtra("tank_name"));
            setText(R.id.stock_date_value, stockIntent.getStringExtra("date_added"));
            ((TextView) findViewById(R.id.stock_quantity_value))
                    .setText(String.valueOf(stockIntent.getIntExtra("stock_quantity", 0)));
        } else {
            setText(R.id.stock_id_value, "--");
            setText(R.id.stock_name_value, "Select a stock");
            setText(R.id.stock_species_value, "");
            setText(R.id.stock_stage_value, "");
            setText(R.id.stock_tank_value, "");
            setText(R.id.stock_date_value, "");
            setText(R.id.stock_quantity_value, "--");
        }
        findViewById(R.id.stock_harvest_label).setVisibility(View.GONE);
        findViewById(R.id.stock_harvest_value).setVisibility(View.GONE);

        scheduleContainer = findViewById(R.id.detail_schedule_items);
        scheduleContainer.removeAllViews();
        showScheduleMessage("Loading feeding details...");
        setText(R.id.detail_daily_feed, "Loading...");
        setText(R.id.detail_feed_type, "Loading feeding requirement...");

        ImageView btnBack = findViewById(R.id.btn_back_stock_display);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        Button btnRecalculate = findViewById(R.id.btn_recalculate_stock);
        if (btnRecalculate != null) btnRecalculate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CalculatorActivity.class);
            intent.putExtra("species_id", stockIntent.getIntExtra("species_id", 0));
            intent.putExtra("growth_stage", stockIntent.getStringExtra("growth_stage"));
            intent.putExtra("stock_quantity", stockIntent.getIntExtra("stock_quantity", 0));
            startActivity(intent);
        });
        Button btnEditSched = findViewById(R.id.btn_edit_schedule);
        if (btnEditSched != null) btnEditSched.setOnClickListener(v ->
                startActivity(new Intent(this, ScheduleActivity.class)));
        Button btnTransferTank = findViewById(R.id.btn_transfer_tank);
        if (btnTransferTank != null) btnTransferTank.setOnClickListener(v ->
                startActivity(new Intent(this, NewTankActivity.class)));
        View doneButton = findViewById(R.id.btn_done_ration);
        if (doneButton != null) doneButton.setVisibility(View.GONE);

        if (stockId > 0) loadRelatedData();
        else showScheduleMessage("Select a stock from the Stocks screen to view its feeding details.");
        if (stockId > 0) loadStockLookups(stockIntent);
    }

    private void loadStockLookups(Intent stockIntent) {
        ApiService api = RetrofitClient.getApiService();
        int speciesId = stockIntent.getIntExtra("species_id", 0);
        if (speciesId > 0) {
            api.getSpecies(ApiUtils.listQuery("species"))
                    .enqueue(new Callback<ApiEnvelope<List<ApiModels.FishSpecies>>>() {
                @Override
                public void onResponse(
                        Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call,
                        Response<ApiEnvelope<List<ApiModels.FishSpecies>>> response
                ) {
                    ApiEnvelope<List<ApiModels.FishSpecies>> body = validBody(response);
                    if (body != null) {
                        for (ApiModels.FishSpecies fishSpecies : body.data) {
                            if (fishSpecies.species_id == speciesId) {
                                String name = safe(fishSpecies.species_name, fishSpecies.scientific_name);
                                setText(R.id.stock_species_value, fishSpecies.scientific_name);
                                if (name != null) {
                                    TextView title = findViewById(R.id.stock_name_value);
                                    if (title.getText().toString().trim().isEmpty()) {
                                        title.setText(name + " Group");
                                    }
                                }
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call, Throwable error) {
                    ApiUtils.showFailure(StockDisplayActivity.this, error);
                }
            });
        }

        int tankId = stockIntent.getIntExtra("tank_id", 0);
        if (tankId > 0) {
            api.getTanks(ApiUtils.listQuery("tanks"))
                    .enqueue(new Callback<ApiEnvelope<List<ApiModels.FishTank>>>() {
                @Override
                public void onResponse(
                        Call<ApiEnvelope<List<ApiModels.FishTank>>> call,
                        Response<ApiEnvelope<List<ApiModels.FishTank>>> response
                ) {
                    ApiEnvelope<List<ApiModels.FishTank>> body = validBody(response);
                    if (body != null) {
                        for (ApiModels.FishTank tank : body.data) {
                            if (tank.tank_id == tankId) {
                                setText(R.id.stock_tank_value, tank.tank_name);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiEnvelope<List<ApiModels.FishTank>>> call, Throwable error) {
                    ApiUtils.showFailure(StockDisplayActivity.this, error);
                }
            });
        }
    }

    private void loadRelatedData() {
        ApiService api = RetrofitClient.getApiService();

        Map<String, String> scheduleQuery = ApiUtils.listQuery("feeding_schedules");
        scheduleQuery.put("stock_id", String.valueOf(stockId));
        api.getSchedules(scheduleQuery).enqueue(new Callback<ApiEnvelope<List<ApiModels.FeedingSchedule>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FeedingSchedule>>> call,
                    Response<ApiEnvelope<List<ApiModels.FeedingSchedule>>> response
            ) {
                ApiEnvelope<List<ApiModels.FeedingSchedule>> body = validBody(response);
                if (body != null) {
                    schedules.clear();
                    schedules.addAll(body.data);
                    renderSchedules();
                }
                requestLoaded();
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FeedingSchedule>>> call, Throwable error) {
                ApiUtils.showFailure(StockDisplayActivity.this, error);
                requestLoaded();
            }
        });

        Map<String, String> recordQuery = ApiUtils.listQuery("feeding_records");
        recordQuery.put("stock_id", String.valueOf(stockId));
        api.getFeedingRecords(recordQuery).enqueue(new Callback<ApiEnvelope<List<ApiModels.FeedingRecord>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FeedingRecord>>> call,
                    Response<ApiEnvelope<List<ApiModels.FeedingRecord>>> response
            ) {
                ApiEnvelope<List<ApiModels.FeedingRecord>> body = validBody(response);
                if (body != null) {
                    records.clear();
                    records.addAll(body.data);
                    renderSchedules();
                }
                requestLoaded();
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FeedingRecord>>> call, Throwable error) {
                ApiUtils.showFailure(StockDisplayActivity.this, error);
                requestLoaded();
            }
        });

        api.getFeedingRequirements(ApiUtils.listQuery("feeding_requirements"))
                .enqueue(new Callback<ApiEnvelope<List<ApiModels.FeedingRequirement>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<List<ApiModels.FeedingRequirement>>> call,
                            Response<ApiEnvelope<List<ApiModels.FeedingRequirement>>> response
                    ) {
                        ApiEnvelope<List<ApiModels.FeedingRequirement>> body = validBody(response);
                        if (body != null) {
                            ApiModels.FeedingRequirement requirement = findRequirement(body.data);
                            if (requirement == null) {
                                setText(R.id.detail_daily_feed, "No requirement set");
                                setText(R.id.detail_feed_type, "Recommended feed type: unavailable");
                            } else {
                                setText(R.id.detail_daily_feed,
                                        requirement.reqfeed_gramsperday + " grams");
                                setText(R.id.detail_feed_type,
                                        "Recommended Feed Type: " + requirement.reqfeed_type);
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiEnvelope<List<ApiModels.FeedingRequirement>>> call,
                            Throwable error
                    ) {
                        ApiUtils.showFailure(StockDisplayActivity.this, error);
                    }
                });

        if (growthId > 0) {
            api.getGrowth(ApiUtils.listQuery("growth"))
                    .enqueue(new Callback<ApiEnvelope<List<ApiModels.FishGrowth>>>() {
                @Override
                public void onResponse(
                        Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call,
                        Response<ApiEnvelope<List<ApiModels.FishGrowth>>> response
                ) {
                    ApiEnvelope<List<ApiModels.FishGrowth>> body = validBody(response);
                    if (body != null) {
                        for (ApiModels.FishGrowth item : body.data) {
                            if (item.growth_id == growthId) {
                                setText(R.id.stock_stage_value, item.growth_stage);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call, Throwable error) {
                    ApiUtils.showFailure(StockDisplayActivity.this, error);
                }
            });
        }
    }

    private void requestLoaded() {
        loadedRequests++;
        if (loadedRequests == 2) renderSchedules();
    }

    private void renderSchedules() {
        if (scheduleContainer == null || loadedRequests < 2) return;
        scheduleContainer.removeAllViews();
        setText(R.id.detail_schedule_count, schedules.size() + " feeding schedules");
        if (schedules.isEmpty()) {
            showScheduleMessage("No feeding schedules for this stock.");
            return;
        }
        for (ApiModels.FeedingSchedule schedule : schedules) {
            ApiModels.FeedingRecord record = findRecord(schedule.sched_id);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(8), 0, dp(8));

            TextView feed = new TextView(this);
            feed.setText(safe(schedule.sched_name, "Feeding") + " • "
                    + safe(schedule.sched_days, "Days not set"));
            feed.setTextColor(Color.BLACK);
            feed.setTextSize(13);

            TextView time = new TextView(this);
            String status = record == null ? "Not recorded" : safe(record.record_status, "Recorded");
            time.setText(safe(schedule.sched_time, "Time not set") + " • " + status);
            time.setTextColor(Color.DKGRAY);
            time.setTextSize(12);

            LinearLayout labels = new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.addView(feed);
            labels.addView(time);
            LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            row.addView(labels, labelsParams);

            Button done = new Button(this);
            done.setText(record != null && "DONE".equalsIgnoreCase(record.record_status) ? "Done" : "Mark Done");
            done.setAllCaps(false);
            done.setEnabled(record == null || !"DONE".equalsIgnoreCase(record.record_status));
            done.setOnClickListener(v -> markDone(schedule));
            row.addView(done);
            scheduleContainer.addView(row);
        }
    }

    private void markDone(ApiModels.FeedingSchedule schedule) {
        ApiModels.NewFeedingRecord request = new ApiModels.NewFeedingRecord();
        request.sched_id = schedule.sched_id;
        request.stock_id = stockId;
        request.record_status = "DONE";
        request.record_date_time = new SimpleDateFormat("MMMM-dd-yyyy, hh:mm a", Locale.US)
                .format(Calendar.getInstance().getTime());
        RetrofitClient.getApiService().createFeedingRecord("feeding_records", request)
                .enqueue(new Callback<ApiEnvelope<ApiModels.FeedingRecord>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<ApiModels.FeedingRecord>> call,
                            Response<ApiEnvelope<ApiModels.FeedingRecord>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !"success".equalsIgnoreCase(response.body().status)
                                || response.body().data == null) {
                            ApiUtils.showFailure(StockDisplayActivity.this, response);
                            return;
                        }
                        records.add(0, response.body().data);
                        renderSchedules();
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<ApiModels.FeedingRecord>> call, Throwable error) {
                        ApiUtils.showFailure(StockDisplayActivity.this, error);
                    }
                });
    }

    private ApiModels.FeedingRecord findRecord(int scheduleId) {
        for (ApiModels.FeedingRecord record : records) {
            if (record.sched_id == scheduleId) return record;
        }
        return null;
    }

    private ApiModels.FeedingRequirement findRequirement(List<ApiModels.FeedingRequirement> requirements) {
        for (ApiModels.FeedingRequirement requirement : requirements) {
            if (requirement.growth_id == growthId) return requirement;
        }
        return null;
    }

    private void showScheduleMessage(String message) {
        if (scheduleContainer == null) return;
        scheduleContainer.removeAllViews();
        TextView view = new TextView(this);
        view.setText(message);
        view.setTextColor(Color.DKGRAY);
        view.setTextSize(14);
        view.setPadding(0, dp(12), 0, dp(12));
        scheduleContainer.addView(view);
    }

    private void setText(int viewId, String value) {
        TextView view = findViewById(viewId);
        if (view != null && value != null && !value.trim().isEmpty()) view.setText(value);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
