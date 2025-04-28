package com.capstone.ticketservice.seat.dto;

import com.capstone.ticketservice.seat.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {

    private Long seatId;
    private String rowName;
    private String seatNumber;
    private String status;
    private Long sectionId;
    private String sectionName;

    public static SeatDto fromEntity(Seat seat) {
        return SeatDto.builder()
                .seatId(seat.getSeatId())
                .rowName(seat.getRowName())
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .sectionId(seat.getSection().getSectionId())
                .sectionName(seat.getSection().getName())
                .build();
    }
}
