package com.example.stadiumflow.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ConcessionOrderTest {

    @Test
    public void testConcessionOrderGettersAndSetters() {
        ConcessionOrder order = new ConcessionOrder("Section_112", "Mega Hotdog", 8.0, true);
        
        assertEquals("Section_112", order.getZoneId());
        assertEquals("Mega Hotdog", order.getItemMenu());
        assertEquals(8.0, order.getPaidPrice());
        assertTrue(order.isOfflineMesh());
        assertNotNull(order.getTimestamp());
        assertNull(order.getId());
        
        ConcessionOrder emptyOrder = new ConcessionOrder();
        assertNull(emptyOrder.getZoneId());
    }
}
