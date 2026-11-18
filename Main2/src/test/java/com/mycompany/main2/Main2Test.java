/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.mycompany.main2;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Student
 */



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Main2Test {

    private Main2.Login login;

    @BeforeEach
    void setUp() {
        Main2.Message.resetAll();
        login = new Main2.Login();
    }

    // =========================================================================
    // Login & Registration Tests
    // =========================================================================

    @Test
    void testCheckUserNameValid() {
        assertTrue(login.checkUserName("ab_cd"));
        assertTrue(login.checkUserName("u_1"));
    }

    @Test
    void testCheckUserNameInvalid() {
        // Missing underscore or too long (> 5 characters)
        assertFalse(login.checkUserName("abcde"));
        assertFalse(login.checkUserName("toolong_"));
    }

    @Test
    void testCheckPasswordComplexityValid() {
        assertTrue(login.checkPasswordComplexity("Pass123!"));
    }

    @Test
    void testCheckPasswordComplexityInvalid() {
        // Too short, missing capital, digit, or special character
        assertFalse(login.checkPasswordComplexity("pass123!")); // No uppercase
        assertFalse(login.checkPasswordComplexity("PASSWORD!")); // No digit
        assertFalse(login.checkPasswordComplexity("Password12")); // No special char
        assertFalse(login.checkPasswordComplexity("Pass1!"));    // Too short (< 8 chars)
    }

    @Test
    void testRegisterAndLoginUser() {
        String regResult = login.registerUser("John", "Doe", "j_doe", "Secure1#");
        assertTrue(regResult.contains("User registered successfully"));

        assertTrue(login.loginUser("j_doe", "Secure1#"));
        assertFalse(login.loginUser("j_doe", "WrongPass1!"));
        
        String status = login.returnLoginStatus("j_doe", "Secure1#");
        assertTrue(status.contains("Welcome John Doe"));
    }

    // =========================================================================
    // Message Validation & ID Tests
    // =========================================================================

    @Test
    void testGenerateMessageID() {
        String id1 = Main2.Message.generateMessageID();
        String id2 = Main2.Message.generateMessageID();
        
        assertNotNull(id1);
        assertEquals(10, id1.length());
        assertNotEquals(id1, id2);
    }

    @Test
    void testCheckMessageID() {
        Main2.Message msg = new Main2.Message("+27718693002", "Hello World");
        assertTrue(msg.checkMessageID());
    }

    @Test
    void testCheckRecipientCellValid() {
        Main2.Message msg = new Main2.Message("+27718693002", "Test");
        String result = msg.checkRecipientCell();
        assertTrue(result.startsWith("Cell phone number successfully"));
    }

    @Test
    void testCheckRecipientCellInvalid() {
        Main2.Message msg = new Main2.Message("0718693002", "Test"); // Missing international code
        String result = msg.checkRecipientCell();
        assertTrue(result.contains("incorrectly formatted"));
    }

    @Test
    void testCheckMessageLengthValid() {
        String text = "Short message";
        assertEquals("Message sent", Main2.Message.checkMessageLength(text));
    }

    @Test
    void testCheckMessageLengthExceeded() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 260; i++) {
            sb.append("a");
        }
        String result = Main2.Message.checkMessageLength(sb.toString());
        assertTrue(result.contains("exceeds 250 characters"));
    }

    // =========================================================================
    // Message Hashing & Handling Tests
    // =========================================================================

    @Test
    void testCreateMessageHash() {
        // Using a controlled message ID by invoking the alternative constructor
        Main2.Message msg = new Main2.Message("1234567890", "+27718693002", "Hello World", 0);
        String hash = msg.createMessageHash();
        
        // Hash format: firstTwo:numMessagesSent:FIRSTWORDLASTWORD (uppercase)
        assertEquals("12:0:HELLOWORLD", hash);
    }

    @Test
    void testSentMessageOptions() {
        Main2.Message msg = new Main2.Message("+27718693002", "Testing send action");

        // Option 1: Send Message
        String sendOutcome = msg.SentMessage(1);
        assertEquals("Message successfully sent.", sendOutcome);
        assertEquals(1, Main2.Message.returnTotalMessages());
        assertTrue(Main2.Message.getSentMessages().contains(msg));

        // Option 2: Disregard Message
        Main2.Message msg2 = new Main2.Message("+27718693002", "Disregard me");
        String disregardOutcome = msg2.SentMessage(2);
        assertTrue(disregardOutcome.contains("Press 0 to delete"));
        assertTrue(Main2.Message.getDisregardedMessages().contains(msg2));

        // Option 3: Store Message
        Main2.Message msg3 = new Main2.Message("+27718693002", "Store me for later");
        String storeOutcome = msg3.SentMessage(3);
        assertEquals("Message successfully stored.", storeOutcome);
        assertTrue(Main2.Message.getStoredMessages().contains(msg3));
    }

    // =========================================================================
    // Search, Query & Report Utilities Tests
    // =========================================================================

    @Test
    void testGetLongestMessage() {
        Main2.Message m1 = new Main2.Message("+27718693002", "Short");
        Main2.Message m2 = new Main2.Message("+27718693002", "This is a significantly longer message text.");
        
        List<Main2.Message> list = List.of(m1, m2);
        assertEquals("This is a significantly longer message text.", Main2.Message.getLongestMessage(list));
    }

    @Test
    void testSearchByMessageID() {
        Main2.Message m1 = new Main2.Message("9999999999", "+27718693002", "Findable message", 0);
        List<Main2.Message> list = List.of(m1);

        assertEquals("Findable message", Main2.Message.searchByMessageID("9999999999", list));
        assertTrue(Main2.Message.searchByMessageID("0000000000", list).contains("No message found"));
    }

    @Test
    void testSearchMessagesByRecipient() {
        Main2.Message m1 = new Main2.Message("+27718693002", "Msg for target");
        Main2.Message m2 = new Main2.Message("+27799999999", "Msg for other");
        Main2.Message m3 = new Main2.Message("+27718693002", "Another for target");

        List<Main2.Message> list = List.of(m1, m2, m3);
        List<String> results = Main2.Message.searchMessagesByRecipient("+27718693002", list);

        assertEquals(2, results.size());
        assertEquals("Msg for target", results.get(0));
        assertEquals("Another for target", results.get(1));
    }

    @Test
    void testDeleteMessageByHash() {
        Main2.Message m1 = new Main2.Message("1234567890", "+27718693002", "Delete target", 0);
        // Hash for "12;0;DELETETARGET" -> Let's get the generated hash directly
        String targetHash = m1.getMessageHash();

        List<Main2.Message> list = new java.util.ArrayList<>();
        list.add(m1);

        String deleteResult = Main2.Message.deleteMessageByHash(targetHash, list);
        assertTrue(deleteResult.contains("successfully deleted"));
        assertTrue(list.isEmpty());
    }
}
