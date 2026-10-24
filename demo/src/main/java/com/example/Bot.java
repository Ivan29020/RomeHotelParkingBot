package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("deprecation")
public class Bot extends TelegramLongPollingBot {

    private DatabaseManager dbManager = new DatabaseManager();
    private ParkingManager parkingManager = new ParkingManager(dbManager);
    private Map<Long, User> sessions = new HashMap<>();
    private boolean running = true; // Aggiunta della variabile per controllare l'esecuzione

    // Parser
    private static int serverPort;
    private static String serverAddress;

    static {
        // Carica le informazioni del database dal file XML
        Document document = XmlUtil.loadDocument("config.xml");
        if (document != null) {
            Element root = document.getDocumentElement();
            serverPort = Integer.parseInt(XmlUtil.getElementValue(root, "port"));
            serverAddress = XmlUtil.getElementValue(root, "address");
        }
    }

    User user = null;

    @Override
    public void onUpdateReceived(Update update) {
        if (!running)
            return; // se il bot non è running non fa niente

        if (update.hasMessage() && update.getMessage().hasText()) { // se riceve un messaggio con testo
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Crea una connessione sicura solo se SSL è riuscito
            try (SSLSocket socket = createSecureSocket(serverAddress, serverPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                // Se la connessione SSL è riuscita, invia il messaggio
                out.println(messageText);
            } catch (SSLHandshakeException e) {
                System.err.println("SSL handshake failed: " + e.getMessage());
                e.printStackTrace();
                stop(); // Chiama stop() per arrestare il bot in caso di handshake fallito
            } catch (SocketTimeoutException e) {
                System.err.println("Socket timed out: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("IO exception: " + e.getMessage());
                e.printStackTrace();
                stop(); // Chiama stop() per arrestare il bot in caso di eccezioni I/O
            } catch (Exception e) {
                // Se c'è un errore SSL, non inviare il messaggio
                System.err.println("SSL connection failed. The message will not be sent: " + e.getMessage());
                sendMsg(chatId, "🔴 **Connection failed!** The bot cannot connect to the server.");
                return; // Non proseguire con il resto dell'elaborazione
            }

            // Gestisci il comando (se la connessione è riuscita, altrimenti non fare nulla)
            handleCommand(messageText, chatId);
        }

    }

    private SSLSocket createSecureSocket(String address, int port) throws Exception {
        // Carica il keystore con il certificato di fiducia
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream trustStream = new FileInputStream("demo\\TSLCertificate\\client.keystore")) {
            trustStore.load(trustStream, "ClientCertificate24".toCharArray());
        }

        // Crea il TrustManagerFactory con il keystore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Crea un SSLContext con il TrustManager personalizzato
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);

        // Ottieni un SSLSocketFactory dal contesto SSL personalizzato
        SSLSocketFactory factory = sslContext.getSocketFactory();

        // Crea e configura il socket SSL
        SSLSocket socket = (SSLSocket) factory.createSocket(address, port);
        socket.setEnabledProtocols(new String[] { "TLSv1.2" });
        try {
            socket.startHandshake(); // Inizia la connessione SSL
        } catch (SSLException e) {
            System.err.println("SSL Handshake failed: " + e.getMessage());
            System.exit(1); // Arresta il bot
            throw new Exception("TLS handshake failed", e); // Rilancia l'eccezione
        }

        return socket;
    }

    // Metodo per fermare il bot e settare running a false
    public void stop() {
        running = false;
        System.exit(1); // Chiama System.exit per arrestare l'applicazione
    }

