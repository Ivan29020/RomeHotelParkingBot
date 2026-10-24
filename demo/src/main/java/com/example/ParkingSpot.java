package com.example;

public class ParkingSpot {
    private String idParking;
    private boolean isOccupied;
    private String name;

    public ParkingSpot(String idParking,boolean isOccupied,String name) {
        this.idParking = idParking;
        this.isOccupied = false;
        this.name = name;
    }

    public String getIdParking() {
        return idParking;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

   
}
