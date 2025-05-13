package com.capstone.ticketservice.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponseDto {

    private Long ticketId;
    private Long accessCode;
    private String status;
    private String eventName;
    private LocalDateTime eventStartDate;
    private String seatInfo;
    private String barcodeImageUrl;

    // 티켓 정보를 간략히 표시하기 위한 필드
    private String ticketSummary;

    // 메시지 및 오류 처리를 위한 필드
    private boolean success;
    private String message;

    // TicketDto에서 변환하는 정적 메소드
    public static TicketResponseDto fromTicketDto(TicketDto ticketDto) {
        return TicketResponseDto.builder()
                .ticketId(ticketDto.getTicketId())
                .accessCode(ticketDto.getAccessCode())
                .status(ticketDto.getStatus())
                .eventName(ticketDto.getEventName())
                .eventStartDate(ticketDto.getEventStartDate())
                .seatInfo(ticketDto.getFormattedSeatInfo())
                .barcodeImageUrl(ticketDto.getBarcodeImageUrl())
                .ticketSummary(ticketDto.getEventName() + " - " + ticketDto.getFormattedEventDate())
                .success(true)
                .build();
    }
}