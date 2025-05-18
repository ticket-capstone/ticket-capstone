package com.capstone.ticketservice.seat.dto;

import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSeatDto {
    private Long performanceSeatId;
    private Integer price;
    private String status;
    private LocalDateTime lockUntil;
    private Long eventId;
    private String eventName;
    private Long seatId;
    private String rowName;
    private String seatNumber;
    private Long sectionId;
    private String sectionName;
    private Long lockedByUserId;

    public static PerformanceSeatDto fromEntity(PerformanceSeat performanceSeat) {
        return PerformanceSeatDto.builder()
                .performanceSeatId(performanceSeat.getPerformanceSeatId())
                .price(performanceSeat.getPrice())
                .status(performanceSeat.getStatus())
                .lockUntil(performanceSeat.getLockUntil())
                .eventId(performanceSeat.getEvent().getEventId())
                .eventName(performanceSeat.getEvent().getName())
                .seatId(performanceSeat.getSeat().getSeatId())
                .rowName(performanceSeat.getSeat().getRowName())
                .seatNumber(performanceSeat.getSeat().getSeatNumber())
                .sectionId(performanceSeat.getSeat().getSection().getSectionId())
                .sectionName(performanceSeat.getSeat().getSection().getName())
                .lockedByUserId(performanceSeat.getLockedByUser() != null ?
                        performanceSeat.getLockedByUser().getUserId() : null)
                .build();
    }

    @JsonIgnore  // JSON 직렬화에서 제외
    public boolean isEmpty() {
        return "AVAILABLE".equals(this.status);
    }
}
