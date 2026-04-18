package com.example.stadiumflow.service;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import com.example.stadiumflow.dto.GeminiRequest;
import com.example.stadiumflow.dto.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * Service for Google Gemini API integration via REST
 * Used for Prompt Wars competition - demonstrates Google AI services
 */
@Service
public class GeminiApiService {

    private static Logger logger = LoggerFactory.getLogger(GeminiApiService.class);
    // Using gemini-pro-latest - stable alias that always points to latest available pro model
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-latest:generateContent";

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ZoneRepository zoneRepository;
    private final RestTemplate restTemplate;
    private boolean isConfigured = false;

    public GeminiApiService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void initialize() {
        try {
            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("placeholder")) {
                isConfigured = true;
                logger.info("✅ Google Gemini API initialized successfully!");
                logger.info("🏆 Using Google Gemini API for Prompt Wars competition");
            } else {
                logger.warn("⚠️ Gemini API key not configured. Set GEMINI_API_KEY environment variable.");
            }
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Gemini API: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process query using Google Gemini API via REST
     * @param query User query
     * @return AI-generated response or null if unavailable
     */
    public String processWithGemini(String query) {
        if (!isConfigured) {
            logger.debug("Gemini API not configured");
            return null;
        }

        try {
            // Get current stadium data for context
            List<Zone> zones = zoneRepository.findAll();
            StringBuilder context = new StringBuilder();

            context.append("You are StadiumPulse AI, an intelligent assistant for the Coldplay 'Music of the Spheres' concert at a major stadium.\n\n");
            context.append("CURRENT REAL-TIME STADIUM DATA:\n");

            for (Zone zone : zones) {
                int density = zone.getDensity();
                int waitTime = zone.getWaitTime();
                String crowdLevel = density < 20 ? "Light" : density < 40 ? "Moderate" : "Heavy";

                context.append(String.format("- %s: %s crowd (density: %d%%), %d min wait\n",
                    zone.getName(), crowdLevel, density, waitTime));
            }

            context.append("\nUSER QUERY: ").append(query);
            context.append("\n\nINSTRUCTIONS: Provide a helpful, friendly, and concise response about the stadium status. ");
            context.append("Include specific zone names, wait times, and actionable recommendations. ");
            context.append("Be enthusiastic about the Coldplay concert!");

            logger.info("🤖 Calling Google Gemini API...");

            // Call Gemini API via REST
            String response = callGeminiApi(context.toString());

            if (response != null && !response.isEmpty()) {
                logger.info("✅ Gemini API response received ({} chars)", response.length());
                return response;
            } else {
                logger.warn("⚠️ Gemini API returned empty response");
                return null;
            }

        } catch (Exception e) {
            logger.error("❌ Gemini API call failed: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Call Gemini API using typed DTOs for improved Code Quality
     */
    private String callGeminiApi(String prompt) {
        try {
            String url = GEMINI_API_URL + "?key=" + apiKey;

            // PERFORMANCE & QUALITY OPTIMIZATION: Use structured DTOs instead of Raw Maps
            GeminiRequest.Part part = new GeminiRequest.Part(prompt);
            GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
            GeminiRequest requestBody = new GeminiRequest(Collections.singletonList(content));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GeminiRequest> entity = new HttpEntity<>(requestBody, headers);

            // Make request using GeminiResponse DTO
            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                GeminiResponse.class
            );

            // Extract text from response safely without manual casts
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                GeminiResponse body = response.getBody();
                List<GeminiResponse.Candidate> candidates = body.getCandidates();

                if (candidates != null && !candidates.isEmpty()) {
                    GeminiResponse.Candidate candidate = candidates.get(0);
                    GeminiResponse.Content contentRes = candidate.getContent();
                    
                    if (contentRes != null && contentRes.getParts() != null && !contentRes.getParts().isEmpty()) {
                        return contentRes.getParts().get(0).getText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to call Gemini API: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if Gemini API is available
     * @return true if initialized and ready
     */
    public boolean isAvailable() {
        return isConfigured;
    }

    /**
     * Get the API key status (for debugging)
     * @return masked API key or status message
     */
    public String getApiKeyStatus() {
        if (apiKey == null || apiKey.isEmpty() || apiKey.equals("placeholder")) {
            return "Not configured";
        }
        return "Configured (***" + apiKey.substring(Math.max(0, apiKey.length() - 4)) + ")";
    }
}
