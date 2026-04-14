package com.example.stadiumflow;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ZoneRepositoryTest {

    @Autowired
    private ZoneRepository zoneRepository;

    @Test
    public void testCreateAndFindZone() {
        // Given
        Zone zone = new Zone("Gate_X", "Secret Gate", 10, 50);
        zoneRepository.save(zone);

        // When
        Zone found = zoneRepository.findById("Gate_X").orElse(null);

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Secret Gate");
    }

    @Test
    public void testUpdateWaitTime() {
        // Given
        Zone zone = new Zone("Gate_A", "Gate A", 5, 20);
        zoneRepository.save(zone);

        // When
        zone.setWaitTime(15);
        zoneRepository.save(zone);
        Zone updated = zoneRepository.findById("Gate_A").get();

        // Then
        assertThat(updated.getWaitTime()).isEqualTo(15);
    }
}
