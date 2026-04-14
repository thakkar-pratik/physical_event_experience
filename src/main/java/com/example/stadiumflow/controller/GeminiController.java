package com.example.stadiumflow.controller;

import com.example.stadiumflow.service.GeminiService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/ask")
    public Map<String, String> askAssistant(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "");
        return geminiService.processQuery(query);
    }
}
