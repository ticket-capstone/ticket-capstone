package com.capstone.ticketservice.waitingqueue.dto;

import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingQueueDto {
    private Long queueId;
    private Integer position;
    private String status;
    private LocalDateTime entryTime;
    private LocalDateTime processingStartedAt;
    private Long userId;
    private Long eventId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WaitingQueueDto fromEntity(WaitingQueue waitingQueue) {
        return WaitingQueueDto.builder()
                .queueId(waitingQueue.getQueueId())
                .status(waitingQueue.getStatus().name()) // Enum → String 변환
                .entryTime(waitingQueue.getEntryTime())
                .processingStartedAt(waitingQueue.getProcessingStartedAt())
                .userId(waitingQueue.getUser().getUserId())
                .eventId(waitingQueue.getEvent().getEventId())
                .createdAt(waitingQueue.getCreatedAt())
                .updatedAt(waitingQueue.getUpdatedAt())
                .build();
    }

}

