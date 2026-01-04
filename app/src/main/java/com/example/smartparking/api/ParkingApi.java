package com.example.smartparking.api;

import com.example.smartparking.model.ParkingLot;
import com.example.smartparking.model.ParkingSlot;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Simple API interface
 * Defines what data we want to get from the backend
 */
public interface ParkingApi {

    /**
     * Get all parking lots from backend
     * This will call: http://your-backend-url/api/parking/lots
     */
    @GET("api/parking/lots")
    Call<List<ParkingLot>> getParkingLots();

    /**
     * Get all parking slots from backend
     * This will call: http://your-backend-url/api/parking/slots
     */
    @GET("api/parking/slots")
    Call<List<ParkingSlot>> getParkingSlots();
}
