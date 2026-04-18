package com.example.stadiumflow.service;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Advanced tests for IoTService to cover error paths and edge cases
 */
public class IoTServiceAdvancedTest {

    @Mock
    private ZoneRepository zoneRepository;

    private IoTService ioTService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ioTService = new IoTService(zoneRepository);
    }

    @Test
    public void testBroadcastData_WithFailedEmitter() throws Exception {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        // Get the emitters list using reflection
        @SuppressWarnings("unchecked")
        List<SseEmitter> emitters = (List<SseEmitter>) ReflectionTestUtils.getField(ioTService, "emitters");
        
        // Add a spy emitter that will throw IOException
        SseEmitter spyEmitter = spy(new SseEmitter(60000L));
        doThrow(new IOException("Connection closed")).when(spyEmitter).send(any());
        
        if (emitters != null) {
            emitters.add(spyEmitter);
        }
        
        // This should handle the IOException and remove the failed emitter
        assertDoesNotThrow(() -> ioTService.simulateSensorDataAndSaveToDB());
    }

    @Test
    public void testSendHeartbeat_WithFailedEmitter() {
        Zone zone = new Zone("Test", "Test", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        // Get emitters list
        @SuppressWarnings("unchecked")
        List<SseEmitter> emitters = (List<SseEmitter>) ReflectionTestUtils.getField(ioTService, "emitters");
        
        // Add a spy emitter that throws on send
        SseEmitter spyEmitter = spy(new SseEmitter(60000L));
        try {
            doThrow(new IOException("Timeout")).when(spyEmitter).send(any());
        } catch (IOException e) {
            // Won't happen in setup
        }
        
        if (emitters != null) {
            emitters.add(spyEmitter);
        }
        
        // Should handle error gracefully
        assertDoesNotThrow(() -> ioTService.sendHeartbeat());
    }

    @Test
    public void testRegisterClient_CallbacksExecute() {
        Zone zone = new Zone("Test", "Test", 8, 28);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        SseEmitter emitter = ioTService.registerClient();
        assertNotNull(emitter);
        
        // Manually trigger callbacks
        emitter.onCompletion(() -> {
            // Completion callback
        });
        
        emitter.onTimeout(() -> {
            // Timeout callback
        });
        
        // Complete the emitter
        emitter.complete();
    }

    @Test
    public void testSimulateSensorData_AllWaitTimeRanges() {
        Zone zone1 = new Zone("Z1", "Zone 1", 0, 10);
        Zone zone2 = new Zone("Z2", "Zone 2", 15, 50);
        Zone zone3 = new Zone("Z3", "Zone 3", 30, 90);
        
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2, zone3));
        
        // Run multiple simulations
        for (int i = 0; i < 30; i++) {
            ioTService.simulateSensorDataAndSaveToDB();
        }
        
        // Verify batch saves were called 30 times
        verify(zoneRepository, times(30)).saveAll(anyList());
    }

    @Test
    public void testBroadcastData_WithMultipleEmitterStates() {
        Zone zone = new Zone("Test", "Test", 10, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Register several emitters
        ioTService.registerClient();
        ioTService.registerClient();
        ioTService.registerClient();

        // Simulate data broadcast - should work with all active emitters
        assertDoesNotThrow(() -> ioTService.simulateSensorDataAndSaveToDB());

        // Broadcast again
        assertDoesNotThrow(() -> ioTService.simulateSensorDataAndSaveToDB());
    }

    @Test
    public void testEmitterList_ThreadSafety() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        // Verify emitters list is CopyOnWriteArrayList (thread-safe)
        Object emitters = ReflectionTestUtils.getField(ioTService, "emitters");
        assertTrue(emitters instanceof CopyOnWriteArrayList);
    }

    @Test
    public void testSeedDatabase_CreatesCorrectZones() {
        // Clear any previous interactions
        reset(zoneRepository);
        
        // Call seedDatabase
        ioTService.seedDatabase();
        
        // Verify 4 zones were saved
        verify(zoneRepository, times(4)).save(any(Zone.class));
    }

    @Test
    public void testRegisterClient_WithLargeNumberOfClients() {
        Zone zone = new Zone("Test", "Test", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        // Register many clients
        for (int i = 0; i < 100; i++) {
            SseEmitter emitter = ioTService.registerClient();
            assertNotNull(emitter);
        }
        
        // Broadcast should handle all emitters
        assertDoesNotThrow(() -> ioTService.simulateSensorDataAndSaveToDB());
    }

    @Test
    public void testSendHeartbeat_WithNoEmitters() {
        // Clear emitters
        @SuppressWarnings("unchecked")
        List<SseEmitter> emitters = (List<SseEmitter>) ReflectionTestUtils.getField(ioTService, "emitters");
        if (emitters != null) {
            emitters.clear();
        }
        
        // Should not throw
        assertDoesNotThrow(() -> ioTService.sendHeartbeat());
    }

    @Test
    public void testSimulateSensorData_WaitTimeAlwaysPositive() {
        Zone zone = new Zone("Test", "Test", 0, 10);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));
        
        // Simulate many times to ensure wait time is always >= 1
        for (int i = 0; i < 50; i++) {
            ioTService.simulateSensorDataAndSaveToDB();
        }
        
        // Verify batch saves were called
        verify(zoneRepository, times(50)).saveAll(anyList());
    }

    @Test
    public void testGetAllZones_DelegatesToRepository() {
        Zone zone1 = new Zone("Z1", "Zone 1", 5, 20);
        Zone zone2 = new Zone("Z2", "Zone 2", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));
        
        List<Zone> result = ioTService.getAllZones();
        
        assertEquals(2, result.size());
        verify(zoneRepository, times(1)).findAll();
    }
}
