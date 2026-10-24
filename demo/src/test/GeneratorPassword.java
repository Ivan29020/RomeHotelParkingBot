import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

public class GeneratorPassword {

    // Metodo per generare un salt casuale
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];  // Salt di 16 byte
        random.nextBytes(salt); // Popola il salt con valori casuali
        return Base64.getEncoder().encodeToString(salt); // Converte il salt in una stringa Base64
    }

    // Metodo per fare l'hash della password combinata con il salt
    public static String hashPasswordWithSalt(String password, String salt) {
        try {
            // Combina password e salt
            String input = password + salt;

            // Crea l'istanza di SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes()); // Crea l'hash della combinazione

            // Restituisce l'hash in formato Base64
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Errore durante l'hashing della password", e);
        }
    }

    // Metodo principale per testare la generazione di salt e hash
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Ciclo per chiedere le password fino a quando l'utente non decide di fermarsi
        while (true) {
            System.out.print("Inserisci la password (digita 'exit' per uscire): ");
            String password = scanner.nextLine();

            // Se l'utente digita "exit", il programma termina
            if (password.equalsIgnoreCase("exit")) {
                System.out.println("Uscita dal programma.");
                break; // Esce dal ciclo
            }

            // Genera un salt casuale
            String salt = generateSalt();
            System.out.println("Salt generato: " + salt);

            // Hash della password con il salt generato
            String hashedPassword = hashPasswordWithSalt(password, salt);
            System.out.println("Password con salt hashata: " + hashedPassword);
            System.out.println(); // Aggiunge una riga vuota per separare i tentativi
        }
        
        scanner.close(); // Chiude lo scanner
    }
}
