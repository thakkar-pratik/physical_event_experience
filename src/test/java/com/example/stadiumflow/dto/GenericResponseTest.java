package com.example.stadiumflow.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GenericResponseTest {

    @Test
    public void testConstructor() {
        GenericResponse response = new GenericResponse("success", "Operation completed");
        
        assertNotNull(response);
        assertEquals("success", response.getStatus());
        assertEquals("Operation completed", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testTimestampIsRecent() {
        LocalDateTime before = LocalDateTime.now();
        GenericResponse response = new GenericResponse("success", "Test");
        LocalDateTime after = LocalDateTime.now();
        
        assertTrue(response.getTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(response.getTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    public void testDifferentStatuses() {
        GenericResponse success = new GenericResponse("success", "Success message");
        GenericResponse error = new GenericResponse("error", "Error message");
        GenericResponse warning = new GenericResponse("warning", "Warning message");
        
        assertEquals("success", success.getStatus());
        assertEquals("error", error.getStatus());
        assertEquals("warning", warning.getStatus());
    }

    @Test
    public void testNullValues() {
        GenericResponse response = new GenericResponse(null, null);
        
        assertNull(response.getStatus());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testEmptyStrings() {
        GenericResponse response = new GenericResponse("", "");
        
        assertEquals("", response.getStatus());
        assertEquals("", response.getMessage());
    }

    @Test
    public void testLongMessage() {
        String longMessage = "Long message ".repeat(100);
        GenericResponse response = new GenericResponse("success", longMessage);
        
        assertEquals(longMessage, response.getMessage());
    }

    @Test
    public void testMultipleInstancesHaveDifferentTimestamps() throws InterruptedException {
        GenericResponse response1 = new GenericResponse("success", "Test 1");
        Thread.sleep(10); // Small delay to ensure different timestamps
        GenericResponse response2 = new GenericResponse("success", "Test 2");
        
        assertNotEquals(response1.getTimestamp(), response2.getTimestamp());
    }
}
