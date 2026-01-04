package com.example.smartparking.model;

/**
 * Simple class to store parking slot data
 * This matches the backend JSON response
 */
public class ParkingSlot {
    private Long id;
    private int slotNumber;
    private boolean occupied;
    private String sensorId;

    // Empty constructor (required for Retrofit/Gson)
    public ParkingSlot() {
    }

    // Getters - used to read the data
    public Long getId() {
        return id;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public String getSensorId() {
        return sensorId;
    }

    // Setters - used to set the data
    public void setId(Long id) {
        this.id = id;
    }

    public void setSlotNumber(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
}
