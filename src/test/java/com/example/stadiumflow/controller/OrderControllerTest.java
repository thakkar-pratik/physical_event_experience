package com.example.stadiumflow.controller;

import com.example.stadiumflow.domain.ConcessionOrder;
import com.example.stadiumflow.dto.GenericResponse;
import com.example.stadiumflow.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;



public class OrderControllerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPlaceOrder() {
        ConcessionOrder order = new ConcessionOrder("Section_112", "Mega Hotdog", 8.0, true);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        ResponseEntity<GenericResponse> response = orderController.placeOrder(order);

        verify(orderRepository).save(order);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("success", response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("stadium ledger"));
    }

    @Test
    public void testPlaceOrder_OfflineMesh() {
        ConcessionOrder order = new ConcessionOrder("Section_120", "Water Bottle", 2.5, true);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        ResponseEntity<GenericResponse> response = orderController.placeOrder(order);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        verify(orderRepository).save(order);
    }

    @Test
    public void testPlaceOrder_OnlineMode() {
        ConcessionOrder order = new ConcessionOrder("VIP_Zone", "Premium Meal", 25.0, false);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        ResponseEntity<GenericResponse> response = orderController.placeOrder(order);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    public void testPlaceOrder_WithEmptyZone() {
        ConcessionOrder order = new ConcessionOrder("", "Item", 5.0, false);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        ResponseEntity<GenericResponse> response = orderController.placeOrder(order);
        assertNotNull(response);
    }

    @Test
    public void testPlaceOrder_WithNullZone() {
        ConcessionOrder order = new ConcessionOrder(null, "Item", 5.0, false);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        ResponseEntity<GenericResponse> response = orderController.placeOrder(order);
        assertNotNull(response);
    }

    @Test
    public void testPlaceOrder_MultipleTimes() {
        ConcessionOrder order = new ConcessionOrder("Section_112", "Hotdog", 8.0, true);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        for (int i = 0; i < 10; i++) {
            ResponseEntity<GenericResponse> response = orderController.placeOrder(order);
            assertEquals("success", response.getBody().getStatus());
        }

        verify(orderRepository, times(10)).save(any(ConcessionOrder.class));
    }

    @Test
    public void testPlaceOrder_VerifyResponseStructure() {
        ConcessionOrder order = new ConcessionOrder("Test", "Test", 5.0, false);
        when(orderRepository.save(any(ConcessionOrder.class))).thenReturn(order);

        ResponseEntity<GenericResponse> response = orderController.placeOrder(order);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getStatus());
        assertNotNull(response.getBody().getMessage());
        assertNotNull(response.getBody().getTimestamp());
    }
}
