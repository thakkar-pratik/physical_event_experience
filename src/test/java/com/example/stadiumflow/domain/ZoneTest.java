package com.example.stadiumflow.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ZoneTest {

    @Test
    public void testZoneGettersAndSetters() {
        Zone zone = new Zone("Gate_A", "Gate A North", 15, 80);
        
        assertEquals("Gate_A", zone.getId());
        assertEquals("Gate A North", zone.getName());
        assertEquals(15, zone.getWaitTime());
        assertEquals(80, zone.getDensity());
        
        zone.setId("Gate_B");
        zone.setName("Gate B South");
        zone.setWaitTime(5);
        zone.setDensity(20);
        
        assertEquals("Gate_B", zone.getId());
        assertEquals("Gate B South", zone.getName());
        assertEquals(5, zone.getWaitTime());
        assertEquals(20, zone.getDensity());
        
        Zone emptyZone = new Zone();
        assertNull(emptyZone.getId());
    }
}
