package com.capstone.ticketservice.waitingqueue.model;

import jakarta.persistence.*;
import lombok.*;
import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.user.model.Users;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@Table(name = "WAITINGQUEUE")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingQueue {

    public enum QueueStatus {
        WAITING, PROCESSING, COMPLETED, TIMEOUT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "queue_id")
    private Long queueId;

    @Enumerated(EnumType.STRING) // 데이터가 string 값으로 저장됨 , 적지 않을 경우 1,2로 매핑되어 저장됨 -> 순서바뀌면 매핑테이블 에러남.
    private QueueStatus status;

    private LocalDateTime entryTime;
    private LocalDateTime processingStartedAt;

    @ManyToOne(fetch = FetchType.LAZY)  //user 필드에 접근하기 전까지 DB에서 조회 X -> 최적화
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        entryTime = now;
    }//protected인 이유 -> 프래임워크에서만 자동으로 호출하게 하여 안정성을 높임.

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
