package com.example.stadiumflow.controller;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import com.example.stadiumflow.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analytics Controller - Demonstrates Cloud Storage integration.
 * Saves stadium metrics to Google Cloud Storage for analysis.
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    private final StorageService storageService;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;
    
    public AnalyticsController(StorageService storageService, 
                              ZoneRepository zoneRepository,
                              ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.zoneRepository = zoneRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Save current stadium metrics snapshot to Cloud Storage.
     * This demonstrates active Google Cloud Storage usage.
     */
    @PostMapping("/snapshot")
    public Map<String, Object> saveSnapshot() {
        try {
            // Gather current metrics
            List<Zone> zones = zoneRepository.findAll();
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("timestamp", LocalDateTime.now().toString());
            metrics.put("zones", zones);
            metrics.put("totalZones", zones.size());
            
            // Calculate aggregate stats
            int totalWaitTime = zones.stream().mapToInt(Zone::getWaitTime).sum();
            int avgWaitTime = zones.isEmpty() ? 0 : totalWaitTime / zones.size();
            metrics.put("averageWaitTime", avgWaitTime);
            metrics.put("totalWaitTime", totalWaitTime);
            
            // Save to Cloud Storage
            String json = objectMapper.writeValueAsString(metrics);
            boolean saved = storageService.isAvailable();
            
            if (saved) {
                storageService.saveMetricsSnapshot(json);
                return Map.of(
                    "success", true,
                    "message", "Snapshot saved to Google Cloud Storage",
                    "timestamp", LocalDateTime.now().toString(),
                    "storageProvider", "Google Cloud Storage",
                    "zones", zones.size()
                );
            } else {
                return Map.of(
                    "success", false,
                    "message", "Cloud Storage not available (running in local mode)",
                    "tip", "Enable Cloud Storage by setting GOOGLE_APPLICATION_CREDENTIALS"
                );
            }
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "message", "Failed to save snapshot: " + e.getMessage()
            );
        }
    }
    
    /**
     * List all analytics files in Cloud Storage.
     */
    @GetMapping("/files")
    public Map<String, Object> listFiles() {
        if (!storageService.isAvailable()) {
            return Map.of(
                "available", false,
                "message", "Cloud Storage not configured",
                "tip", "Run with --spring.profiles.active=prod and set GOOGLE_APPLICATION_CREDENTIALS"
            );
        }
        
        List<String> files = storageService.listAnalyticsFiles();
        return Map.of(
            "available", true,
            "files", files,
            "count", files.size(),
            "provider", "Google Cloud Storage"
        );
    }
    
    /**
     * Get Cloud Storage status.
     */
    @GetMapping("/storage/status")
    public Map<String, Object> getStorageStatus() {
        return Map.of(
            "available", storageService.isAvailable(),
            "provider", "Google Cloud Storage",
            "service", "Cloud Storage",
            "documentation", "See GOOGLE_SERVICES_SETUP.md for setup instructions"
        );
    }
}
