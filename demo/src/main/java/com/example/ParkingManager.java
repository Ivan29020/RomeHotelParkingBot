package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public class ParkingManager {
    private DatabaseManager dbManager;

    public ParkingManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    public boolean deleteVehicle(String name){
        Connection conn = dbManager.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM vehicle WHERE name = ?");
            ps.setString(1, name);
            int  rowsAffected = ps.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("Eliminate " + rowsAffected + " con successo");
                return true;
            }else 
                return false;     
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }
    public String registerVehicle(Vehicle Vehicle, String type) {
        Connection conn = dbManager.getConnection();
        try {
            // Check if user exists
            PreparedStatement checkUserPs = conn.prepareStatement("SELECT COUNT(*) FROM Users WHERE username = ?");
            checkUserPs.setString(1, Vehicle.getUsername());
            ResultSet rs = checkUserPs.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("L'utente non esiste");
                return "🔴User does not exist";
            }

            // Insert Vehicle
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Vehicle (name, brand, licensePlate, color, numberOfWheels, username, type) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, Vehicle.getName());
            ps.setString(2, Vehicle.getBrand());
            ps.setString(3, Vehicle.getLicensePlate());
            ps.setString(4, Vehicle.getColor());
            ps.setInt(5, Vehicle.getNumberOfWheels());
            ps.setString(6, Vehicle.getUsername());
            ps.setString(7, type);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Inserimento riuscito");
                return "true";
            } else {
                System.out.println("Nessuna riga inserita");
                return "false";
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Errore di vincolo di chiave esterna: " + e.getMessage());
            return "❗Vehicle already registered";
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { //una violazione del vincolo UNIQUE
                System.err.println("Vehicle already registered.");
                return "❗Vehicle already registered";
            } else {
                System.err.println("Errore durante l'inserimento: " + e.getMessage());
                return "false";
            }
        }
    }

    public String parkingVehicle(Vehicle veicolo) {
        Connection conn = dbManager.getConnection();
        try {
            // Check if the Vehicle exists
            //System.out.println("VERIFICO L'ESISTENZA DEL VEICOLO");
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Vehicle WHERE name = ?");
            ps.setString(1, veicolo.getName());
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("Veicolo non trovato");
                return "❗Vehicle does not exist"; // Veicolo non trovato
            }

            // Check for a free spot
            System.out.println("CERCO IL PARCHEGGIO LIBERO");
            ps = conn.prepareStatement("SELECT * FROM Parking WHERE is_occupied = 0 LIMIT 1");
            rs = ps.executeQuery();

            if (rs.next()) {
                String spotId = rs.getString("id_parking");
                System.out.println("Parcheggio trovato: " + spotId);
                // Update parking spot
                ps = conn.prepareStatement("UPDATE Parking SET is_occupied = 1, name = ? WHERE id_parking = ?");
                ps.setString(1, veicolo.getName());
                ps.setString(2, spotId);
                System.out.println("MODIFICATO IL PARCHEGGIO");
                veicolo.park();
                ps.executeUpdate();
                return spotId;
            } else {
                System.out.println("Non ci sono posti");
                return "❗Sorry no places available";
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Errore di vincolo di chiave esterna: " + e.getMessage());
            // Fornisci un messaggio di errore specifico
            return "🟢Vehicle already parked";
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No free spot or error occurred
    }

    public Vehicle getVehicleByName(String name) {
        Connection conn = dbManager.getConnection();
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Vehicle WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
    
            if (rs.next()) {
                String brand = rs.getString("brand");
                String licensePlate = rs.getString("licensePlate");
                String color = rs.getString("color");
                String username = rs.getString("username");
                int numberOfWheels = rs.getInt("numberOfWheels");
                String type = rs.getString("type");
    
                if (type.equals("Car")) {
                    return new Car(name, brand, licensePlate, color, username,numberOfWheels);
                } else if (type.equals("Bike")) {
                    return new Bike(name, brand, licensePlate, color, username,numberOfWheels);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String showVehicle(User user) {
        Connection conn = dbManager.getConnection();
        StringBuilder concatenatedString = new StringBuilder();
        int maxNameLength = "Name".length();
        int maxLicensePlateLength = "LicensePlate".length();
        int maxColorLength = "Color".length();
        int maxBrandLength = "Brand".length();
        int maxTypeLength = "Type".length();
        int maxParkingLength = "Parking".length();

        List<String[]> rows = new ArrayList<>();

        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT name, licensePlate, color, brand, type, id_parking FROM Vehicle LEFT JOIN Parking USING (name) WHERE username = ?");
            ps.setString(1, user.getUsername());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String licensePlate = rs.getString("licensePlate");
                String color = rs.getString("color");
                String brand = rs.getString("brand");
                String type = rs.getString("type");
                String id_parking = rs.getString("id_parking") == null ? "N/A" : rs.getString("id_parking");

                // Determina la lunghezza massima di ciascun campo
                if (name.length() > maxNameLength)
                    maxNameLength = name.length();
                if (licensePlate.length() > maxLicensePlateLength)
                    maxLicensePlateLength = licensePlate.length();
                if (color.length() > maxColorLength)
                    maxColorLength = color.length();
                if (brand.length() > maxBrandLength)
                    maxBrandLength = brand.length();
                if (type.length() > maxTypeLength)
                    maxTypeLength = type.length();
                if (id_parking.length() > maxParkingLength)
                    maxParkingLength = id_parking.length();

                rows.add(new String[] { name, licensePlate, color, brand, type, id_parking });
            }

            // Crea la stringa di formato basata sulle lunghezze massime
            String formatString = "%s %-" + (maxNameLength) + "s %-" + (maxLicensePlateLength - 2) + "s %-"
                    + (maxColorLength) + "s %-" + (maxBrandLength + 1) + "s %-" + (maxTypeLength+1) + "s %-"
                    + (maxParkingLength + 2) + "s";

            // Crea e aggiungi l'intestazione
            String header = String.format(formatString, "", "Name", "LicensePlate", "Color", "Brand", "Type",
                    "Parking");
            concatenatedString.append("```\n"); // Apre il blocco di codice per la formattazione Telegram Markdown
            concatenatedString.append(header).append("\n");

            // Crea la linea di separazione
            int headerLength = header.length()-5;
            String separator = "-".repeat(headerLength);
            concatenatedString.append(separator).append("\n");

            // Aggiungi le righe dei dati
            for (String[] row : rows) {
                String emoji = "";
                switch (row[4].toLowerCase()) {
                    case "car":
                        emoji = "🚗";
                        break;
                    case "bike":
                        emoji = "🏍️";
                        break;
                    default:
                        emoji = "🚙";
                        break;
                }
                concatenatedString
                        .append(String.format(formatString, emoji, row[0], row[1], row[2], row[3], row[4], row[5]))
                        .append("\n");
            }
            concatenatedString.append("```"); // Chiude il blocco di codice per la formattazione Telegram Markdown
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return concatenatedString.toString();
    }

    public String showInfoVehicle(String name,User user){
        Connection conn = dbManager.getConnection();
        Vehicle vehicle = getVehicleByName(name);
        System.out.println(name);
        try{
            PreparedStatement ps = conn.prepareStatement("SELECT username, id_parking FROM vehicle JOIN parking USING(name) WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) { // Verifica che il ResultSet non sia vuoto
                if (user.getUsername().equals(rs.getString("username"))) {
                    return vehicle.info() + " park at " + rs.getString("id_parking") + " spot"; 
                } else {    
                    return "❌Non ho trovato il veicolo";
                }
            } else {
                return "❌Non ho trovato il veicolo"; // Nome non trovato nel database
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Stampa l'eccezione per un debug più dettagliato
            return "❌Si è verificato un errore durante la ricerca del veicolo.";}
     }



    public String listVehicle() {
        Connection conn = dbManager.getConnection();
        StringBuilder concatenatedString = new StringBuilder();
        int maxNameLength = "Name".length();
        int maxLicensePlateLength = "LicensePlate".length();
        int maxColorLength = "Color".length();
        int maxBrandLength = "Brand".length();
        int maxTypeLength = "Type".length();
        int maxParkingLength = "Parking".length();

        List<String[]> rows = new ArrayList<>();

        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT name, licensePlate, color, brand, type, id_parking FROM Vehicle LEFT JOIN Parking USING (name)");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String licensePlate = rs.getString("licensePlate");
                String color = rs.getString("color");
                String brand = rs.getString("brand");
                String type = rs.getString("type");
                String id_parking = rs.getString("id_parking") == null ? "N/A" : rs.getString("id_parking");

                // Determina la lunghezza massima di ciascun campo
                if (name.length() > maxNameLength)
                    maxNameLength = name.length();
                if (licensePlate.length() > maxLicensePlateLength)
                    maxLicensePlateLength = licensePlate.length();
                if (color.length() > maxColorLength)
                    maxColorLength = color.length();
                if (brand.length() > maxBrandLength)
                    maxBrandLength = brand.length();
                if (type.length() > maxTypeLength)
                    maxTypeLength = type.length();
                if (id_parking.length() > maxParkingLength)
                    maxParkingLength = id_parking.length();

                rows.add(new String[] { name, licensePlate, color, brand, type, id_parking });
            }

            // Crea la stringa di formato basata sulle lunghezze massime
            String formatString = "%s %-" + (maxNameLength) + "s %-" + (maxLicensePlateLength - 2) + "s %-"
                    + (maxColorLength) + "s %-" + (maxBrandLength + 1) + "s %-" + (maxTypeLength+1) + "s %-"
                    + (maxParkingLength + 2) + "s";

            // Crea e aggiungi l'intestazione
            String header = String.format(formatString, "", "Name", "LicensePlate", "Color", "Brand", "Type",
                    "Parking");
            concatenatedString.append("```\n"); // Apre il blocco di codice per la formattazione Telegram Markdown
            concatenatedString.append(header).append("\n");

            // Crea la linea di separazione
            int headerLength = header.length()-5;
            String separator = "-".repeat(headerLength);
            concatenatedString.append(separator).append("\n");

            // Aggiungi le righe dei dati
            for (String[] row : rows) {
                String emoji = "";
                switch (row[4].toLowerCase()) {
                    case "car":
                        emoji = "🚗";
                        break;
                    case "bike":
                        emoji = "🏍️";
                        break;
                    default:
                        emoji = "🚙";
                        break;
                }
                concatenatedString
                        .append(String.format(formatString, emoji, row[0], row[1], row[2], row[3], row[4], row[5]))
                        .append("\n");
            }
            concatenatedString.append("```"); // Chiude il blocco di codice per la formattazione Telegram Markdown
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return concatenatedString.toString();
    }
    public boolean toFreeSpot(String name) { //per "liberare un parcheggio"
        Connection conn = dbManager.getConnection();
        try {
            // Find the car
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM Vehicle WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String carName = rs.getString("name");
                // Free the parking spot
                ps = conn.prepareStatement("UPDATE Parking SET is_occupied = FALSE, name = NULL WHERE name = ?");
                ps.setString(1, carName);
                int rowsAffected = ps.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Aggiornamento eseguito correttamente.");

                    return true;

                } else {
                    System.out.println("Nessun parcheggio trovato con il nome specificato: " + carName);
                    return false;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // No matching car found
    }
}
