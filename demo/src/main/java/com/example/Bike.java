package com.example;

public class Bike extends Vehicle {
    

    public Bike(String name,String brand, String licensePlate, String color, String username,int numberOfWheels) {
            super(name,brand, licensePlate, color, username, numberOfWheels);
        }
    @Override
    public String park() {
        return "🏍️ The bike  " + getName() + " parked at ";
    }    
    @Override
    public String info(){
        return  "🏍️ Bike " + getName() +" "+ getBrand()+" " + getLicensePlate() +" "+ getColor() +" "+ getNumberOfWheels();
    }


    
}
