package com.example.stadiumflow.controller;

import com.example.stadiumflow.service.GeminiService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    
    private final GeminiService geminiService;
    
    public HealthController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }
    
    @GetMapping("/vertex-ai")
    public Map<String, Object> checkVertexAI() {
        Map<String, Object> status = new HashMap<>();
        
        // Use reflection to check if Vertex AI is initialized
        try {
            java.lang.reflect.Field vertexAiField = GeminiService.class.getDeclaredField("vertexAi");
            java.lang.reflect.Field modelField = GeminiService.class.getDeclaredField("model");
            java.lang.reflect.Field projectIdField = GeminiService.class.getDeclaredField("vertexProjectId");
            java.lang.reflect.Field locationField = GeminiService.class.getDeclaredField("vertexLocation");
            java.lang.reflect.Field modelNameField = GeminiService.class.getDeclaredField("vertexModelName");
            
            vertexAiField.setAccessible(true);
            modelField.setAccessible(true);
            projectIdField.setAccessible(true);
            locationField.setAccessible(true);
            modelNameField.setAccessible(true);
            
            Object vertexAi = vertexAiField.get(geminiService);
            Object model = modelField.get(geminiService);
            String projectId = (String) projectIdField.get(geminiService);
            String location = (String) locationField.get(geminiService);
            String modelName = (String) modelNameField.get(geminiService);
            
            status.put("vertexAiInitialized", vertexAi != null);
            status.put("modelInitialized", model != null);
            status.put("projectId", projectId);
            status.put("location", location);
            status.put("modelName", modelName);
            status.put("status", (vertexAi != null && model != null) ? "USING_VERTEX_AI" : "USING_FALLBACK");
            
        } catch (Exception e) {
            status.put("error", e.getMessage());
            status.put("errorType", e.getClass().getName());
            status.put("status", "ERROR");
        }
        
        return status;
    }
}
