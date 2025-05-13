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
public class TicketDto {

    private Long ticketId;
    private Long accessCode;
    private String status; // ISSUED, USED, CANCELLED 등의 상태값
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 주문 항목 관련 정보
    private Long orderItemId;
    private Long orderId;

    // 이벤트 정보
    private Long eventId;
    private String eventName;
    private LocalDateTime eventStartDate;

    // 좌석 정보
    private String sectionName;
    private String rowName;
    private String seatNumber;

    // 사용자 정보
    private Long userId;
    private String userName;

    // QR코드/바코드 생성을 위한 정보
    private String barcodeImageUrl;

    // 추가 정보
    private String venueName;
    private String venueAddress;
    private String ticketHolderName;
    private String specialInstructions;

    // 티켓 가격 정보
    private Long price;

    // 정책 정보
    private boolean isRefundable;
    private String refundPolicy;

    // 출력용 포맷팅 메소드들
    public String getFormattedEventDate() {
        return eventStartDate != null ?
                eventStartDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) :
                "";
    }

    public String getFormattedSeatInfo() {
        return sectionName + " 구역 " + rowName + "행 " + seatNumber + "번";
    }

    // 티켓 상태에 따른 메소드
    public boolean isUsed() {
        return "USED".equals(status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equals(status);
    }

    public boolean isValid() {
        return "ISSUED".equals(status);
    }

    public boolean isExpired() {
        return eventStartDate != null && eventStartDate.isBefore(LocalDateTime.now());
    }
}