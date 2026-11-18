/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.main2;

/**
 *
 * @author Student
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main2 {


    public static void main(String[] args) {
        Scanner in = Message.INPUT;
        Login login = new Login();

        // ---------- Register ----------
        System.out.println("=== Register ===");
        boolean registered = false;
        while (!registered) {
            System.out.print("First name: ");
            String first = in.nextLine().trim();
            System.out.print("Last name: ");
            String last = in.nextLine().trim();
            System.out.print("Username (underscore, max 5 chars): ");
            String user = in.nextLine().trim();
            System.out.print("Password (8+ chars, capital, number, special): ");
            String pass = in.nextLine();
            String result = login.registerUser(first, last, user, pass);
            System.out.println(result);
            registered = result.contains("registered successfully");
        }

        // ---------- Login (must succeed before anything else) ----------
        System.out.println("\n=== Login ===");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Username: ");
            String user = in.nextLine().trim();
            System.out.print("Password: ");
            String pass = in.nextLine();
            System.out.println(login.returnLoginStatus(user, pass));
            loggedIn = login.loginUser(user, pass);
        }

        // ---------- Welcome + number of messages ----------
        System.out.println("\nWelcome to QuickChat.");

        // Load any messages that were stored to disk in a previous run.
        Message.loadStoredMessagesFromFile();

        int maxMessages = readPositiveInt(in, "How many messages do you wish to enter? ");
        int entered = 0;

        // ---------- Menu loop ----------
        boolean running = true;
        while (running) {
            System.out.println("\nMenu:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Stored Messages");
            System.out.println("4) Quit");
            System.out.print("Choose an option: ");
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1":
                    if (entered >= maxMessages) {
                        System.out.println("You have already entered the " + maxMessages
                                + " message(s) you set at the start.");
                        break;
                    }
                    if (sendOneMessage(in)) {
                        entered++;
                    }
                    break;
                case "2":
                    showSentReport();
                    break;
                case "3":
                    manageStoredMessages(in, login);
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter 1, 2, 3 or 4.");
            }
        }

        System.out.println("\nTotal number of messages sent: " + Message.returnTotalMessages());
        System.out.println("Goodbye.");
    }

    /** Captures one message. Returns true if it counted toward the message limit. */
    private static boolean sendOneMessage(Scanner in) {
        // Recipient
        String recipient;
        while (true) {
            System.out.print("Recipient cell number (with international code, e.g. +27718693002): ");
            recipient = in.nextLine().trim();
            Message probe = new Message(recipient, "x");
            String status = probe.checkRecipientCell();
            System.out.println(status);
            if (status.startsWith("Cell phone number successfully")) {
                break;
            }
        }

        // Message text
        String text;
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            text = in.nextLine();
            if (text.trim().isEmpty()) {
                System.out.println("Message cannot be empty.");
            } else if (text.length() > 250) {
                System.out.println("Please enter a message of less than 250 characters.");
            } else {
                System.out.println("Message sent");
                break;
            }
        }

        Message msg = new Message(recipient, text);
        System.out.println("Message ID generated: " + msg.getMessageID());

        String outcome = msg.SentMessage();
        System.out.println(outcome);

        if (outcome.equals("Message successfully sent.")) {
            System.out.println("\n--- Message details ---");
            System.out.print(msg.getDetails());
            return true;
        }
        return outcome.equals("Message successfully stored.");
    }

    /** Menu option 2: full report of every message that has been sent in this run. */
    private static void showSentReport() {
        String report = Message.generateReport(Message.sentMessages);
        if (report.isEmpty()) {
            System.out.println("No messages have been sent yet.");
        } else {
            System.out.println("\n--- Sent Messages Report ---");
            System.out.print(report);
        }
    }

    /** Menu option 3: the "Stored Messages" sub-menu (Part 3, requirement 2). */
    private static void manageStoredMessages(Scanner in, Login login) {
        boolean inStoredMenu = true;
        while (inStoredMenu) {
            System.out.println("\n-- Stored Messages --");
            System.out.println("a) Display sender and recipient of all stored messages");
            System.out.println("b) Display the longest stored message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for messages by recipient");
            System.out.println("e) Delete a message using the message hash");
            System.out.println("f) Display a report of all stored messages");
            System.out.println("g) Back to main menu");
            System.out.print("Choose an option: ");
            String choice = in.nextLine().trim().toLowerCase();

            switch (choice) {
                case "a":
                    displaySenderAndRecipient(login);
                    break;
                case "b":
                    String longest = Message.getLongestMessage(Message.storedMessages);
                    System.out.println(longest.isEmpty() ? "There are no stored messages."
                            : "Longest stored message: " + longest);
                    break;
                case "c":
                    System.out.print("Enter the message ID to search for: ");
                    String id = in.nextLine().trim();
                    System.out.println(Message.searchByMessageID(id, Message.storedMessages));
                    break;
                case "d":
                    System.out.print("Enter the recipient to search for: ");
                    String recipient = in.nextLine().trim();
                    List<String> found = Message.searchMessagesByRecipient(recipient, Message.storedMessages);
                    if (found.isEmpty()) {
                        System.out.println("No stored messages found for " + recipient + ".");
                    } else {
                        for (String text : found) {
                            System.out.println(text);
                        }
                    }
                    break;
                case "e":
                    System.out.print("Enter the message hash of the message to delete: ");
                    String hash = in.nextLine().trim();
                    System.out.println(Message.deleteMessageByHash(hash, Message.storedMessages));
                    break;
                case "f":
                    String report = Message.generateReport(Message.storedMessages);
                    System.out.println(report.isEmpty() ? "There are no stored messages."
                            : "\n--- Stored Messages Report ---\n" + report);
                    break;
                case "g":
                    inStoredMenu = false;
                    break;
                default:
                    System.out.println("Invalid option, please enter a, b, c, d, e, f or g.");
            }
        }
    }

    private static void displaySenderAndRecipient(Login login) {
        if (Message.storedMessages.isEmpty()) {
            System.out.println("There are no stored messages.");
            return;
        }
        for (Message m : Message.storedMessages) {
            System.out.println("Sender: " + login.getFullName() + ", Recipient: " + m.getRecipient());
        }
    }

    private static int readPositiveInt(Scanner in, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int n = Integer.parseInt(in.nextLine().trim());
                if (n > 0) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
                // fall through
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }

    /**
     * Simple registration / login handling.
     * Replace or merge with the Login class from Part 1 of your POE if you already have one.
     */
    static class Login {

        private String username;
        private String password;
        private String firstName;
        private String lastName;

        /** Username must contain an underscore and be no more than 5 characters. */
        public boolean checkUserName(String username) {
            return username != null && username.contains("_") && username.length() <= 5;
        }

        /** Password: at least 8 chars, a capital letter, a number and a special character. */
        public boolean checkPasswordComplexity(String password) {
            if (password == null || password.length() < 8) {
                return false;
            }
            boolean upper = false, digit = false, special = false;
            for (char c : password.toCharArray()) {
                if (Character.isUpperCase(c)) upper = true;
                else if (Character.isDigit(c)) digit = true;
                else if (!Character.isLetterOrDigit(c)) special = true;
            }
            return upper && digit && special;
        }

        public String registerUser(String firstName, String lastName, String username, String password) {
            if (!checkUserName(username)) {
                return "Username is not correctly formatted, please ensure that your username "
                        + "contains an underscore and is no more than five characters in length.";
            }
            if (!checkPasswordComplexity(password)) {
                return "Password is not correctly formatted, please ensure that the password contains "
                        + "at least eight characters, a capital letter, a number and a special character.";
            }
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.password = password;
            return "Username successfully captured.\nPassword successfully captured.\nUser registered successfully.";
        }

        public boolean loginUser(String username, String password) {
            return this.username != null
                    && this.username.equals(username)
                    && this.password.equals(password);
        }

        public String returnLoginStatus(String username, String password) {
            if (loginUser(username, password)) {
                return "Welcome " + firstName + " " + lastName + ", it is great to see you again.";
            }
            return "Username or password incorrect, please try again.";
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }
    }

    static class Message {

        // ---- shared state for the whole program run ----
        private static final List<Message> sentMessages = new ArrayList<>();
        private static final List<Message> disregardedMessages = new ArrayList<>();
        private static final List<Message> storedMessages = new ArrayList<>();
        private static final List<String> messageHashes = new ArrayList<>();
        private static final List<String> messageIDs = new ArrayList<>();
        private static int totalMessagesSent = 0;
        private static final Random RANDOM = new Random();
        public static String storageFile = "messages.json";
        /** One shared Scanner for the whole program (two Scanners on System.in lose input). */
        public static final Scanner INPUT = new Scanner(System.in);

        // ---- per message data ----
        private final String messageID;
        private final int numMessagesSent;   // number of this message (starts at 0)
        private final String recipient;
        private final String message;
        private final String messageHash;

        /** Recipient: international code (+ and 1-3 digits) followed by up to ten digits. */
        private static final Pattern CELL_PATTERN = Pattern.compile("^\\+\\d{1,3}\\d{1,10}$");

        public Message(String recipient, String message) {
            this(generateMessageID(), recipient, message, totalMessagesSent);
        }

        /** Constructor that lets tests supply a known message ID. */
        public Message(String messageID, String recipient, String message, int numMessagesSent) {
            this.messageID = messageID;
            this.recipient = recipient;
            this.message = message;
            this.numMessagesSent = numMessagesSent;
            this.messageHash = createMessageHash();
            registerIdentifiers();
        }

        /** Private constructor used when rebuilding a Message from a stored JSON record. */
        private Message(String messageID, String recipient, String message, String messageHash, boolean fromStorage) {
            this.messageID = messageID;
            this.recipient = recipient;
            this.message = message;
            this.numMessagesSent = -1;
            this.messageHash = messageHash;
            registerIdentifiers();
        }

        /** Rebuilds a Message exactly as it was stored, keeping its original hash. */
        public static Message fromStorage(String messageID, String recipient, String message, String messageHash) {
            return new Message(messageID, recipient, message, messageHash, true);
        }

        /** Every array that must contain all message hashes / IDs (Part 3, requirement 1). */
        private void registerIdentifiers() {
            messageIDs.add(this.messageID);
            messageHashes.add(this.messageHash);
        }

        // ------------------------------------------------------------------
        // Required methods
        // ------------------------------------------------------------------

        /** Ten-digit random ID (leading zeros allowed). */
        public static String generateMessageID() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                sb.append(RANDOM.nextInt(10));
            }
            return sb.toString();
        }

        /** Message ID must be no more than ten characters. */
        public boolean checkMessageID() {
            return messageID != null && messageID.length() <= 10;
        }

        /** Validates the recipient cell number and returns the required status message. */
        public String checkRecipientCell() {
            if (recipient != null && CELL_PATTERN.matcher(recipient).matches()) {
                return "Cell phone number successfully captured.";
            }
            return "Cell phone number is incorrectly formatted or does not contain an international "
                    + "code. Please correct the number and try again.";
        }

        /** Validates message length: must not exceed 250 characters. */
        public static String checkMessageLength(String text) {
            if (text != null && text.length() <= 250) {
                return "Message sent";
            }
            int over = (text == null) ? 0 : text.length() - 250;
            return "Message exceeds 250 characters by " + over + ", please reduce size.";
        }

        /** Hash = first two digits of ID : message number : FIRSTWORDLASTWORD (upper case). */
        public String createMessageHash() {
            String firstTwo = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
            String[] words = message.trim().split("\\s+");
            String first = cleanWord(words[0]);
            String last = cleanWord(words[words.length - 1]);
            return (firstTwo + ":" + numMessagesSent + ":" + first + last).toUpperCase();
        }

        private static String cleanWord(String word) {
            return word.replaceAll("[^A-Za-z0-9]", "");
        }

        /**
         * Lets the user choose to send, disregard or store the message.
         * 1 = Send, 2 = Disregard, 3 = Store.
         */
        public String SentMessage() {
            Scanner in = INPUT;
            System.out.println("Choose an option:\n1) Send Message\n2) Disregard Message\n3) Store Message to send later");
            int choice;
            try {
                choice = Integer.parseInt(in.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }
            String result = SentMessage(choice);
            if (choice == 2) {
                System.out.println(result);
                String confirm = in.nextLine().trim();
                return confirm.equals("0") ? "Message deleted." : "Message kept but not sent.";
            }
            return result;
        }

        /** Non-interactive version (used by the unit tests and by SentMessage()). */
        public String SentMessage(int choice) {
            switch (choice) {
                case 1:
                    sentMessages.add(this);
                    totalMessagesSent++;
                    return "Message successfully sent.";
                case 2:
                    disregardedMessages.add(this);
                    return "Press 0 to delete the message.";
                case 3:
                    storeMessage();
                    return "Message successfully stored.";
                default:
                    return "Invalid option.";
            }
        }

        /** Returns all messages sent while the program is running. */
        public static String printMessages() {
            return generateReport(sentMessages);
        }

        public static int returnTotalMessages() {
            return totalMessagesSent;
        }

        /** Stores this message in a JSON file (one JSON array of message objects). */
        public void storeMessage() {
            storedMessages.add(this);
            writeStoredMessagesToFile();
        }

        /** Rewrites the JSON file from whatever is currently in storedMessages. */
        private static void writeStoredMessagesToFile() {
            StringBuilder json = new StringBuilder("[\n");
            for (int i = 0; i < storedMessages.size(); i++) {
                json.append(storedMessages.get(i).toJson());
                json.append(i < storedMessages.size() - 1 ? ",\n" : "\n");
            }
            json.append("]\n");
            try {
                Path path = Paths.get(storageFile);
                Files.write(path, json.toString().getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                System.out.println("Could not write JSON file: " + e.getMessage());
            }
        }

        /**
         * Reads messages.json (if it exists) and rebuilds the Stored Messages array
         * from it (Part 3, requirement 1 - "Stored Messages" array).
         */
        public static List<Message> loadStoredMessagesFromFile() {
            Path path = Paths.get(storageFile);
            if (!Files.exists(path)) {
                return storedMessages;
            }
            String content;
            try {
                content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                System.out.println("Could not read JSON file: " + e.getMessage());
                return storedMessages;
            }

            storedMessages.clear();
            Pattern objectPattern = Pattern.compile("\\{[^{}]*\\}");
            Matcher objectMatcher = objectPattern.matcher(content);
            while (objectMatcher.find()) {
                String block = objectMatcher.group();
                String id = extractField(block, "messageID");
                String hash = extractField(block, "messageHash");
                String recipient = extractField(block, "recipient");
                String text = extractField(block, "message");
                if (id != null && recipient != null && text != null) {
                    storedMessages.add(fromStorage(id, recipient, text, hash == null ? "" : hash));
                }
            }
            return storedMessages;
        }

        /** Pulls "key": "value" (with basic escape handling) out of one JSON object block. */
        private static String extractField(String jsonBlock, String key) {
            Pattern fieldPattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
            Matcher fieldMatcher = fieldPattern.matcher(jsonBlock);
            if (!fieldMatcher.find()) {
                return null;
            }
            return unescape(fieldMatcher.group(1));
        }

        private static String unescape(String s) {
            return s.replace("\\\"", "\"").replace("\\n", "\n")
                    .replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
        }

        // ------------------------------------------------------------------
        // Part 3 - query / report helpers (used by the menu and the unit tests)
        // ------------------------------------------------------------------

        /** Returns the text of every message in the list, in order. */
        public static List<String> toMessageTexts(List<Message> messages) {
            List<String> texts = new ArrayList<>();
            for (Message m : messages) {
                texts.add(m.getMessage());
            }
            return texts;
        }

        /** Returns the text of the longest message in the list ("" if the list is empty). */
        public static String getLongestMessage(List<Message> messages) {
            String longest = "";
            for (Message m : messages) {
                if (m.getMessage().length() > longest.length()) {
                    longest = m.getMessage();
                }
            }
            return longest;
        }

        /** Searches by message ID and returns its message text, or a not-found message. */
        public static String searchByMessageID(String id, List<Message> messages) {
            for (Message m : messages) {
                if (m.getMessageID().equals(id)) {
                    return m.getMessage();
                }
            }
            return "No message found with ID " + id + ".";
        }

        /** Returns the text of every message addressed to the given recipient. */
        public static List<String> searchMessagesByRecipient(String recipient, List<Message> messages) {
            List<String> matches = new ArrayList<>();
            for (Message m : messages) {
                if (m.getRecipient().equals(recipient)) {
                    matches.add(m.getMessage());
                }
            }
            return matches;
        }

        /** Deletes the message whose hash matches, returning a confirmation or not-found message. */
        public static String deleteMessageByHash(String hash, List<Message> messages) {
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                if (m.getMessageHash().equals(hash)) {
                    messages.remove(i);
                    if (messages == storedMessages) {
                        writeStoredMessagesToFile();
                    }
                    return "Message: \"" + m.getMessage() + "\" successfully deleted.";
                }
            }
            return "No message found with hash " + hash + ".";
        }

        /** Full-detail report (Message Hash, Recipient, Message) for every message in the list. */
        public static String generateReport(List<Message> messages) {
            StringBuilder sb = new StringBuilder();
            for (Message m : messages) {
                sb.append(m.getDetails()).append("\n");
            }
            return sb.toString();
        }

        // ------------------------------------------------------------------
        // Helpers
        // ------------------------------------------------------------------

        public String getDetails() {
            return "Message ID: " + messageID
                    + "\nMessage Hash: " + messageHash
                    + "\nRecipient: " + recipient
                    + "\nMessage: " + message
                    + "\n";
        }

        private String toJson() {
            return "  {\n"
                    + "    \"messageID\": \"" + esc(messageID) + "\",\n"
                    + "    \"messageHash\": \"" + esc(messageHash) + "\",\n"
                    + "    \"recipient\": \"" + esc(recipient) + "\",\n"
                    + "    \"message\": \"" + esc(message) + "\"\n"
                    + "  }";
        }

        private static String esc(String s) {
            return s.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        }

        /** Clears static state (used between unit tests). */
        public static void resetAll() {
            sentMessages.clear();
            disregardedMessages.clear();
            storedMessages.clear();
            messageHashes.clear();
            messageIDs.clear();
            totalMessagesSent = 0;
        }

        public String getMessageID() { return messageID; }
        public String getMessageHash() { return messageHash; }
        public String getRecipient() { return recipient; }
        public String getMessage() { return message; }
        public int getNumMessagesSent() { return numMessagesSent; }

        public static List<Message> getSentMessages() { return sentMessages; }
        public static List<Message> getDisregardedMessages() { return disregardedMessages; }
        public static List<Message> getStoredMessages() { return storedMessages; }
        public static List<String> getMessageHashesArray() { return messageHashes; }
        public static List<String> getMessageIDsArray() { return messageIDs; }
    }
}
