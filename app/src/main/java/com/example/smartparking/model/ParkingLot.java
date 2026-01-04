package com.example.smartparking.model;

import java.util.List;

/**
 * Simple class to store parking lot data
 * This matches the backend JSON response
 */
public class ParkingLot {
    private Long id;
    private String name;
    private String address;
    private List<ParkingSlot> parkingSlots;

    // For demo, we'll add dummy GPS coordinates for France
    private double latitude = 48.8566;  // Paris, France (dummy location)
    private double longitude = 2.3522;

    // Empty constructor
    public ParkingLot() {
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<ParkingSlot> getParkingSlots() {
        return parkingSlots;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setParkingSlots(List<ParkingSlot> parkingSlots) {
        this.parkingSlots = parkingSlots;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Helper method to count free slots
     * @return number of free (not occupied) slots
     */
    public int getFreeSlots() {
        if (parkingSlots == null) return 0;

        int count = 0;
        for (ParkingSlot slot : parkingSlots) {
            if (!slot.isOccupied()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Helper method to get total number of slots
     */
    public int getTotalSlots() {
        if (parkingSlots == null) return 0;
        return parkingSlots.size();
    }
}
