package com.example.stadiumflow.controller;

import com.example.stadiumflow.repository.ZoneRepository;
import com.example.stadiumflow.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Simplified tests for HealthController focusing on branch coverage
 */
public class HealthControllerSimpleTest {

    @Mock
    private ZoneRepository zoneRepository;

    private GeminiService geminiService;
    private HealthController healthController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        geminiService = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");
        healthController = new HealthController(geminiService);
    }

    @Test
    public void testCheckVertexAI_BasicCall() {
        Map<String, Object> result = healthController.checkVertexAI();
        
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
    }

    @Test
    public void testCheckVertexAI_HasAllFields() {
        Map<String, Object> result = healthController.checkVertexAI();
        
        assertTrue(result.containsKey("vertexAiInitialized"));
        assertTrue(result.containsKey("modelInitialized"));
        assertTrue(result.containsKey("projectId"));
        assertTrue(result.containsKey("location"));
        assertTrue(result.containsKey("modelName"));
        assertTrue(result.containsKey("status"));
    }

    @Test
    public void testCheckVertexAI_ReturnsCorrectProjectId() {
        Map<String, Object> result = healthController.checkVertexAI();
        
        assertEquals("test-project", result.get("projectId"));
    }

    @Test
    public void testCheckVertexAI_ReturnsCorrectLocation() {
        Map<String, Object> result = healthController.checkVertexAI();
        
        assertEquals("us-central1", result.get("location"));
    }

    @Test
    public void testCheckVertexAI_ReturnsCorrectModelName() {
        Map<String, Object> result = healthController.checkVertexAI();
        
        assertEquals("gemini-pro", result.get("modelName"));
    }

    @Test
    public void testCheckVertexAI_StatusNotNull() {
        // Without actual GCP, status will vary
        Map<String, Object> result = healthController.checkVertexAI();

        String status = (String) result.get("status");
        assertNotNull(status);
        // Status can be USING_FALLBACK, USING_VERTEX_AI, or ERROR
        assertTrue(
            "USING_FALLBACK".equals(status) ||
            "USING_VERTEX_AI".equals(status) ||
            "ERROR".equals(status)
        );
    }

    @Test
    public void testCheckVertexAI_NullServiceThrowsNPE() {
        healthController = new HealthController(null);
        
        Map<String, Object> result = healthController.checkVertexAI();
        
        // Should catch exception and return ERROR status
        assertEquals("ERROR", result.get("status"));
        assertTrue(result.containsKey("error"));
        assertTrue(result.containsKey("errorType"));
    }

    @Test
    public void testCheckVertexAI_MockServiceReflectionFails() {
        GeminiService mockService = mock(GeminiService.class);
        healthController = new HealthController(mockService);
        
        Map<String, Object> result = healthController.checkVertexAI();
        
        // Reflection on mock may fail, should return status
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
    }

    @Test
    public void testCheckVertexAI_MultipleCalls() {
        for (int i = 0; i < 3; i++) {
            Map<String, Object> result = healthController.checkVertexAI();
            assertNotNull(result);
            assertTrue(result.containsKey("status"));
        }
    }

    @Test
    public void testCheckVertexAI_DifferentConfigurations() {
        // Test with different configurations
        GeminiService service1 = new GeminiService(zoneRepository, "proj1", "loc1", "model1", "key1");
        HealthController controller1 = new HealthController(service1);
        
        Map<String, Object> result1 = controller1.checkVertexAI();
        assertEquals("proj1", result1.get("projectId"));
        assertEquals("loc1", result1.get("location"));
        assertEquals("model1", result1.get("modelName"));
    }
}
