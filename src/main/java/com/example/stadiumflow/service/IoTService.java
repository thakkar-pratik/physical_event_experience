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

    public IoTService(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    @PostConstruct
    public void seedDatabase() {
        zoneRepository.save(new Zone("Gate_A", "Gate A", 5, 20));
        zoneRepository.save(new Zone("Gate_C", "Gate C", 5, 20));
        zoneRepository.save(new Zone("Section_112", "Section 112", 5, 20));
        zoneRepository.save(new Zone("Section_120", "Section 120", 5, 20));
        System.out.println("✅ JPA: Initialized 4 Stadium Zones in H2 Database.");
    }

    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        this.emitters.add(emitter);
        
        try {
            // "MAX OUT" CLOUD FLUSHING: Send a 2KB preamble of comments.
            // Many proxies (Cloud Run, Nginx, etc.) buffer the first few KB of a stream.
            // This padding forces an immediate flush so the fan gets data in < 1 second.
            StringBuilder padding = new StringBuilder();
            for (int i = 0; i < 2048; i++) padding.append(" ");
            emitter.send(SseEmitter.event().comment(padding.toString()));
            
            // Immediate first data push
            emitter.send(SseEmitter.event().data(zoneRepository.findAll()));
        } catch (IOException e) {
            emitter.complete();
            this.emitters.remove(emitter);
        }
        
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        return emitter;
    }

    @Scheduled(fixedRate = 5000)
    public void simulateSensorDataAndSaveToDB() {
        List<Zone> zones = zoneRepository.findAll();
        for (Zone zone : zones) {
            int time = random.nextInt(30);
            if (time == 0) time = 1;
            zone.setWaitTime(time);
            zoneRepository.save(zone); 
        }

        broadcastData(zoneRepository.findAll());
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
