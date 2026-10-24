package com.example;

public abstract class Vehicle {
    
    private String name;
    private String licensePlate;
    private String color;
    private String brand;
    private int numberOfWheels;
    private String username;
    


    public Vehicle(String name,String brand, String licensePlate, String color, String username,int numberOfWheels) {
        this.name = name;
        this.brand = brand;
        this.licensePlate = licensePlate;
        this.color = color;
        this.username = username;
        this.numberOfWheels = numberOfWheels;
        
    }

    public abstract String park();
    public abstract String info();
    
    public String getName(){
        return name;
    }
    public int getNumberOfWheels(){
        return numberOfWheels;
    }
    public void setNumberOfWheels(int numberOfWheels){
        this.numberOfWheels=numberOfWheels;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
   
}
    

