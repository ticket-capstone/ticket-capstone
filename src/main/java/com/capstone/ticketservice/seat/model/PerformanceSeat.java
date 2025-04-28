package com.capstone.ticketservice.seat.model;

import com.capstone.ticketservice.event.model.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PerformanceSeat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "performance_seat_id")
    private Long performanceSeatId;

    private Integer price;
    private String status;

    @Version
    private Integer version;

    @Column(name = "lock_until")
    private LocalDateTime lockUntil;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    public boolean isLocked() {
        return lockUntil != null && lockUntil.isAfter(LocalDateTime.now());
    }

    public void lock(int lockTimeInSeconds) {
        this.lockUntil = LocalDateTime.now().plusSeconds(lockTimeInSeconds);
        this.status = "LOCKED";
    }

    public void unlock() {
        this.lockUntil = null;
        this.status = "AVAILABLE";
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
