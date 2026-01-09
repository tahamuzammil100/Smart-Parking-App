package com.example.smartparking.model

data class ParkingLot(
    val id: Long = 0,
    val name: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val parkingSlots: List<ParkingSlot> = emptyList()
) {
    val freeSlots: Int
        get() = parkingSlots.count { !it.occupied }

    val totalSlots: Int
        get() = parkingSlots.size
}
