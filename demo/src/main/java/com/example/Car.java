package com.example;

public class Car extends Vehicle {

    public Car(String name,String brand, String licensePlate, String color, String username,int numberOfWheels) {
            super(name,brand, licensePlate, color, username, numberOfWheels);
        }

    @Override
    public String park() {
        return "🚗 The car " + getName() + " parked at ";
    }
    @Override
    public String info(){
        return  "🚗 Car " + getName() +" "+ getBrand() +" "+ getLicensePlate() +" "+ getColor() +" "+ getNumberOfWheels();
    }
    
}
