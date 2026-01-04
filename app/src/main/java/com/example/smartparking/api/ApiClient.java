package com.example.smartparking.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Simple API client to connect to the backend
 */
public class ApiClient {

    // Backend server URL
    private static final String BASE_URL = "http://192.168.1.232:8080/";

    private static Retrofit retrofit = null;

    /**
     * Get Retrofit instance
     * Retrofit is a library that makes API calls easy
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)  // Your backend URL
                    .addConverterFactory(GsonConverterFactory.create())  // Converts JSON to Java objects
                    .build();
        }
        return retrofit;
    }

    /**
     * Get the parking API service
     * This is what you'll use in your activity to fetch data
     */
    public static ParkingApi getParkingApi() {
        return getClient().create(ParkingApi.class);
    }
}
