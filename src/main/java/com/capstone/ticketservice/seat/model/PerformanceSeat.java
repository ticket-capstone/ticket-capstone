package com.capstone.ticketservice.seat.model;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.user.model.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PERFORMANCESEAT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERFORMANCE_SEAT_ID")
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
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEAT_ID", nullable = false)
    private Seat seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locked_by_user_id")
    private Users lockedByUser;  // 좌석을 락한 사용자

    public boolean isLocked() {
        return lockUntil != null && lockUntil.isAfter(LocalDateTime.now());
    }

    public void lock(int lockTimeInSeconds, Users user) {
        this.lockUntil = LocalDateTime.now().plusSeconds(lockTimeInSeconds);
        this.status = "LOCKED";
        this.lockedByUser = user;
    }

    public void unlock() {
        this.lockUntil = null;
        this.status = "AVAILABLE";
        this.lockedByUser = null;
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
