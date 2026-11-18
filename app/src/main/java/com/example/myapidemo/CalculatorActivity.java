package com.example.myapidemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalculatorActivity extends AppCompatActivity {

    private final List<ApiModels.FishSpecies> species = new ArrayList<>();
    private final List<ApiModels.FishGrowth> growth = new ArrayList<>();
    private final List<ApiModels.FeedingRequirement> requirements = new ArrayList<>();
    private ApiModels.FishSpecies selectedSpecies;
    private String selectedStage = "ADULT";
    private Button speciesButton;
    private Button calculateButton;
    private boolean speciesLoaded;
    private boolean growthLoaded;
    private boolean requirementsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calculator_page);

        NavigationHelper.setupBottomNavigation(this, R.id.tab_Calculator);
        View headerProfile = findViewById(R.id.header_profile);
        if (headerProfile != null) {
            headerProfile.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }

        speciesButton = findViewById(R.id.btn_select_species);
        calculateButton = findViewById(R.id.btn_calculate_feed);
        EditText populationField = findViewById(R.id.edt_population);
        int initialPopulation = getIntent().getIntExtra("stock_quantity", 0);
        if (initialPopulation > 0) populationField.setText(String.valueOf(initialPopulation));
        String initialStage = getIntent().getStringExtra("growth_stage");
        if (initialStage != null && !initialStage.trim().isEmpty()) selectedStage = initialStage;
        speciesButton.setOnClickListener(v -> selectSpecies());
        setStageButton(R.id.btn_stage_fry, "FRY");
        setStageButton(R.id.btn_stage_juvenile, "JUVENILE");
        setStageButton(R.id.btn_stage_adult, "ADULT");
        calculateButton.setOnClickListener(v -> calculateFeed());
        loadData();
    }

    private void loadData() {
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
                    speciesLoaded = true;
                    int selectedSpeciesId = getIntent().getIntExtra("species_id", 0);
                    for (ApiModels.FishSpecies fishSpecies : species) {
                        if (fishSpecies.species_id == selectedSpeciesId) {
                            selectedSpecies = fishSpecies;
                            speciesButton.setText(speciesName(fishSpecies));
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishSpecies>>> call, Throwable error) {
                ApiUtils.showFailure(CalculatorActivity.this, error);
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
                    growthLoaded = true;
                }
            }

            @Override
            public void onFailure(Call<ApiEnvelope<List<ApiModels.FishGrowth>>> call, Throwable error) {
                ApiUtils.showFailure(CalculatorActivity.this, error);
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
                            requirements.clear();
                            requirements.addAll(body.data);
                            requirementsLoaded = true;
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiEnvelope<List<ApiModels.FeedingRequirement>>> call,
                            Throwable error
                    ) {
                        ApiUtils.showFailure(CalculatorActivity.this, error);
                    }
                });
    }

    private void selectSpecies() {
        if (!speciesLoaded || species.isEmpty()) {
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
                    speciesButton.setText(options[index]);
                }).show();
    }

    private void setStageButton(int buttonId, String stage) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(v -> {
            selectedStage = stage;
            Toast.makeText(this, "Selected growth stage: " + stage, Toast.LENGTH_SHORT).show();
        });
    }

    private void calculateFeed() {
        if (!growthLoaded || !requirementsLoaded) {
            Toast.makeText(this, "Feeding data is still loading.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedSpecies == null) {
            Toast.makeText(this, "Select a species first.", Toast.LENGTH_SHORT).show();
            return;
        }
        EditText populationField = findViewById(R.id.edt_population);
        String populationText = populationField.getText().toString().trim();
        int population = 0;
        if (!populationText.isEmpty()) {
            try {
                population = Integer.parseInt(populationText);
                if (population < 0) throw new NumberFormatException();
            } catch (NumberFormatException error) {
                populationField.setError("Enter a non-negative fish count.");
                return;
            }
        }

        ApiModels.FishGrowth matchingGrowth = null;
        for (ApiModels.FishGrowth item : growth) {
            if (item.species_id == selectedSpecies.species_id
                    && selectedStage.equalsIgnoreCase(item.growth_stage)) {
                matchingGrowth = item;
                break;
            }
        }
        if (matchingGrowth == null) {
            Toast.makeText(this, "No growth data exists for this species and stage.", Toast.LENGTH_LONG).show();
            return;
        }
        ApiModels.FeedingRequirement requirement = null;
        for (ApiModels.FeedingRequirement item : requirements) {
            if (item.growth_id == matchingGrowth.growth_id) {
                requirement = item;
                break;
            }
        }
        if (requirement == null) {
            Toast.makeText(this, "No feeding requirement is configured for this growth stage.", Toast.LENGTH_LONG).show();
            return;
        }

        String populationSummary = population > 0 ? "\nPopulation: " + population + " pcs" : "";
        new AlertDialog.Builder(this)
                .setTitle("Feed Calculation Result")
                .setMessage("Species: " + speciesName(selectedSpecies)
                        + "\nGrowth Stage: " + matchingGrowth.growth_stage
                        + populationSummary
                        + "\n\nRequired Daily Feed: " + requirement.reqfeed_gramsperday + " grams"
                        + "\nFeed Type: " + requirement.reqfeed_type)
                .setPositiveButton("OK", null)
                .setNeutralButton("View Schedule", (dialog, which) ->
                        startActivity(new Intent(this, ScheduleActivity.class)))
                .show();
    }

    private String speciesName(ApiModels.FishSpecies fishSpecies) {
        if (fishSpecies.species_name != null && !fishSpecies.species_name.trim().isEmpty()) {
            return fishSpecies.species_name;
        }
        return fishSpecies.scientific_name == null ? "Fish" : fishSpecies.scientific_name;
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
