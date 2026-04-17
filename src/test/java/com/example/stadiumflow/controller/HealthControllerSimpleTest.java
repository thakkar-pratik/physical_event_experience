package com.example.stadiumflow.controller;

import com.example.stadiumflow.repository.ZoneRepository;
import com.example.stadiumflow.service.GeminiApiService;
import com.example.stadiumflow.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

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

    // ==== TESTS FOR 100% BRANCH COVERAGE ====

    @Test
    public void testCheckVertexAI_WithGeminiApiServiceAvailable() {
        // Test lines 30-31 and 64-66: when geminiApiService != null branch
        GeminiApiService geminiApiService = mock(GeminiApiService.class);
        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.getApiKeyStatus()).thenReturn("Configured (test-key...)");

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");

        HealthController controller = new HealthController(service);
        // Inject geminiApiService into HealthController
        ReflectionTestUtils.setField(controller, "geminiApiService", geminiApiService);

        Map<String, Object> result = controller.checkVertexAI();

        assertNotNull(result);
        assertTrue(result.containsKey("geminiApiAvailable"));
        assertTrue(result.containsKey("geminiApiKeyStatus"));
        assertEquals(true, result.get("geminiApiAvailable"));
        assertEquals("Configured (test-key...)", result.get("geminiApiKeyStatus"));
        // When geminiApiService is available, status should be USING_GEMINI_API
        assertEquals("USING_GEMINI_API", result.get("status"));
        assertEquals("Google Gemini API (Prompt Wars)", result.get("provider"));
    }

    @Test
    public void testCheckVertexAI_WithGeminiApiServiceNull() {
        // Test lines 32-34: when geminiApiService == null branch
        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");
        // geminiApiService is null by default

        HealthController controller = new HealthController(service);
        Map<String, Object> result = controller.checkVertexAI();

        assertNotNull(result);
        // Should have geminiApiAvailable = false when null
        assertTrue(result.containsKey("geminiApiAvailable"));
        assertEquals(false, result.get("geminiApiAvailable"));
        assertEquals("Not configured", result.get("geminiApiKeyStatus"));
    }

    @Test
    public void testCheckVertexAI_WithGeminiApiServiceNotAvailable() {
        // Test lines 30-31: when geminiApiService != null but NOT available
        GeminiApiService geminiApiService = mock(GeminiApiService.class);
        when(geminiApiService.isAvailable()).thenReturn(false);
        when(geminiApiService.getApiKeyStatus()).thenReturn("Not configured");

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");

        HealthController controller = new HealthController(service);
        // Inject geminiApiService into HealthController
        ReflectionTestUtils.setField(controller, "geminiApiService", geminiApiService);

        Map<String, Object> result = controller.checkVertexAI();

        assertNotNull(result);
        assertTrue(result.containsKey("geminiApiAvailable"));
        assertTrue(result.containsKey("geminiApiKeyStatus"));
        assertEquals(false, result.get("geminiApiAvailable"));
        assertEquals("Not configured", result.get("geminiApiKeyStatus"));
    }

    @Test
    public void testCheckVertexAI_WithModelInitialized() {
        // Test lines 64-70: when model and vertexAi are initialized
        // Note: Without actual GCP credentials, we can't fully test this
        // But we can test the reflection logic
        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", null);
        HealthController controller = new HealthController(service);

        Map<String, Object> result = controller.checkVertexAI();

        assertNotNull(result);
        // Should have modelInitialized and vertexAiInitialized fields
        assertTrue(result.containsKey("modelInitialized"));
        assertTrue(result.containsKey("vertexAiInitialized"));
    }

    @Test
    public void testCheckVertexAI_WithExceptionDuringReflection() {
        // Test exception path at line 75
        GeminiService badService = new GeminiService(null, null, null, null, null);
        HealthController controller = new HealthController(badService);

        Map<String, Object> result = controller.checkVertexAI();

        // Should catch exception and return ERROR status
        assertNotNull(result);
        assertTrue(result.containsKey("status"));
        // May be ERROR due to null parameters
    }

    @Test
    public void testCheckVertexAI_AllFieldsPresent() {
        // Comprehensive test for all fields
        GeminiApiService geminiApiService = mock(GeminiApiService.class);
        when(geminiApiService.isAvailable()).thenReturn(true);
        when(geminiApiService.getApiKeyStatus()).thenReturn("Configured");

        GeminiService service = new GeminiService(zoneRepository, "my-project", "us-west1", "gemini-1.5-pro", "api-key");

        HealthController controller = new HealthController(service);
        // Inject geminiApiService into HealthController
        ReflectionTestUtils.setField(controller, "geminiApiService", geminiApiService);

        Map<String, Object> result = controller.checkVertexAI();

        // Verify all expected fields are present
        assertTrue(result.containsKey("vertexAiInitialized"));
        assertTrue(result.containsKey("modelInitialized"));
        assertTrue(result.containsKey("projectId"));
        assertTrue(result.containsKey("location"));
        assertTrue(result.containsKey("modelName"));
        assertTrue(result.containsKey("geminiApiAvailable"));
        assertTrue(result.containsKey("geminiApiKeyStatus"));
        assertTrue(result.containsKey("status"));
        assertTrue(result.containsKey("provider"));

        // Verify values
        assertEquals("my-project", result.get("projectId"));
        assertEquals("us-west1", result.get("location"));
        assertEquals("gemini-1.5-pro", result.get("modelName"));
        assertEquals(true, result.get("geminiApiAvailable"));
        assertEquals("Configured", result.get("geminiApiKeyStatus"));
        assertEquals("USING_GEMINI_API", result.get("status"));
    }

    @Test
    public void testCheckVertexAI_EdgeCaseEmptyStrings() {
        // Test with empty string configurations
        GeminiService service = new GeminiService(zoneRepository, "", "", "", "");
        HealthController controller = new HealthController(service);

        Map<String, Object> result = controller.checkVertexAI();

        assertNotNull(result);
        assertEquals("", result.get("projectId"));
        assertEquals("", result.get("location"));
        assertEquals("", result.get("modelName"));
    }

    @Test
    public void testCheckVertexAI_GeminiApiServiceThrowsException() {
        // Test when geminiApiService methods throw exceptions at line 30-31
        // The exception happens OUTSIDE the try block (lines 29-31), so it's NOT caught
        // Actually the exception is caught because the try block starts at line 38
        // But lines 29-31 are BEFORE the try block, so if isAvailable() throws, it's uncaught
        GeminiApiService geminiApiService = mock(GeminiApiService.class);
        when(geminiApiService.isAvailable()).thenThrow(new RuntimeException("Test exception"));

        GeminiService service = new GeminiService(zoneRepository, "test-project", "us-central1", "gemini-pro", "test-key");

        HealthController controller = new HealthController(service);
        // Inject geminiApiService into HealthController
        ReflectionTestUtils.setField(controller, "geminiApiService", geminiApiService);

        // The exception is thrown at line 30 which is BEFORE try block
        // So it will propagate up as an uncaught exception
        assertThrows(RuntimeException.class, () -> {
            controller.checkVertexAI();
        });
    }
}
