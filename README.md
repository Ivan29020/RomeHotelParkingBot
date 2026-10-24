# RomeHotelParkingBot

Welcome to the **RomeHotelParkingBot** project! This bot was developed to simplify the parking experience for hotel guests during their stay, allowing vehicles and parking spaces to be managed efficiently and intuitively.

## Main Features

With **RomeHotelParkingBot**, guests can:

- View their registered vehicles.
- Check the parking status of their vehicles.
- Get information about the location and parking status of their vehicles.

Administrators can:

- Register new users and vehicles.
- Manage vehicle entry and exit procedures.

## Getting Started

### For Guests

1. If this is your first time staying at the hotel, contact an administrator to create your account.
2. Once registered, log in using the credentials provided.
3. Use the bot commands to manage your vehicles.

### Registering a Vehicle

1. Provide the necessary information to an administrator.
2. When you arrive at the parking area, the administrator will tell you where to park and open the entrance gate.
3. To exit, contact the administrator at the exit booth.

## Available Commands

### For Administrators

- **Register a vehicle:**

  `/vehicleRegister <type> <name> <brand> <licensePlate> <color> <customerName>`

- **Create a user profile:**

  `/register <username> <password> <name> <surname> <cf> <telephoneNumber>`

- **Park a vehicle:**

  `/parking <name>`

- **Log in:**

  `/login <username> <password>`

- **Retrieve a vehicle:**

  `/retrieve <name>`

- **Display all vehicles:**

  `/list`

- **Delete a vehicle:**

  `/deleteVehicle <name>`

### For Customers

- **General Information:**

  `/help` ➔ Displays the available commands.

  `/maps` ➔ Shows the hotel's location.

  `/city` ➔ Provides information about Rome.

- **Log in:**

  `/login <username> <password>`

- **View all your vehicles:**

  `/vehicle`

- **Check the status of a specific vehicle:**

  `/infoVehicle <name>`

## Project Requirements

### Implemented Features

- **Object-Oriented Programming**, using:
  - Encapsulation (**information hiding**).
  - Interfaces (➔ `Runnable`).
  - Abstract classes (➔ `Vehicle`).
  - Inheritance (➔ `Bike`, `Car`).
  - Polymorphism (➔ `info()`, `park()`).

- **Threads:** Timeout management to stop the server.
- **Multithreading.**
- **Exception handling** using `try` and `catch`.
- **XML configuration files:**
  - Database configuration (`config.xml`) and client-server communication settings.
- **Client-server communication:** Support for TCP, UDP, or REST (➔ `Server.java`, `Bot.java`).
- **Database connection** (➔ `DatabaseManager.java`).
- **XML parsing** (➔ `XmlUtil.java`, using a DOM parser).
- **Maven:** Project management (➔ `pom.xml`).

## Implemented Optional Requirements

- Full Maven support.

## Conceptual Database Schema

![image](https://github.com/user-attachments/assets/28c35004-4987-4d3b-b355-0917b2d4341e)

---

Thank you for your interest in **RomeHotelParkingBot**! For more information or to contribute to the project, visit the [Issues](../../issues) section or submit a [Pull Request](../../pulls).
