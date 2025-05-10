package com.capstone.ticketservice.order.dto;

import com.capstone.ticketservice.order.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long orderItemId;
    private Long price;
    private String status;
    private Long performanceSeatId;
    private String sectionName;
    private String rowName;
    private String seatNumber;
    private Long eventId;
    private String eventName;

    public static OrderItemDto fromEntity(OrderItem orderItem) {
        // OrderItem이 null인 경우 처리
        if (orderItem == null) {
            return null;
        }

        OrderItemDto.OrderItemDtoBuilder builder = OrderItemDto.builder()
                .orderItemId(orderItem.getOrderItemId())
                .price(orderItem.getPrice())
                .status(orderItem.getStatus());

        try {
            // PerformanceSeat이 초기화되었는지 확인
            if (orderItem.getPerformanceSeat() != null &&
                    Hibernate.isInitialized(orderItem.getPerformanceSeat())) {

                builder.performanceSeatId(orderItem.getPerformanceSeat().getPerformanceSeatId());

                // Seat이 초기화되었는지 확인
                if (orderItem.getPerformanceSeat().getSeat() != null &&
                        Hibernate.isInitialized(orderItem.getPerformanceSeat().getSeat())) {

                    builder.rowName(orderItem.getPerformanceSeat().getSeat().getRowName())
                            .seatNumber(orderItem.getPerformanceSeat().getSeat().getSeatNumber());

                    // Section이 초기화되었는지 확인
                    if (orderItem.getPerformanceSeat().getSeat().getSection() != null &&
                            Hibernate.isInitialized(orderItem.getPerformanceSeat().getSeat().getSection())) {

                        builder.sectionName(orderItem.getPerformanceSeat().getSeat().getSection().getName());
                    }
                }

                // Event이 초기화되었는지 확인
                if (orderItem.getPerformanceSeat().getEvent() != null &&
                        Hibernate.isInitialized(orderItem.getPerformanceSeat().getEvent())) {

                    builder.eventId(orderItem.getPerformanceSeat().getEvent().getEventId())
                            .eventName(orderItem.getPerformanceSeat().getEvent().getName());
                }
            }
        } catch (Exception e) {
            // 에러 로깅 (실제 로거 사용 권장)
            System.err.println("OrderItemDto 변환 중 오류 발생: " + e.getMessage());
        }

        return builder.build();
    }
}