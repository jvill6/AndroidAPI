package com.example.myapidemo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewTankActivity extends AppCompatActivity {

    private static final int DEFAULT_USER_ID = 1;
    private final List<ApiModels.FishSpecies> species = new ArrayList<>();
    private final List<ApiModels.FishTank> tanks = new ArrayList<>();
    private ApiModels.FishSpecies selectedSpecies;
    private ApiModels.FishTank selectedTank;
    private String selectedStage;
    private String dateAdded;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_tank_page);

        NavigationHelper.setupBottomNavigation(this, R.id.tab_NewTank);
        View headerProfile = findViewById(R.id.header_profile);
        if (headerProfile != null) {
            headerProfile.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }

        registerButton = findViewById(R.id.btn_register_tank);
        dateAdded = new SimpleDateFormat("MM/dd/yyyy", Locale.US)
                .format(Calendar.getInstance().getTime());
        setupSpeciesSelector();
        setupTankSelector();
        setupStageSelector();
        setupDateSelector();
        setupNavigation();
        loadChoices();
    }

    private void loadChoices() {
        ApiService api = RetrofitClient.getApiService();
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
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call, Throwable error) {
                ApiUtils.showFailure(NewTankActivity.this, error);
            }
        });

        java.util.Map<String, String> tankQuery = ApiUtils.listQuery("tanks");
        tankQuery.put("user_id", String.valueOf(DEFAULT_USER_ID));
        api.getTanks(tankQuery).enqueue(new Callback<ApiEnvelope<List<ApiModels.FishTank>>>() {
            @Override
            public void onResponse(
                    Call<ApiEnvelope<List<ApiModels.FishTank>>> call,
                    Response<ApiEnvelope<List<ApiModels.FishTank>>> response
            ) {
                ApiEnvelope<List<ApiModels.FishTank>> body = validBody(response);
                if (body != null) {
                    tanks.clear();
                    tanks.addAll(body.data);
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishTank>>> call, Throwable error) {
                ApiUtils.showFailure(NewTankActivity.this, error);
            }
        });
    }

    private void setupSpeciesSelector() {
        Button button = findViewById(R.id.btn_select_species);
        button.setOnClickListener(v -> {
            if (species.isEmpty()) {
                Toast.makeText(this, "Species have not loaded yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] options = new String[species.size()];
            for (int i = 0; i < species.size(); i++) {
                options[i] = speciesName(species.get(i));
            }
            new AlertDialog.Builder(this).setTitle("Select Species")
                    .setItems(options, (dialog, index) -> {
                        selectedSpecies = species.get(index);
                        button.setText(options[index]);
                    }).show();
        });
    }

    private void setupTankSelector() {
        Button button = findViewById(R.id.btn_assign_tank);
        button.setOnClickListener(v -> {
            if (tanks.isEmpty()) {
                Toast.makeText(this, "Tanks have not loaded yet.", Toast.LENGTH_SHORT).show();
                return;
            }
            String[] options = new String[tanks.size()];
            for (int i = 0; i < tanks.size(); i++) {
                ApiModels.FishTank tank = tanks.get(i);
                options[i] = tank.tank_name == null ? "Tank " + tank.tank_id : tank.tank_name;
            }
            new AlertDialog.Builder(this).setTitle("Assign to Tank")
                    .setItems(options, (dialog, index) -> {
                        selectedTank = tanks.get(index);
                        button.setText(options[index]);
                    }).show();
        });
    }

    private void setupStageSelector() {
        setStageButton(R.id.btn_stage_fry, "FRY");
        setStageButton(R.id.btn_stage_juvenile, "JUVENILE");
        setStageButton(R.id.btn_stage_adult, "ADULT");
    }

    private void setStageButton(int buttonId, String stage) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            selectedStage = stage;
            Toast.makeText(this, "Selected growth stage: " + stage, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDateSelector() {
        Button dateButton = findViewById(R.id.btn_date_stocked);
        dateButton.setText(dateAdded);
        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this, (picker, year, month, day) -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, day);
                dateAdded = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(selected.getTime());
                dateButton.setText(dateAdded);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setupNavigation() {
        Button cancelButton = findViewById(R.id.btn_cancel_tank);
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> finish());
        }
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> submitStock());
        }
    }

    private void submitStock() {
        EditText quantityField = findViewById(R.id.edt_fish_count);
        String quantityText = quantityField.getText().toString().trim();
        if (selectedSpecies == null || selectedTank == null || selectedStage == null) {
            Toast.makeText(this, "Select a species, growth stage, and tank.", Toast.LENGTH_LONG).show();
            return;
        }
        final int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity < 0) throw new NumberFormatException();
        } catch (NumberFormatException error) {
            quantityField.setError("Enter a non-negative fish count.");
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Saving...");
        java.util.Map<String, String> growthQuery = ApiUtils.listQuery("growth");
        growthQuery.put("user_id", String.valueOf(DEFAULT_USER_ID));
        RetrofitClient.getApiService().getGrowth(growthQuery)
                .enqueue(new Callback<ApiEnvelope<List<ApiModels.FishGrowth>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call,
                            Response<ApiEnvelope<List<ApiModels.FishGrowth>>> response
                    ) {
                        ApiEnvelope<List<ApiModels.FishGrowth>> body = validBody(response);
                        if (body == null) {
                            resetRegisterButton();
                            return;
                        }
                        ApiModels.FishGrowth match = findMatchingGrowth(body.data);
                        if (match == null) {
                            createGrowth(quantity);
                            return;
                        }
                        createStock(match, quantity);
                    }

                    @Override
                    public void onFailure(
                            Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call,
                            Throwable error
                    ) {
                        resetRegisterButton();
                        ApiUtils.showFailure(NewTankActivity.this, error);
                    }
                });
    }

    private void createGrowth(int quantity) {
        ApiModels.NewFishGrowth growth = new ApiModels.NewFishGrowth();
        growth.species_id = selectedSpecies.species_id;
        growth.user_id = DEFAULT_USER_ID;
        growth.tank_id = selectedTank.tank_id;
        growth.growth_stage = selectedStage;
        RetrofitClient.getApiService().createGrowth("growth", growth)
                .enqueue(new Callback<ApiEnvelope<ApiModels.FishGrowth>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<ApiModels.FishGrowth>> call,
                            Response<ApiEnvelope<ApiModels.FishGrowth>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !"success".equalsIgnoreCase(response.body().status)
                                || response.body().data == null) {
                            ApiUtils.showFailure(NewTankActivity.this, response);
                            resetRegisterButton();
                            return;
                        }
                        createStock(response.body().data, quantity);
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<ApiModels.FishGrowth>> call, Throwable error) {
                        resetRegisterButton();
                        ApiUtils.showFailure(NewTankActivity.this, error);
                    }
                });
    }

    private ApiModels.FishGrowth findMatchingGrowth(List<ApiModels.FishGrowth> growth) {
        for (ApiModels.FishGrowth item : growth) {
            if (item.species_id == selectedSpecies.species_id
                    && item.tank_id == selectedTank.tank_id
                    && selectedStage.equalsIgnoreCase(item.growth_stage)) {
                return item;
            }
        }
        return null;
    }

    private void createStock(ApiModels.FishGrowth growth, int quantity) {
        ApiModels.NewFishStock stock = new ApiModels.NewFishStock();
        stock.tank_id = selectedTank.tank_id;
        stock.species_id = selectedSpecies.species_id;
        stock.stock_name = speciesName(selectedSpecies) + " Group";
        stock.stock_quantity = quantity;
        stock.growth_id = growth.growth_id;
        stock.date_added = dateAdded;

        RetrofitClient.getApiService().createStock("stocks", stock)
                .enqueue(new Callback<ApiEnvelope<ApiModels.FishStock>>() {
                    @Override
                    public void onResponse(
                            Call<ApiEnvelope<ApiModels.FishStock>> call,
                            Response<ApiEnvelope<ApiModels.FishStock>> response
                    ) {
                        if (!response.isSuccessful() || response.body() == null
                                || !"success".equalsIgnoreCase(response.body().status)
                                || response.body().data == null) {
                            ApiUtils.showFailure(NewTankActivity.this, response);
                            resetRegisterButton();
                            return;
                        }
                        Toast.makeText(NewTankActivity.this, "Stock registered.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(NewTankActivity.this, StocksActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<ApiEnvelope<ApiModels.FishStock>> call, Throwable error) {
                        resetRegisterButton();
                        ApiUtils.showFailure(NewTankActivity.this, error);
                    }
                });
    }

    private <T> ApiEnvelope<List<T>> validBody(Response<ApiEnvelope<List<T>>> response) {
        if (!response.isSuccessful() || response.body() == null
                || !"success".equalsIgnoreCase(response.body().status) || response.body().data == null) {
            ApiUtils.showFailure(this, response);
            return null;
        }
        return response.body();
    }

    private String speciesName(ApiModels.FishSpecies fishSpecies) {
        if (fishSpecies.species_name != null && !fishSpecies.species_name.trim().isEmpty()) {
            return fishSpecies.species_name;
        }
        return fishSpecies.scientific_name == null ? "Fish" : fishSpecies.scientific_name;
    }

    private void resetRegisterButton() {
        registerButton.setEnabled(true);
        registerButton.setText("Register Stock");
    }
}
