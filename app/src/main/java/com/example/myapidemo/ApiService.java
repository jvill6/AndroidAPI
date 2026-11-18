package com.example.myapidemo;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface ApiService {

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FishStock>>> getStocks(@QueryMap Map<String, String> query);

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FishSpecies>>> getSpecies(@QueryMap Map<String, String> query);

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FishTank>>> getTanks(@QueryMap Map<String, String> query);

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FishGrowth>>> getGrowth(@QueryMap Map<String, String> query);

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FeedingSchedule>>> getSchedules(@QueryMap Map<String, String> query);

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FeedingRequirement>>> getFeedingRequirements(@QueryMap Map<String, String> query);

    @GET("index.php")
    Call<ApiEnvelope<List<ApiModels.FeedingRecord>>> getFeedingRecords(@QueryMap Map<String, String> query);

    @POST("index.php")
    Call<ApiEnvelope<ApiModels.FishGrowth>> createGrowth(
            @Query("endpoint") String endpoint,
            @Body ApiModels.NewFishGrowth growth
    );

    @POST("index.php")
    Call<ApiEnvelope<ApiModels.FishStock>> createStock(
            @Query("endpoint") String endpoint,
            @Body ApiModels.NewFishStock stock
    );

    @POST("index.php")
    Call<ApiEnvelope<ApiModels.FishTank>> createTank(
            @Query("endpoint") String endpoint,
            @Body ApiModels.NewFishTank tank
    );

    @DELETE("index.php")
    Call<ApiEnvelope<Void>> deleteStock(
            @Query("endpoint") String endpoint,
            @Query("id") int stockId
    );

    @DELETE("index.php")
    Call<ApiEnvelope<Void>> deleteTank(
            @Query("endpoint") String endpoint,
            @Query("id") int tankId
    );

    @DELETE("index.php")
    Call<ApiEnvelope<Void>> deleteSchedule(
            @Query("endpoint") String endpoint,
            @Query("id") int scheduleId
    );

    @DELETE("index.php")
    Call<ApiEnvelope<Void>> deleteFeedingRecord(
            @Query("endpoint") String endpoint,
            @Query("id") int recordId
    );

    @DELETE("index.php")
    Call<ApiEnvelope<Void>> deleteFeedingRequirement(
            @Query("endpoint") String endpoint,
            @Query("id") int requirementId
    );

    @DELETE("index.php")
    Call<ApiEnvelope<Void>> deleteGrowth(
            @Query("endpoint") String endpoint,
            @Query("id") int growthId
    );

    @POST("index.php")
    Call<ApiEnvelope<ApiModels.FeedingRecord>> createFeedingRecord(
            @Query("endpoint") String endpoint,
            @Body ApiModels.NewFeedingRecord record
    );
}
