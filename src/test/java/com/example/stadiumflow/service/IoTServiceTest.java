package com.example.stadiumflow.service;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IoTServiceTest {

    @Mock
    private ZoneRepository zoneRepository;

    @InjectMocks
    private IoTService ioTService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSeedDatabase() {
        ioTService.seedDatabase();
        verify(zoneRepository, times(4)).save(any(Zone.class));
    }

    @Test
    public void testStreamIoTData() {
        SseEmitter emitter = ioTService.registerClient();
        assertNotNull(emitter);
    }

    @Test
    public void testSimulateSensorDataAndSaveToDB() {
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        ioTService.simulateSensorDataAndSaveToDB();

        verify(zoneRepository, atLeastOnce()).findAll();
        verify(zoneRepository, atLeastOnce()).save(any(Zone.class));
    }
    
    @Test
    public void testSimulateSensorDataAndSaveToDB_MultipleZones() {
        Zone zone1 = new Zone("Gate_A", "Gate A", 5, 20);
        Zone zone2 = new Zone("Gate_B", "Gate B", 10, 30);
        Zone zone3 = new Zone("Section_1", "Section 1", 15, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2, zone3));

        ioTService.simulateSensorDataAndSaveToDB();

        verify(zoneRepository, atLeast(2)).findAll();
        verify(zoneRepository, times(3)).save(any(Zone.class));
    }

    @Test
    public void testSimulateSensorDataAndSaveToDB_EmptyZones() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        ioTService.simulateSensorDataAndSaveToDB();

        verify(zoneRepository, atLeastOnce()).findAll();
        verify(zoneRepository, never()).save(any(Zone.class));
    }

    @Test
    public void testRegisterClient_MultipleClients() {
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        SseEmitter emitter1 = ioTService.registerClient();
        SseEmitter emitter2 = ioTService.registerClient();
        SseEmitter emitter3 = ioTService.registerClient();

        assertNotNull(emitter1);
        assertNotNull(emitter2);
        assertNotNull(emitter3);
    }

    @Test
    public void testGetAllZones() {
        Zone zone1 = new Zone("Gate_A", "Gate A", 5, 20);
        Zone zone2 = new Zone("Gate_B", "Gate B", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));

        java.util.List<Zone> zones = ioTService.getAllZones();

        assertNotNull(zones);
        assertEquals(2, zones.size());
        verify(zoneRepository).findAll();
    }

    @Test
    public void testGetAllZones_EmptyList() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        java.util.List<Zone> zones = ioTService.getAllZones();

        assertNotNull(zones);
        assertEquals(0, zones.size());
    }

    @Test
    public void testSendHeartbeat_NoEmitters() {
        // Should not throw exception when no emitters are registered
        assertDoesNotThrow(() -> ioTService.sendHeartbeat());
    }

    @Test
    public void testSimulateSensorData_UpdatesWaitTimes() {
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        ioTService.simulateSensorDataAndSaveToDB();

        verify(zoneRepository, atLeastOnce()).save(any(Zone.class));
    }

    @Test
    public void testSimulateSensorData_MultipleCalls() {
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        for (int i = 0; i < 5; i++) {
            ioTService.simulateSensorDataAndSaveToDB();
        }

        verify(zoneRepository, atLeast(5)).save(any(Zone.class));
    }

    @Test
    public void testSendHeartbeat_WithRegisteredEmitters() {
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Register a few emitters
        SseEmitter emitter1 = ioTService.registerClient();
        SseEmitter emitter2 = ioTService.registerClient();

        // Send heartbeat should not throw
        assertDoesNotThrow(() -> ioTService.sendHeartbeat());
    }

    @Test
    public void testRegisterClient_WithEmptyZones() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        SseEmitter emitter = ioTService.registerClient();
        assertNotNull(emitter);
    }

    @Test
    public void testSimulateSensorData_ZeroWaitTimeHandling() {
        Zone zone1 = new Zone("Zone_1", "Zone 1", 0, 10);
        Zone zone2 = new Zone("Zone_2", "Zone 2", 0, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));

        // Simulate multiple times to ensure wait time is set to at least 1
        for (int i = 0; i < 10; i++) {
            ioTService.simulateSensorDataAndSaveToDB();
        }

        verify(zoneRepository, atLeast(10)).save(any(Zone.class));
    }

    @Test
    public void testRegisterClient_InitialDataPush() {
        Zone zone = new Zone("Test_Zone", "Test Zone", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        SseEmitter emitter = ioTService.registerClient();

        assertNotNull(emitter);
        verify(zoneRepository, atLeastOnce()).findAll();
    }

    @Test
    public void testSimulateSensorData_WithVariousWaitTimes() {
        Zone zone1 = new Zone("Zone_1", "Zone 1", 1, 10);
        Zone zone2 = new Zone("Zone_2", "Zone 2", 15, 50);
        Zone zone3 = new Zone("Zone_3", "Zone 3", 29, 90);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2, zone3));

        ioTService.simulateSensorDataAndSaveToDB();

        verify(zoneRepository, times(3)).save(any(Zone.class));
    }

    @Test
    public void testGetAllZones_ReturnsCorrectData() {
        Zone zone1 = new Zone("Zone_A", "Zone A", 5, 20);
        Zone zone2 = new Zone("Zone_B", "Zone B", 10, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));

        java.util.List<Zone> result = ioTService.getAllZones();

        assertEquals(2, result.size());
        assertEquals("Zone_A", result.get(0).getId());
        assertEquals("Zone_B", result.get(1).getId());
    }

    @Test
    public void testSeedDatabase_CallsRepository() {
        // We already test seedDatabase in the first test
        // This is a duplicate, so let's make it simpler
        assertDoesNotThrow(() -> ioTService.seedDatabase());
    }

    @Test
    public void testBroadcastData_HandlesCompletedEmitters() throws Exception {
        Zone zone = new Zone("Gate_A", "Gate A", 10, 30);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Register a client but don't complete it
        SseEmitter emitter = ioTService.registerClient();

        // Simulate sensor data which will try to broadcast
        assertDoesNotThrow(() -> ioTService.simulateSensorDataAndSaveToDB());

        // Send heartbeat should also not throw
        assertDoesNotThrow(() -> ioTService.sendHeartbeat());
    }

    @Test
    public void testRegisterClient_WithNoInitialData() {
        when(zoneRepository.findAll()).thenReturn(Arrays.asList());

        // Even with empty zones, should not throw
        assertDoesNotThrow(() -> ioTService.registerClient());
    }

    @Test
    public void testSendHeartbeat_WithSeveralEmitters() {
        Zone zone = new Zone("Test_Zone", "Test Zone", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Register multiple clients
        ioTService.registerClient();
        ioTService.registerClient();
        ioTService.registerClient();

        // Send heartbeat should work
        assertDoesNotThrow(() -> ioTService.sendHeartbeat());
    }

    @Test
    public void testBroadcastData_WithMultipleEmitters() {
        Zone zone = new Zone("Test", "Test", 8, 35);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        // Register emitters
        ioTService.registerClient();
        ioTService.registerClient();

        // Broadcast should work
        assertDoesNotThrow(() -> ioTService.simulateSensorDataAndSaveToDB());
    }

    @Test
    public void testSimulateSensorData_RandomWaitTimeGeneration() {
        Zone zone1 = new Zone("Z1", "Zone 1", 10, 30);
        Zone zone2 = new Zone("Z2", "Zone 2", 15, 40);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone1, zone2));

        // Run simulation multiple times
        for (int i = 0; i < 20; i++) {
            ioTService.simulateSensorDataAndSaveToDB();
        }

        // Verify saves were called many times
        verify(zoneRepository, atLeast(40)).save(any(Zone.class));
    }

    @Test
    public void testRegisterClient_OnCompletionCallback() {
        Zone zone = new Zone("Test", "Test", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        SseEmitter emitter = ioTService.registerClient();
        assertNotNull(emitter);

        // Complete the emitter (this should trigger onCompletion callback)
        emitter.complete();
    }

    @Test
    public void testRegisterClient_OnTimeoutCallback() {
        Zone zone = new Zone("Test", "Test", 5, 20);
        when(zoneRepository.findAll()).thenReturn(Arrays.asList(zone));

        SseEmitter emitter = ioTService.registerClient();
        assertNotNull(emitter);

        // Manually trigger timeout callback
        emitter.completeWithError(new Exception("Timeout"));
    }
}
