package com.example.stadiumflow;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.service.IoTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IoTDataControllerTest {

    @Mock
    private IoTService ioTService;

    private IoTDataController ioTDataController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ioTDataController = new IoTDataController(ioTService);
    }

    @Test
    public void testStreamIoTData() {
        SseEmitter mockEmitter = new SseEmitter();
        when(ioTService.registerClient()).thenReturn(mockEmitter);
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        SseEmitter result = ioTDataController.streamIoTData(response);
        
        assertNotNull(result);
        assertEquals(mockEmitter, result);
        assertEquals("no-cache", response.getHeader("Cache-Control"));
        assertEquals("no", response.getHeader("X-Accel-Buffering"));
        verify(ioTService).registerClient();
    }

    @Test
    public void testStreamIoTData_MultipleClients() {
        SseEmitter mockEmitter1 = new SseEmitter();
        SseEmitter mockEmitter2 = new SseEmitter();
        
        when(ioTService.registerClient())
            .thenReturn(mockEmitter1)
            .thenReturn(mockEmitter2);
        
        MockHttpServletResponse response1 = new MockHttpServletResponse();
        MockHttpServletResponse response2 = new MockHttpServletResponse();
        
        SseEmitter result1 = ioTDataController.streamIoTData(response1);
        SseEmitter result2 = ioTDataController.streamIoTData(response2);
        
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotEquals(result1, result2);
        verify(ioTService, times(2)).registerClient();
    }

    @Test
    public void testGetIoTData() {
        Zone zone1 = new Zone("Gate_A", "Gate A", 10, 50);
        Zone zone2 = new Zone("Gate_B", "Gate B", 15, 60);
        List<Zone> zones = Arrays.asList(zone1, zone2);
        
        when(ioTService.getAllZones()).thenReturn(zones);
        
        List<Zone> result = ioTDataController.getIoTData();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Gate_A", result.get(0).getId());
        assertEquals("Gate_B", result.get(1).getId());
        verify(ioTService).getAllZones();
    }

    @Test
    public void testGetIoTData_EmptyList() {
        when(ioTService.getAllZones()).thenReturn(Arrays.asList());
        
        List<Zone> result = ioTDataController.getIoTData();
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetIoTData_MultipleRequests() {
        Zone zone1 = new Zone("Gate_A", "Gate A", 10, 50);
        List<Zone> zones = Arrays.asList(zone1);

        when(ioTService.getAllZones()).thenReturn(zones);

        List<Zone> result1 = ioTDataController.getIoTData();
        List<Zone> result2 = ioTDataController.getIoTData();

        assertNotNull(result1);
        assertNotNull(result2);
        verify(ioTService, times(2)).getAllZones();
    }

    @Test
    public void testStreamIoTData_SetsCorrectHeaders() {
        SseEmitter mockEmitter = new SseEmitter();
        when(ioTService.registerClient()).thenReturn(mockEmitter);

        MockHttpServletResponse response = new MockHttpServletResponse();
        ioTDataController.streamIoTData(response);

        assertEquals("no-cache", response.getHeader("Cache-Control"));
        assertEquals("no", response.getHeader("X-Accel-Buffering"));
    }

    @Test
    public void testGetIoTData_WithMultipleZones() {
        Zone zone1 = new Zone("Gate_A", "Gate A", 10, 50);
        Zone zone2 = new Zone("Gate_B", "Gate B", 15, 60);
        Zone zone3 = new Zone("Section_112", "Section 112", 5, 25);
        List<Zone> zones = Arrays.asList(zone1, zone2, zone3);

        when(ioTService.getAllZones()).thenReturn(zones);

        List<Zone> result = ioTDataController.getIoTData();

        assertEquals(3, result.size());
        assertEquals("Gate_A", result.get(0).getId());
        assertEquals("Gate_B", result.get(1).getId());
        assertEquals("Section_112", result.get(2).getId());
    }

    @Test
    public void testStreamIoTData_DoesNotThrow() {
        SseEmitter mockEmitter = new SseEmitter();
        when(ioTService.registerClient()).thenReturn(mockEmitter);

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertDoesNotThrow(() -> ioTDataController.streamIoTData(response));
    }

    @Test
    public void testGetIoTData_ReturnsCorrectDataStructure() {
        Zone zone = new Zone("Test_Zone", "Test Zone", 12, 45);
        when(ioTService.getAllZones()).thenReturn(Arrays.asList(zone));

        List<Zone> result = ioTDataController.getIoTData();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test_Zone", result.get(0).getId());
        assertEquals("Test Zone", result.get(0).getName());
        assertEquals(12, result.get(0).getWaitTime());
        assertEquals(45, result.get(0).getDensity());
    }

    @Test
    public void testStreamIoTData_ReturnsNonNullEmitter() {
        SseEmitter mockEmitter = new SseEmitter();
        when(ioTService.registerClient()).thenReturn(mockEmitter);

        MockHttpServletResponse response = new MockHttpServletResponse();
        SseEmitter result = ioTDataController.streamIoTData(response);

        assertNotNull(result);
    }
}
