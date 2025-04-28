package com.capstone.ticketservice.seat.dto;

import com.capstone.ticketservice.seat.model.PerformanceSeat;
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
                .build();
    }
}
