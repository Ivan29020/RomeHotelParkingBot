package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import java.security.KeyStore;
import java.net.SocketException;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Server implements Runnable {
    private volatile boolean running = true;
    private SSLServerSocket serverSocket;
    private static int PORT;
    private static String OUTPUT_FILE;
    private Thread serverThread;

    static {
        Document document = XmlUtil.loadDocument("config.xml");
        if (document != null) {
            Element root = document.getDocumentElement();
            PORT = Integer.parseInt(XmlUtil.getElementValue(root, "port"));
            OUTPUT_FILE = XmlUtil.getElementValue(root, "file");
        }
    }

    public void run() {
        // Sposta setupSSLContext all'interno del run in modo che
        // l'eccezione venga catturata e l'applicazione venga arrestata
        SSLContext sslContext = setupSSLContext();
        if (sslContext == null) {
            System.err.println("SSL context setup failed. Server will not start.");
            stop(); // Chiama stop() anche qui per assicurarsi che il thread si fermi
            return; // Non avviare il server se SSLContext è null
        }

        try {
            SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);
            System.out.println("Secure Server is listening on port " + PORT);

            while (running) {
                SSLSocket clientSocket = null;
                BufferedReader in = null;
                BufferedWriter out = null;
                try {
                    clientSocket = (SSLSocket) serverSocket.accept();
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new BufferedWriter(new FileWriter(OUTPUT_FILE, true));

                    clientSocket.setSoTimeout(60000); // Timeout di 60 secondi per la lettura

                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Received: " + message);
                        out.write(message);
                        out.newLine();
                    }
                } catch (SocketException e) {
                    if (running) {
                        System.err.println("Client connection interrupted: " + e.getMessage());
                    }
                    // La SocketException è lanciata quando la socket viene chiusa forzatamente.
                    // Usciamo dal loop se stiamo interrompendo il server.
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Exception handling client: " + e.getMessage());
                        e.printStackTrace();
                    }
                } finally {
                    try {
                        if (out != null)
                            out.close();
                    } catch (IOException e) {
                        System.err.println("Errore nella chiusura del BufferedWriter: " + e.getMessage());
                    }
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        System.err.println("Errore nella chiusura del BufferedReader: " + e.getMessage());
                    }
                    try {
                        if (clientSocket != null)
                            clientSocket.close();
                    } catch (IOException e) {
                        System.err.println("Errore nella chiusura della Socket: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not start secure server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop(); // Chiama stop() nel finally per assicurare che le risorse vengano liberate
        }
    }

    public void stop() {
        running = false;
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt(); // Interrompi il thread
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    System.out.println("Secure Server stopped.");
                }
                serverThread.join(); // Aspetta che il thread termini
            } catch (IOException | InterruptedException e) {
                System.err.println("Error stopping server: " + e.getMessage());
                e.printStackTrace();
            }
        }
        // Non chiamare System.exit qui. Lascia che il main gestisca l'uscita.
    }

    private SSLContext setupSSLContext() {
        try {
            String keystorePath = "demo\\TSLCertificate\\server.keystore"; // Percorso del keystore
            char[] keystorePassword = "RomeCertificate24".toCharArray(); // Password del keystore
            char[] keyPassword = "RomeCertificate24".toCharArray(); // Password della chiave (se diversa dalla password
                                                                    // del keystore)

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream keystoreStream = new FileInputStream(keystorePath)) {
                keyStore.load(keystoreStream, keystorePassword);
            }
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyPassword);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            System.setProperty("jdk.tls.rejectClientInitiatedRenegotiation", "true");
            return sslContext;
        } catch (FileNotFoundException e) {
            System.err.println("Keystore file not found: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Termina l'applicazione con codice di errore
            return null; // Non raggiungibile, ma necessario per la compilazione
        } catch (Exception e) {
            System.err.println("SSLContext setup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Termina l'applicazione con codice di errore
            return null; // Non raggiungibile, ma necessario per la compilazione
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Bot());
        } catch (TelegramApiException e) {
            System.err.println("Errore durante l'avvio del bot: " + e.getMessage());
            e.printStackTrace();
        }

        Server server = new Server();
        Thread serverThread = new Thread(server);
        server.serverThread = serverThread;
        // Avvia il serverThread solo se setupSSLContext non lancia eccezione
        serverThread.start();

        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.stop();
    }
}