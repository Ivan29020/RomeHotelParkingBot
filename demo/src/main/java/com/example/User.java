package com.example;

public class User {
    private String username;
    private String password;
    private String name;
    private String surname;
    private String role;
    private String cf;
    private String telephoneNumber;
   



    public User(String username, String password,String name,String surname,String role,String cf,String telephoneNumber) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.role = role;
        this.cf = cf;
        this.telephoneNumber = telephoneNumber;
    }
    public String getCf() {
        return cf;
    }
    public String getTelephoneNumber() {
        return telephoneNumber;
    }
    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

    public String getRole(){
        return role;
    }
    public String getName(){
        return name;
    }
    public String getSurname(){
        return surname;
    }
}
