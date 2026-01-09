package com.example.smartparking.model

data class ParkingSlot(
    val id: Long = 0,
    val slotNumber: Int = 0,
    val occupied: Boolean = false,
    val sensorId: String? = null,
    val parkingLot: ParkingLotReference? = null
)

data class ParkingLotReference(
    val id: Long = 0,
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)
