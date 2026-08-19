package com.example.myapidemo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    // GET /index.php
    @GET("index.php")
    Call<List<Product>> getProducts();

    // GET /index.php?category=Electronics&is_active=1
    @GET("index.php")
    Call<List<Product>> getProductsFiltered(
            @Query("category") String category,
            @Query("is_active") Integer isActive
    );

    // GET /index.php?id=1
    @GET("index.php")
    Call<Product> getProduct(
            @Query("id") int id
    );

    // POST /index.php
    @POST("index.php")
    Call<Product> createProduct(
            @Body Product product
    );

    // PUT /index.php?id=1
    @PUT("index.php")
    Call<Product> updateProduct(
            @Query("id") int id,
            @Body Product product
    );

    // DELETE /index.php?id=1
    @DELETE("index.php")
    Call<Void> deleteProduct(
            @Query("id") int id
    );
}
