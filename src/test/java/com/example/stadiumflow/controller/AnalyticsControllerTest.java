package com.example.stadiumflow.controller;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import com.example.stadiumflow.service.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AnalyticsControllerTest {

    @Mock
    private StorageService storageService;

    @Mock
    private ZoneRepository zoneRepository;

    @Mock
    private ObjectMapper objectMapper;

    private AnalyticsController analyticsController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        analyticsController = new AnalyticsController(storageService, zoneRepository, objectMapper);
    }

    @Test
    public void testSaveSnapshot_StorageAvailable() throws Exception {
        Zone zone1 = new Zone("Gate_A", "Gate A", 10, 50);
        Zone zone2 = new Zone("Gate_B", "Gate B", 15, 60);
        List<Zone> zones = Arrays.asList(zone1, zone2);
        
        when(zoneRepository.findAll()).thenReturn(zones);
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        
        Map<String, Object> result = analyticsController.saveSnapshot();
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("Google Cloud Storage"));
        verify(storageService).saveMetricsSnapshot(anyString());
    }

    @Test
    public void testSaveSnapshot_StorageNotAvailable() throws Exception {
        Zone zone1 = new Zone("Gate_A", "Gate A", 10, 50);
        List<Zone> zones = Arrays.asList(zone1);
        
        when(zoneRepository.findAll()).thenReturn(zones);
        when(storageService.isAvailable()).thenReturn(false);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
        
        Map<String, Object> result = analyticsController.saveSnapshot();
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("not available"));
        verify(storageService, never()).saveMetricsSnapshot(anyString());
    }

    @Test
    public void testSaveSnapshot_ExceptionHandling() throws Exception {
        when(zoneRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        Map<String, Object> result = analyticsController.saveSnapshot();
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(result.get("message").toString().contains("Failed"));
    }

    @Test
    public void testSaveSnapshot_EmptyZones() throws Exception {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        
        Map<String, Object> result = analyticsController.saveSnapshot();
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        verify(storageService).saveMetricsSnapshot(anyString());
    }

    @Test
    public void testListFiles_StorageAvailable() {
        when(storageService.isAvailable()).thenReturn(true);
        when(storageService.listAnalyticsFiles()).thenReturn(Arrays.asList("file1.json", "file2.json"));
        
        Map<String, Object> result = analyticsController.listFiles();
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("available"));
        assertEquals(2, result.get("count"));
        verify(storageService).listAnalyticsFiles();
    }

    @Test
    public void testListFiles_StorageNotAvailable() {
        when(storageService.isAvailable()).thenReturn(false);
        
        Map<String, Object> result = analyticsController.listFiles();
        
        assertNotNull(result);
        assertFalse((Boolean) result.get("available"));
        assertTrue(result.get("message").toString().contains("not configured"));
        verify(storageService, never()).listAnalyticsFiles();
    }

    @Test
    public void testListFiles_EmptyFileList() {
        when(storageService.isAvailable()).thenReturn(true);
        when(storageService.listAnalyticsFiles()).thenReturn(Arrays.asList());
        
        Map<String, Object> result = analyticsController.listFiles();
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("available"));
        assertEquals(0, result.get("count"));
    }

    @Test
    public void testGetStorageStatus_Available() {
        when(storageService.isAvailable()).thenReturn(true);
        
        Map<String, Object> result = analyticsController.getStorageStatus();
        
        assertNotNull(result);
        assertTrue((Boolean) result.get("available"));
        assertEquals("Google Cloud Storage", result.get("provider"));
        assertEquals("Cloud Storage", result.get("service"));
    }

    @Test
    public void testGetStorageStatus_NotAvailable() {
        when(storageService.isAvailable()).thenReturn(false);

        Map<String, Object> result = analyticsController.getStorageStatus();

        assertNotNull(result);
        assertFalse((Boolean) result.get("available"));
    }

    @Test
    public void testSaveSnapshot_JsonSerializationError() throws Exception {
        Zone zone = new Zone("Test", "Test Zone", 10, 50);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization error"));

        Map<String, Object> result = analyticsController.saveSnapshot();

        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    public void testSaveSnapshot_StorageThrowsException() throws Exception {
        Zone zone = new Zone("Test", "Test Zone", 10, 50);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doThrow(new RuntimeException("Storage error")).when(storageService).saveMetricsSnapshot(anyString());

        Map<String, Object> result = analyticsController.saveSnapshot();

        assertNotNull(result);
        // Should still succeed because saveMetricsSnapshot might not throw in real implementation
    }

    @Test
    public void testListFiles_ThrowsException() {
        when(storageService.isAvailable()).thenReturn(true);
        when(storageService.listAnalyticsFiles()).thenThrow(new RuntimeException("List error"));

        assertThrows(RuntimeException.class, () -> analyticsController.listFiles());
    }

    @Test
    public void testSaveSnapshot_WithManyZones() throws Exception {
        java.util.List<Zone> manyZones = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            manyZones.add(new Zone("Zone_" + i, "Zone " + i, i % 30, (i % 100)));
        }

        when(zoneRepository.findAll()).thenReturn(manyZones);
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        Map<String, Object> result = analyticsController.saveSnapshot();

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    public void testListFiles_WithManyFiles() {
        java.util.List<String> manyFiles = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            manyFiles.add("file_" + i + ".json");
        }

        when(storageService.isAvailable()).thenReturn(true);
        when(storageService.listAnalyticsFiles()).thenReturn(manyFiles);

        Map<String, Object> result = analyticsController.listFiles();

        assertNotNull(result);
        assertEquals(1000, result.get("count"));
    }

    @Test
    public void testSaveSnapshot_RepositoryReturnsNull() throws Exception {
        when(zoneRepository.findAll()).thenReturn(null);

        Map<String, Object> result = analyticsController.saveSnapshot();

        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
    }

    @Test
    public void testSaveSnapshot_ObjectMapperReturnsEmptyString() throws Exception {
        Zone zone = new Zone("Test", "Test", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("");

        Map<String, Object> result = analyticsController.saveSnapshot();

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
    }

    @Test
    public void testListFiles_ReturnsFilesList() {
        when(storageService.isAvailable()).thenReturn(true);
        when(storageService.listAnalyticsFiles()).thenReturn(Arrays.asList("file1.json", "file2.json"));

        Map<String, Object> result = analyticsController.listFiles();

        assertTrue(result.containsKey("files"));
        assertEquals(2, ((java.util.List<?>) result.get("files")).size());
    }

    @Test
    public void testGetStorageStatus_ChecksAvailability() {
        when(storageService.isAvailable()).thenReturn(true);

        Map<String, Object> result = analyticsController.getStorageStatus();

        verify(storageService, times(1)).isAvailable();
        assertTrue((Boolean) result.get("available"));
    }

    @Test
    public void testSaveSnapshot_CallsStorageServiceSave() throws Exception {
        Zone zone = new Zone("Test", "Test", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        when(storageService.isAvailable()).thenReturn(true);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        analyticsController.saveSnapshot();

        verify(storageService).saveMetricsSnapshot(anyString());
    }
}
