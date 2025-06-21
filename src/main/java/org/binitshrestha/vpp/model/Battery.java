package org.binitshrestha.vpp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batteries", indexes = {
        @Index(name = "idx_postcode", columnList = "postcode"),
        @Index(name = "idx_watt_capacity", columnList = "wattCapacity")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Battery {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "Battery name is required")
    private String name;

    @Column(nullable = false, length = 10)
    @NotBlank(message = "Postcode is required")
    private String postcode;

    @Column(nullable = false)
    @NotNull(message = "Watt capacity is required")
    @Positive(message = "Watt capacity must be positive")
    private Double wattCapacity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BatteryStatus status = BatteryStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum BatteryStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }
}