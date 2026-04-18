package com.example.stadiumflow.service;

import com.example.stadiumflow.domain.Zone;
import com.example.stadiumflow.repository.ZoneRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class IoTService {

    private final ZoneRepository zoneRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Random random = new Random();

    // PERFORMANCE OPTIMIZATION: Cache the 2KB preamble to avoid repeated string allocation and loops.
    private static final String SSE_PADDING = " ".repeat(2048);

    public IoTService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @PostConstruct
    public void seedDatabase() {
        zoneRepository.save(new Zone("Gate_A", "North Stand Entrance (General)", 5, 20));
        zoneRepository.save(new Zone("Gate_C", "VIP Turf Access (Yellow Zone)", 5, 20));
        zoneRepository.save(new Zone("Section_112", "Food Village (East Concourse)", 5, 20));
        zoneRepository.save(new Zone("Section_120", "Hydration Station 1 (West)", 5, 20));
        System.out.println("✅ JPA: Initialized 4 Coldplay Stadium Zones in H2 Database.");
    }

    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);
        
        try {
            // "MAX OUT" CLOUD FLUSHING: Use cached 2KB preamble of comments.
            emitter.send(SseEmitter.event().comment(SSE_PADDING));
            
            // Immediate first data push
            emitter.send(SseEmitter.event().data(getAllZones()));
        } catch (IOException e) {
            emitter.complete();
            this.emitters.remove(emitter);
        }
        
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        return emitter;
    }

    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    @Scheduled(fixedRate = 5000)
    public void simulateSensorDataAndSaveToDB() {
        List<Zone> zones = zoneRepository.findAll();
        for (Zone zone : zones) {
            int time = random.nextInt(30);
            if (time == 0) time = 1;
            zone.setWaitTime(time);
        }

        // PERFORMANCE OPTIMIZATION: Only call saveAll if there are zones to update.
        if (!zones.isEmpty()) {
            List<Zone> updatedZones = zoneRepository.saveAll(zones);
            broadcastData(updatedZones);
        }
    }

    // Adding a 15-second heartbeat to keep Cloud Run CPU active during idle times
    @Scheduled(fixedRate = 15000)
    public void sendHeartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("ping").data("keep-alive"));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    private void broadcastData(Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
