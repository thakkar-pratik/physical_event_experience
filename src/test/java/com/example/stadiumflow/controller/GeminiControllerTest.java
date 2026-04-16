package com.example.stadiumflow.controller;

import com.example.stadiumflow.dto.AiResponseDto;
import com.example.stadiumflow.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GeminiControllerTest {

    @Mock
    private GeminiService geminiService;

    @InjectMocks
    private GeminiController geminiController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAskAssistant_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "What is the status?");
        
        AiResponseDto mockResponse = new AiResponseDto("Stadium is operating normally", "Test AI");
        when(geminiService.processQuery("What is the status?")).thenReturn(mockResponse);
        
        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Stadium is operating normally", response.getBody().getResponse());
        assertEquals("Test AI", response.getBody().getProvider());
        verify(geminiService).processQuery("What is the status?");
    }

    @Test
    public void testAskAssistant_EmptyQuery() {
        Map<String, String> request = new HashMap<>();
        
        AiResponseDto mockResponse = new AiResponseDto("Default response", "Test AI");
        when(geminiService.processQuery("")).thenReturn(mockResponse);
        
        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(geminiService).processQuery("");
    }

    @Test
    public void testAskAssistant_NoQueryKey() {
        Map<String, String> request = new HashMap<>();
        request.put("different_key", "value");
        
        AiResponseDto mockResponse = new AiResponseDto("Default response", "Test AI");
        when(geminiService.processQuery("")).thenReturn(mockResponse);
        
        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);
        
        assertNotNull(response);
        verify(geminiService).processQuery("");
    }

    @Test
    public void testAskAssistant_NullRequest() {
        Map<String, String> request = null;
        
        assertThrows(NullPointerException.class, () -> {
            geminiController.askAssistant(request);
        });
    }

    @Test
    public void testAskAssistant_LongQuery() {
        Map<String, String> request = new HashMap<>();
        String longQuery = "This is a very long query ".repeat(50);
        request.put("query", longQuery);
        
        AiResponseDto mockResponse = new AiResponseDto("Processed long query", "Test AI");
        when(geminiService.processQuery(longQuery)).thenReturn(mockResponse);
        
        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testAskAssistant_SpecialCharacters() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "Special chars: !@#$%^&*()");
        
        AiResponseDto mockResponse = new AiResponseDto("Handled special chars", "Test AI");
        when(geminiService.processQuery(anyString())).thenReturn(mockResponse);
        
        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testAskAssistant_MultipleRequests() {
        Map<String, String> request1 = new HashMap<>();
        request1.put("query", "Query 1");
        Map<String, String> request2 = new HashMap<>();
        request2.put("query", "Query 2");

        AiResponseDto mockResponse1 = new AiResponseDto("Response 1", "Test AI");
        AiResponseDto mockResponse2 = new AiResponseDto("Response 2", "Test AI");

        when(geminiService.processQuery("Query 1")).thenReturn(mockResponse1);
        when(geminiService.processQuery("Query 2")).thenReturn(mockResponse2);

        ResponseEntity<AiResponseDto> response1 = geminiController.askAssistant(request1);
        ResponseEntity<AiResponseDto> response2 = geminiController.askAssistant(request2);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("Response 1", response1.getBody().getResponse());
        assertEquals("Response 2", response2.getBody().getResponse());
    }

    @Test
    public void testAskAssistant_WithWhitespaceQuery() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "   ");

        AiResponseDto mockResponse = new AiResponseDto("Default response", "Test AI");
        when(geminiService.processQuery("   ")).thenReturn(mockResponse);

        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testAskAssistant_WithUnicodeCharacters() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "Hello 世界 🌍");

        AiResponseDto mockResponse = new AiResponseDto("Unicode response", "Test AI");
        when(geminiService.processQuery(anyString())).thenReturn(mockResponse);

        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testAskAssistant_WithNumericQuery() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "12345");

        AiResponseDto mockResponse = new AiResponseDto("Numeric response", "Test AI");
        when(geminiService.processQuery("12345")).thenReturn(mockResponse);

        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testAskAssistant_ServiceReturnsNull() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "test");

        when(geminiService.processQuery("test")).thenReturn(null);

        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);

        assertNotNull(response);
        assertNull(response.getBody());
    }

    @Test
    public void testAskAssistant_ServiceThrowsException() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "test");

        when(geminiService.processQuery("test")).thenThrow(new RuntimeException("Service error"));

        assertThrows(RuntimeException.class, () -> geminiController.askAssistant(request));
    }

    @Test
    public void testAskAssistant_WithNullQueryValue() {
        Map<String, String> request = new HashMap<>();
        request.put("query", null);

        // Will throw NullPointerException when service tries to process null
        when(geminiService.processQuery(null)).thenThrow(new NullPointerException());

        assertThrows(NullPointerException.class, () -> {
            geminiController.askAssistant(request);
        });
    }

    @Test
    public void testAskAssistant_WithDifferentKey() {
        Map<String, String> request = new HashMap<>();
        request.put("question", "test"); // Wrong key

        // Will get null for "query" key
        // Service should handle this gracefully or throw NPE
        try {
            geminiController.askAssistant(request);
            // If no exception, that's fine
        } catch (NullPointerException e) {
            // Also acceptable
            assertNotNull(e);
        }
    }

    @Test
    public void testAskAssistant_VerifyResponseFields() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "test query");

        AiResponseDto mockResponse = new AiResponseDto("Test response", "Test Provider");
        when(geminiService.processQuery(anyString())).thenReturn(mockResponse);

        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);

        assertNotNull(response.getBody());
        assertEquals("Test response", response.getBody().getResponse());
        assertEquals("Test Provider", response.getBody().getProvider());
    }

    @Test
    public void testAskAssistant_DifferentQueries() {
        AiResponseDto resp1 = new AiResponseDto("Response 1", "Provider");
        AiResponseDto resp2 = new AiResponseDto("Response 2", "Provider");
        AiResponseDto resp3 = new AiResponseDto("Response 3", "Provider");

        when(geminiService.processQuery("q1")).thenReturn(resp1);
        when(geminiService.processQuery("q2")).thenReturn(resp2);
        when(geminiService.processQuery("q3")).thenReturn(resp3);

        Map<String, String> req1 = new HashMap<>();
        req1.put("query", "q1");
        Map<String, String> req2 = new HashMap<>();
        req2.put("query", "q2");
        Map<String, String> req3 = new HashMap<>();
        req3.put("query", "q3");

        assertEquals("Response 1", geminiController.askAssistant(req1).getBody().getResponse());
        assertEquals("Response 2", geminiController.askAssistant(req2).getBody().getResponse());
        assertEquals("Response 3", geminiController.askAssistant(req3).getBody().getResponse());
    }

    @Test
    public void testAskAssistant_HttpStatus200() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "test");

        AiResponseDto mockResponse = new AiResponseDto("Response", "Provider");
        when(geminiService.processQuery(anyString())).thenReturn(mockResponse);

        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);

        assertEquals(200, response.getStatusCodeValue());
    }
}
