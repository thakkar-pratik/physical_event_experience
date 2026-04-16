package com.example.stadiumflow.service;

import com.google.cloud.storage.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Branch coverage tests for StorageService
 */
public class StorageServiceBranchTest {

    @Test
    public void testUploadAnalytics_StorageNull_EnabledFalse() {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", false);
        ReflectionTestUtils.setField(service, "storage", null);
        
        boolean result = service.uploadAnalytics("test.json", "content");
        assertFalse(result);
    }
    
    @Test
    public void testUploadAnalytics_StorageNull_EnabledTrue() {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", true);
        ReflectionTestUtils.setField(service, "storage", null);
        
        boolean result = service.uploadAnalytics("test.json", "content");
        assertFalse(result);
    }
    
    @Test
    public void testUploadAnalytics_StorageNotNull_EnabledFalse() {
        Storage mockStorage = mock(Storage.class);
        
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", false);
        ReflectionTestUtils.setField(service, "storage", mockStorage);
        
        boolean result = service.uploadAnalytics("test.json", "content");
        assertFalse(result);
    }
    
    @Test
    public void testListAnalyticsFiles_StorageNull_EnabledFalse() {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", false);
        ReflectionTestUtils.setField(service, "storage", null);
        
        List<String> files = service.listAnalyticsFiles();
        assertTrue(files.isEmpty());
    }
    
    @Test
    public void testListAnalyticsFiles_StorageNull_EnabledTrue() {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", true);
        ReflectionTestUtils.setField(service, "storage", null);
        
        List<String> files = service.listAnalyticsFiles();
        assertTrue(files.isEmpty());
    }
    
    @Test
    public void testListAnalyticsFiles_StorageNotNull_EnabledFalse() {
        Storage mockStorage = mock(Storage.class);
        
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", false);
        ReflectionTestUtils.setField(service, "storage", mockStorage);
        
        List<String> files = service.listAnalyticsFiles();
        assertTrue(files.isEmpty());
    }
    
    @Test
    public void testIsAvailable_BothNull() {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", false);
        ReflectionTestUtils.setField(service, "storage", null);
        
        assertFalse(service.isAvailable());
    }
    
    @Test
    public void testIsAvailable_StorageNullEnabledTrue() {
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", true);
        ReflectionTestUtils.setField(service, "storage", null);
        
        assertFalse(service.isAvailable());
    }
    
    @Test
    public void testIsAvailable_StorageNotNullEnabledFalse() {
        Storage mockStorage = mock(Storage.class);
        
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", false);
        ReflectionTestUtils.setField(service, "storage", mockStorage);
        
        assertFalse(service.isAvailable());
    }
    
    @Test
    public void testIsAvailable_BothNotNull() {
        Storage mockStorage = mock(Storage.class);
        
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", true);
        ReflectionTestUtils.setField(service, "storage", mockStorage);
        
        assertTrue(service.isAvailable());
    }
    
    @Test
    public void testUploadAnalytics_ExceptionPath() {
        Storage mockStorage = mock(Storage.class);
        when(mockStorage.create(any(BlobInfo.class), any(byte[].class)))
            .thenThrow(new RuntimeException("Upload failed"));
        
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", true);
        ReflectionTestUtils.setField(service, "storage", mockStorage);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
        
        boolean result = service.uploadAnalytics("test.json", "content");
        assertFalse(result);
    }
    
    @Test
    public void testListAnalyticsFiles_ExceptionPath() {
        Storage mockStorage = mock(Storage.class);
        when(mockStorage.list(anyString(), any(Storage.BlobListOption.class)))
            .thenThrow(new RuntimeException("List failed"));
        
        StorageService service = new StorageService();
        ReflectionTestUtils.setField(service, "storageEnabled", true);
        ReflectionTestUtils.setField(service, "storage", mockStorage);
        ReflectionTestUtils.setField(service, "bucketName", "test-bucket");
        
        List<String> files = service.listAnalyticsFiles();
        assertTrue(files.isEmpty());
    }
}
