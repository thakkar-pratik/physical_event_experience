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

    @Test
    public void testParameterizedConstructor() {
        ConcessionOrder order = new ConcessionOrder("VIP_Zone", "Premium Meal", 25.50, false);

        assertEquals("VIP_Zone", order.getZoneId());
        assertEquals("Premium Meal", order.getItemMenu());
        assertEquals(25.50, order.getPaidPrice());
        assertFalse(order.isOfflineMesh());
        assertNotNull(order.getTimestamp());
    }

    @Test
    public void testNullValues() {
        ConcessionOrder order = new ConcessionOrder(null, null, 0.0, false);
        assertNull(order.getZoneId());
        assertNull(order.getItemMenu());
        assertEquals(0.0, order.getPaidPrice());
        assertNotNull(order.getTimestamp());
    }

    @Test
    public void testNegativePrice() {
        ConcessionOrder order = new ConcessionOrder("Test", "Item", -5.0, true);
        assertEquals(-5.0, order.getPaidPrice());
    }

    @Test
    public void testHighPrice() {
        ConcessionOrder order = new ConcessionOrder("VIP", "Premium", 999.99, true);
        assertEquals(999.99, order.getPaidPrice());
    }

    @Test
    public void testDefaultConstructor() {
        ConcessionOrder order = new ConcessionOrder();
        assertNotNull(order);
        assertNull(order.getId());
        assertNull(order.getZoneId());
        assertNull(order.getItemMenu());
        assertNull(order.getTimestamp());
    }

    @Test
    public void testOfflineMeshTrue() {
        ConcessionOrder order = new ConcessionOrder("Gate_A", "Water", 2.0, true);
        assertTrue(order.isOfflineMesh());
    }

    @Test
    public void testOfflineMeshFalse() {
        ConcessionOrder order = new ConcessionOrder("Gate_B", "Soda", 3.5, false);
        assertFalse(order.isOfflineMesh());
    }

    @Test
    public void testTimestampGenerated() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        ConcessionOrder order = new ConcessionOrder("Section_1", "Burger", 8.0, true);
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(order.getTimestamp());
        assertTrue(order.getTimestamp().isAfter(before));
        assertTrue(order.getTimestamp().isBefore(after));
    }
}