    public void sendMsg(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setParseMode("Markdown"); // Abilita la modalità Markdown
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    // resto dei metodi handle ...
    private void handleCommand(String messageText, long chatId) {
        if (messageText.equals("/start")) {
            sendWelcomeMessage(chatId);
        } else if (messageText.equals("/help")) {
            handleHelpCommand(chatId);
        } else if (messageText.equals("/info")) {
            handleInfoCommand(chatId);
        } else if (messageText.equals("/maps")) {
            handleMapsCommand(chatId);
        } else if (messageText.equals("/city")) {
            handleCityCommand(chatId);
        } else if (messageText.startsWith("/login")) {
            handleLoginCommand(messageText, chatId);
        } else {
            user = sessions.get(chatId);
            if (user == null) {
                sendMsg(chatId, "❗Please log in first using /login <username> <password>.");
                return;
            }
            String command = messageText.split(" ")[0];
            switch (command) {
                case "/deleteVehicle":
                    if ("Admin".equals(user.getRole())) {
                        handleDeleteVehicleCommand(messageText, chatId);
                    } else {
                        sendMsg(chatId, "🚫You do not have permission to use this command.");
                    }
                    break;
                case "/parking":
                    if ("Admin".equals(user.getRole())) {
                        handleParkingVehicleCommand(messageText, chatId);
                    } else {
                        sendMsg(chatId, "🚫You do not have permission to use this command.");
                    }
                    break;
                case "/register":
                    if ("Admin".equals(user.getRole())) {
                        handleRegisterUserCommand(messageText, chatId);
                    } else {
                        sendMsg(chatId, "🚫You do not have permission to use this command.");
                    }
                    break;
                case "/retrieve":
                    if ("Admin".equals(user.getRole())) {
                        handleRetrieveCommand(messageText, chatId);
                    } else {
                        sendMsg(chatId, "🚫You do not have permission to use this command.");
                    }

                    break;
                case "/vehicleRegister":
                    if ("Admin".equals(user.getRole())) {
                        handleRegisterVehicleCommand(messageText, chatId);
                    } else {
                        sendMsg(chatId, "🚫You do not have permission to use this command.");
                    }

                    break;
                case "/vehicle":
                    handleShowVehicleCommand(chatId);
                    break;

                case "/infoVehicle":
                    handleShowInfoVehicle(messageText, chatId);

                    break;
                case "/list":
                    if ("Admin".equals(user.getRole())) {
                        handleListCommand(chatId);
                    } else {
                        sendMsg(chatId, "🚫You do not have permission to use this command.");
                    }
                    break;

                default:
                    sendUnknownCommandMessage(chatId);
                    break;
            }
        }
    }

    private void handleHelpCommand(long chatId) {
        String helpMessage = "📋 **Help - List of Commands** 📋\n\n" +
                "Here are the available commands you can use:\n\n" +
                "*Admin*\n" +
                "- /register `<username> <password> <name> <surname> <cf> <telephoneNumber>` ➤ Register the customer in our database.\n"
                +
                "- /login `<username> <password>` ➤ Log in to your account.\n" +
                "- /parking `<name>` ➤ Parking the customer's vehicle.\n" +
                "- /vehicleRegister `<type> <name> <brand> <licensePlate> <color> <customerName>` ➤ Register the customer's vehicle in the database.\n"
                +
                "- /retrieve `<name>` ➤ retrieve the customer's vehicle. \n\n" +
                "- /list ➤ show all vehicle in the database. \n\n" +
                "- /deleteVehicle `<name>`➤ remove a registered vehicle in the database. \n\n" +
                "\n*Customer / Admin*\n" +
                "- /start  ➤ Start the bot.\n" +
                "- /help  ➤ Show the command that you can use.\n" +
                "- /maps  ➤ show the position of the hotel.\n" +
                "- /city  ➤ show about Rome.\n" +
                "- /login `<username> <password>` ➤ Log in to your account.\n" +
                "- /vehicle ➤ show all your vehicle. \n\n" +
                "- /infoVehicle <name>  ➤ show info about the specific vehicle.\n" +
                "If you need further assistance, please contact the administrator.";

        sendMsg(chatId, helpMessage);
    }

    private void handleInfoCommand(long chatId) {
        String infoMessage = "Welcome to RomeHotelParkingBot! 🚗🏨\n\n" +
                "I'm here to simplify your parking experience during your stay at our hotel🏨.\n" +
                "With this bot 🤖, you can view your registered cars and find out if they're parked, as well as where they're parked.\n\n"
                +
                "Using the hotel parking is easy. If it's your first time here and you don't have an account yet🕵️, contact one of our representatives.\n"
                +
                "They'll register you by providing the necessary information👨‍💻. Once you have an account, you can use the /vehicle command to view the status of all your cars.\n\n"
                +
                "To register a vehicle🚗, follow the same procedure: provide the required information to a representative, and your vehicle will be registered.\n"
                +
                "When you need to park, speak to a representative at the parking entrance🅿️. They'll show you where to park and open the passage 🚪 to let you through.\n"
                +
                "The same applies when you need to exit: go to the booth at the parking exit, and the representative will open the 🟥 barrier to let you out with your vehicle.\n\n"
                +
                "We're here to make your stay as comfortable as possible. If you have any questions or need assistance, feel free to ask one of our friendly staff members.\n\n"
                +
                "Happy parking! 🛎️🗝️🚦";

        sendMsg(chatId, infoMessage);
    }

    private void handleCityCommand(long chatId) {
        String infoMessage = "🏛️ *Welcome to Rome!* 🏛️\n\n" +
                "Rome, the capital city of Italy, is a historic and cultural gem. Here are some interesting facts and highlights about this ancient city:\n\n"
                +
                "📜 *History & Culture*\n" +
                "• Founded: 753 BC\n" +
                "• Known as the 'Eternal City'\n" +
                "• Home to the Roman Empire, one of the greatest civilizations in history\n\n" +
                "🎨 *Arts & Architecture*\n" +
                "• Colosseum: The largest ancient amphitheater ever built\n" +
                "• Pantheon: A former Roman temple, now a church, with a magnificent dome\n" +
                "• Vatican City: The smallest independent state in the world, home to St. Peter's Basilica and the Vatican Museums\n\n"
                +
                "🌟 *Famous Landmarks*\n" +
                "• Trevi Fountain: A stunning Baroque fountain where you can toss a coin to ensure a return to Rome\n" +
                "• Roman Forum: The center of ancient Rome's public life\n" +
                "• Spanish Steps: A monumental staircase of 135 steps\n\n" +
                "🍝 *Cuisine*\n" +
                "• Pasta: Enjoy traditional dishes like Carbonara and Amatriciana\n" +
                "• Pizza: Try the classic Roman-style pizza\n" +
                "• Gelato: Indulge in Italy's famous ice cream\n\n" +
                "🌍 *Location*\n" +
                "• Latitude: 41.9028° N\n" +
                "• Longitude: 12.4964° E\n\n" +
                "📞 *Contact*\n" +
                "• Tourist Information: Call +39 06 0608 for assistance\n\n" +
                "We hope you enjoy your stay in this magnificent city! 😊";

        sendMsg(chatId, infoMessage);
    }

    private void handleMapsCommand(long chatId) {
        double latitude = 41.8902102;
        double longitude = 12.4922309;
        sendLocationWithCaption(chatId, latitude, longitude, "Here is the Colosseum 🏛️ in Rome:");
    }

    private void sendLocationWithCaption(long chatId, double latitude, double longitude, String caption) {
        SendLocation location = new SendLocation();
        location.setChatId(String.valueOf(chatId));
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        try {
            sendMsg(chatId, caption);
            execute(location);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String description = "Welcome ❣️. This is the list of commands present in the bot:\n\n" +
                "Log in with the credentials provided to you\n" +
                "/login - <username> <password>\n" +
                "/help  for show the list of commands \n\n" +
                "For more information, contact your administrator. We wish you a good stay! 👨‍✈️";

        // Impostazione del percorso dell'immagine
        String imagePath = "demo/assets/20220127150143-colosseo-roma-lawebpzio-shutterstock-756032350.webp"; // Sostituisci

        // Creazione dell'oggetto SendPhoto
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(new File(imagePath))); // Carica l'immagine dal percorso specificato
        sendPhoto.setCaption(description); // Aggiunge la descrizione all'immagine

        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteVehicleCommand(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            String name = parts[1].toLowerCase();
            if (parkingManager.deleteVehicle(name)) {
                sendMsg(chatId, "🟢 Vehicle " + name + " successfully deleted ");
            } else
                sendMsg(chatId, "🔴 Error deleting the vehicle");

        }

    }

    private void handleLoginCommand(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 3) {
            String username = parts[1];
            String password = parts[2];

            User user = dbManager.login(username, password);
            if (user != null) {
                sessions.put(chatId, user);
                sendMsg(chatId, "🟢 Login successful. Welcome " + user.getRole() + " " + user.getName() + " "
                        + user.getSurname());
            } else {
                sendMsg(chatId, "🔴 Invalid username or password.");
            }
        } else {
            sendMsg(chatId, "❗ Usage: /login <username> <password>");
        }
    }

