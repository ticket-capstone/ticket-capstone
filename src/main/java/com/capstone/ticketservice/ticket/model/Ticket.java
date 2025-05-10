package com.capstone.ticketservice.ticket.model;

import com.capstone.ticketservice.order.model.OrderItem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Random;

@Entity
@Table(name = "Ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "access_code")
    private Long accessCode;

    private String status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_item_id", nullable=false)
    private OrderItem orderItem;

    @PrePersist
    protected void onCreate() {
        issuedAt = LocalDateTime.now();
        usedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (accessCode == null) {
            Random random = new Random();
            long min = 100000000000L; // 12자리 최소값
            long max = 999999999999L; // 12자리 최대값
            accessCode = min + ((long) (random.nextDouble() * (max - min)));
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
