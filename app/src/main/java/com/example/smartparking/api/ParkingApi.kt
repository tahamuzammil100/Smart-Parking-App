package com.example.smartparking.api

import com.example.smartparking.model.ParkingLot
import com.example.smartparking.model.ParkingSlot
import retrofit2.http.GET

interface ParkingApi {
    @GET("api/parking/lots")
    suspend fun getParkingLots(): List<ParkingLot>

    @GET("api/parking/slots")
    suspend fun getParkingSlots(): List<ParkingSlot>
}
