package com.example.stadiumflow.service;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class GeminiServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private GeminiService geminiService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        Zone gateA = new Zone("Gate_A", "Gate A", 25, 80);
        Zone gateC = new Zone("Gate_C", "Gate C", 5, 20);
        Zone section112 = new Zone("Section_112", "Section 112", 2, 10);
        Zone section120 = new Zone("Section_120", "Section 120", 15, 50);
        
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gateA, gateC, section112, section120));
    }

    @Test
    public void testAskAssistant_SpecificZone_Busy() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "how is gate a?");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Gate A"));
        assertTrue(response.get("response").contains("It's quite busy right now!"));
    }

    @Test
    public void testAskAssistant_SpecificZone_Clear() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "how is gate c?");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Gate C"));
        assertTrue(response.get("response").contains("Everything looks clear there. Enjoy!"));
    }

    @Test
    public void testAskAssistant_StatusReport() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "give me a status overview");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Stadium Health Report"));
        assertTrue(response.get("response").contains("Gate A"));
        assertTrue(response.get("response").contains("Section 112"));
    }

    @Test
    public void testAskAssistant_StatusReport_EmptyZones() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());
        Map<String, String> request = new HashMap<>();
        request.put("query", "give me a status overview");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Stadium Health Report"));
    }

    @Test
    public void testAskAssistant_DealFinder_YesDeal() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "is there a deal?");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Dynamic Yield discount is currently active"));
        assertTrue(response.get("response").contains("Section 112"));
    }

    @Test
    public void testAskAssistant_DealFinder_NoDeal() {
        Zone section112 = new Zone("Section_112", "Section 112", 15, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(section112));
        
        Map<String, String> request = new HashMap<>();
        request.put("query", "give me a discount");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Concessions are at normal capacity."));
    }
    
    @Test
    public void testAskAssistant_DealFinder_NoStands() {
        Zone gate = new Zone("Gate_A", "Gate A", 5, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(gate));
        
        Map<String, String> request = new HashMap<>();
        request.put("query", "give me a discount");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("Concessions are at normal capacity."));
    }

    @Test
    public void testAskAssistant_Fallback() {
        Map<String, String> request = new HashMap<>();
        request.put("query", "hello");
        
        Map<String, String> response = geminiService.processQuery(request.get("query"));
        assertTrue(response.get("response").contains("I am the StadiumFlow Assistant."));
    }
}
