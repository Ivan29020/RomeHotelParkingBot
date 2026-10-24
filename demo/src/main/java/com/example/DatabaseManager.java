package com.example;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DatabaseManager {

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    private Connection connection;

    static {
        // Carica le informazioni del database dal file XML
        Document document = XmlUtil.loadDocument("config.xml");
        if (document != null) {
            Element root = document.getDocumentElement();
            URL = XmlUtil.getElementValue(root, "url");
            USER = XmlUtil.getElementValue(root, "user");
            PASSWORD = XmlUtil.getElementValue(root, "password");
        }
    }

    public DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // carica il driver JDBC di MySQL
            connection = DriverManager.getConnection(URL, USER, PASSWORD); // ottiene la connessione dal metodo
                                                                           // getConnection(url,user,password) diverso
                                                                           // dal getConnection() sotto
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() { // getter per prendere la connessione
        return connection;
    }

    public User login(String username, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String salt = rs.getString("salt");
                String storedHashedPassword = rs.getString("password");
                String hashedInputPassword = hashPasswordWithSalt(password, salt);
                // 3. Confronta gli hash
                if (storedHashedPassword.equals(hashedInputPassword)) {
                    String username1 = rs.getString("username");
                    String name = rs.getString("name");
                    String surname = rs.getString("surname");
                    String role = rs.getString("role");
                    String cf = rs.getString("cf");
                    String telephonNumber = rs.getString("telephoneNumber");

                    return new User(username1, storedHashedPassword, name, surname, role, cf, telephonNumber);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private  String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16]; // Lunghezza del salt (16 byte è comune)
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPasswordWithSalt(String password, String salt) {
        try {
            // Combina password e salt
            String input = password + salt;

            // Crea l'istanza di SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes());

            // Codifica l'array di byte in Base64
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (Exception e) {
            throw new RuntimeException("Errore durante l'hashing della password", e);
        }
    }

    public String registerUser(String username, String password, String name, String surname, String role, String cf,
            String telephoneNumber) {
        try {
            String salt = generateSalt();
            String hashedPassword = hashPasswordWithSalt(password, salt);
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO Users (username, password, name, surname, role, cf, telephoneNumber, salt) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            ps.setString(3, name);
            ps.setString(4, surname);
            ps.setString(5, role);
            ps.setString(6, cf);
            ps.setString(7, telephoneNumber);
            ps.setString(8, salt);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0)
                return "true"; // Restituisce true se almeno una riga è stata aggiunta
        } catch (SQLIntegrityConstraintViolationException e) {
            if (e.getSQLState().equals("23000") && e.getMessage().contains("Duplicate entry")) { // 23000 è il codice di
                                                                                                 // stato SQL che
                                                                                                 // rappresenta una
                                                                                                 // violazione dei
                                                                                                 // vincoli di integrità
                System.out.println("Utente già registrato.");
                return "Utente già registrato"; // Restituisce un messaggio specifico in caso di duplicato
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error"; // Restituisce false in caso di eccezione
        }
        return null;
    }
}
