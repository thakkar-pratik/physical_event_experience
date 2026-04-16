package com.example.stadiumflow.integration;

import com.example.stadiumflow.IoTDataController;
import com.example.stadiumflow.controller.GeminiController;
import com.example.stadiumflow.controller.OrderController;
import com.example.stadiumflow.controller.AnalyticsController;
import com.example.stadiumflow.domain.ConcessionOrder;
import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.dto.AiResponseDto;
import com.example.stadiumflow.dto.GenericResponse;
import com.example.stadiumflow.repository.OrderRepository;
import com.example.stadiumflow.repository.ZoneRepository;
import com.example.stadiumflow.service.GeminiService;
import com.example.stadiumflow.service.IoTService;
import com.example.stadiumflow.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration tests to verify end-to-end functionality
 */
public class StadiumFlowIntegrationTest {

    @Mock
    private ZoneRepository zoneRepository;
    
    @Mock
    private OrderRepository orderRepository;
    
    @Mock
    private StorageService storageService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private GeminiService geminiService;
    private IoTService ioTService;
    private GeminiController geminiController;
    private OrderController orderController;
    private AnalyticsController analyticsController;
    private IoTDataController ioTDataController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        geminiService = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");
        ioTService = new IoTService(zoneRepository);
        geminiController = new GeminiController(geminiService);
        orderController = new OrderController(orderRepository);
        analyticsController = new AnalyticsController(storageService, zoneRepository, objectMapper);
        ioTDataController = new IoTDataController(ioTService);
    }

    @Test
    public void testCompleteUserJourney_QueryAndOrder() {
        // Setup zones
        Zone gate = new Zone("Gate_A", "Gate A", 10, 40);
        Zone stand = new Zone("Section_112", "Food Stand", 3, 15);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate, stand));
        
        // User asks about deals
        Map<String, String> request = new HashMap<>();
        request.put("query", "any deals?");
        ResponseEntity<AiResponseDto> aiResponse = geminiController.askAssistant(request);
        
        assertNotNull(aiResponse);
        assertNotNull(aiResponse.getBody());
        assertTrue(aiResponse.getBody().getResponse().contains("Dynamic Yield"));
        
        // User places order
        ConcessionOrder order = new ConcessionOrder("Section_112", "Hotdog", 5.0, true);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);
        
        ResponseEntity<GenericResponse> orderResponse = orderController.placeOrder(order);
        
        assertNotNull(orderResponse);
        assertEquals(200, orderResponse.getStatusCodeValue());
        assertEquals("success", orderResponse.getBody().getStatus());
    }

    @Test
    public void testCompleteUserJourney_CheckStatusThenNavigate() {
        Zone busyGate = new Zone("Gate_Main", "Main Gate", 20, 75);
        Zone clearGate = new Zone("Gate_Side", "Side Gate", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(busyGate, clearGate));
        
        // User asks for status
        Map<String, String> request = new HashMap<>();
        request.put("query", "status");
        ResponseEntity<AiResponseDto> response = geminiController.askAssistant(request);
        
        assertNotNull(response);
        assertTrue(response.getBody().getResponse().contains("Stadium Health Report"));
        assertTrue(response.getBody().getResponse().contains("Main Gate"));
    }

    @Test
    public void testAnalyticsWorkflow() throws Exception {
        Zone zone = new Zone("Test", "Test Zone", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        when(storageService.isAvailable()).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        Map<String, Object> result = analyticsController.saveSnapshot();
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    public void testIoTDataStreaming() {
        Zone zone = new Zone("Gate_A", "Gate A", 8, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        List<Zone> zones = ioTDataController.getIoTData();
        
        assertNotNull(zones);
        assertEquals(1, zones.size());
    }

    @Test
    public void testMultipleSimultaneousQueries() {
        Zone zone = new Zone("Gate_Test", "Test Gate", 12, 45);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        Map<String, String> request1 = new HashMap<>();
        request1.put("query", "status");
        Map<String, String> request2 = new HashMap<>();
        request2.put("query", "deals");
        
        ResponseEntity<AiResponseDto> response1 = geminiController.askAssistant(request1);
        ResponseEntity<AiResponseDto> response2 = geminiController.askAssistant(request2);
        
        assertNotNull(response1);
        assertNotNull(response2);
    }
}
