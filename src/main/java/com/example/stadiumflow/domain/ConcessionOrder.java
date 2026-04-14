package com.example.stadiumflow.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

@Entity
public class ConcessionOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Zone ID must be provided")
    private String zoneId;     // e.g., "Section_112"
    
    @NotBlank(message = "Item Menu must be provided")
    private String itemMenu;   // e.g., "Mega Hotdog"
    
    @PositiveOrZero(message = "Paid price Cannot be negative")
    private double paidPrice;  // Store exact price paid due to dynamic surge pricing
    
    private boolean isOfflineMesh; // Was this ordered while network was down?
    private LocalDateTime timestamp;

    public ConcessionOrder() {}

    public ConcessionOrder(String zoneId, String itemMenu, double paidPrice, boolean isOfflineMesh) {
        this.zoneId = zoneId;
        this.itemMenu = itemMenu;
        this.paidPrice = paidPrice;
        this.isOfflineMesh = isOfflineMesh;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getZoneId() { return zoneId; }
    public String getItemMenu() { return itemMenu; }
    public double getPaidPrice() { return paidPrice; }
    public boolean isOfflineMesh() { return isOfflineMesh; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
