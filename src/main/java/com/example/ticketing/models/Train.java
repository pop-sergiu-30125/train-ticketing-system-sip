package com.example.ticketing.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String trainNumber;

    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private TrainRoute route;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private LocalDateTime baseDepartureTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer delayMinutes = 0;

    @PrePersist
    protected void onCreate() {
        if (delayMinutes == null) {
            delayMinutes = 0;
        }
    }

    public LocalDateTime getActualDepartureTime() {
        return baseDepartureTime.plusMinutes(delayMinutes != null ? delayMinutes : 0);
    }

    public LocalDateTime getActualArrivalTime(Integer travelTimeOffsetMinutes) {
        return baseDepartureTime.plusMinutes(travelTimeOffsetMinutes).plusMinutes(delayMinutes != null ? delayMinutes : 0);
    }
}