    private void handleRegisterVehicleCommand(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 7) {
            String type = parts[1].toLowerCase();
            String name = parts[2];
            String brand = parts[3];
            String licensePlate = parts[4];
            String color = parts[5];
            String username = parts[6];
            String msg = "";
            Vehicle vehicle = null; // Inizializzazione della variabile vehicle

            switch (type) {
                case "car":
                    vehicle = new Car(name, brand, licensePlate, color, username, 4);
                    break;
                case "bike":
                    vehicle = new Bike(name, brand, licensePlate, color, username, 2);
                    break;
                default:
                    // Gestione del caso in cui il tipo non è riconosciuto
                    sendMsg(chatId, "🔴 Tipo veicolo non riconosciuto");
                    break;
            }
            if (vehicle instanceof Car) {
                msg = parkingManager.registerVehicle(vehicle, "Car");
                if (msg.equals("true")) {
                    sendMsg(chatId, "🟢 Car registered ");
                } else {
                    sendMsg(chatId, msg);
                }
            } else {
                msg = parkingManager.registerVehicle(vehicle, "Bike");
                if (msg.equals("true")) {
                    sendMsg(chatId, "🟢 Bike registered ");
                } else {
                    sendMsg(chatId, msg);
                }
            }

        } else {
            sendMsg(chatId, "❗ Usage: /vehicleRegister <type> <name> <brand> <licensePlate> <color>");
        }
    }

    private void handleParkingVehicleCommand(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            String name = parts[1];
            Vehicle Vehicle = parkingManager.getVehicleByName(name); // restituisce o una macchina o una moto
            if (Vehicle != null) {
                String spot = parkingManager.parkingVehicle(Vehicle);
                if (spot.startsWith("P")) {
                    sendMsg(chatId, "🟢 " + Vehicle.park() + spot + " spot"); // in base a che veicolo ha trovato stampa
                                                                              // il relativo park()
                } else {
                    sendMsg(chatId, spot);
                }
            } else {
                sendMsg(chatId, "🔴 Vehicle not found");
            }
        }
    }

    private void handleRegisterUserCommand(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 7) {
            System.out.println("ENTRO");
            String username = parts[1];
            String password = parts[2];
            String name = parts[3];
            String surname = parts[4];
            String cf = parts[5];
            String telephoneNumber = parts[6];

            String msg = "";
            msg = dbManager.registerUser(username, password, name, surname, "Client", cf, telephoneNumber);
            if (msg.equals("true")) {
                sendMsg(chatId, "🟢 User registered");
            } else {
                sendMsg(chatId, msg);
            }
        } else {

            sendMsg(chatId, "❗ Usage: /register <username> <password> <name> <surname> <cf> <telephoneNumber>");
        }
    }

    private void handleRetrieveCommand(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            String name = parts[1];
            boolean success = parkingManager.toFreeSpot(name);
            if (success) {
                sendMsg(chatId, "🟢 Vehicle retrieved successfully.");
            } else {
                sendMsg(chatId, "🔴 Vehicle not found.");
            }
        } else {
            sendMsg(chatId, "❗ Usage: /retrieve <name>");
        }
    }

    public void handleShowVehicleCommand(long chatId) {
        String msg = parkingManager.showVehicle(user);
        sendMsg(chatId, msg);
    }

    public void handleShowInfoVehicle(String messageText, long chatId) {
        String[] parts = messageText.split(" ");
        if (parts.length == 2) {
            String name = parts[1];
            String msg = parkingManager.showInfoVehicle(name, user);
            sendMsg(chatId, msg);
        } else {
            sendMsg(chatId, "❗ Usage: /infoVehicle <name>");
        }
    }

    public void handleListCommand(long chatId) {
        String msg = parkingManager.listVehicle();
        sendMsg(chatId, msg);
    }

    private void sendUnknownCommandMessage(long chatId) {
        sendMsg(chatId, "❗ Unknown command.");
    }

    @Override
    public String getBotUsername() {
        return "RomeHotelParkingBot";
    }

    @Override
    public String getBotToken() {
        return "7142605854:AAHzWKB9lV-z-yjhrLivoAKy6nNBC-jXqlM";
    }
}