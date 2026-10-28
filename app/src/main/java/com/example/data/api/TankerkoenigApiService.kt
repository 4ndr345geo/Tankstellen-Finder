package com.example.data.api

import com.example.data.model.TankerkoenigPricesResponse
import com.example.data.model.TankerkoenigResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TankerkoenigApiService {
    @GET("json/list.php")
    suspend fun getStations(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("rad") rad: Float,
        @Query("sort") sort: String,
        @Query("type") type: String,
        @Query("apikey") apiKey: String
    ): TankerkoenigResponse

    @GET("json/prices.php")
    suspend fun getPrices(
        @Query("ids") ids: String,
        @Query("apikey") apiKey: String
    ): TankerkoenigPricesResponse
}
