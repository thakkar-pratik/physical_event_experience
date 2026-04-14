package com.example.stadiumflow.controller;

import com.example.stadiumflow.domain.ConcessionOrder;
import com.example.stadiumflow.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        
        String response = orderController.placeOrder(order);
        
        verify(orderRepository).save(order);
        assertEquals("{\"status\": \"success\", \"message\": \"Order Saved\"}", response);
    }
}
