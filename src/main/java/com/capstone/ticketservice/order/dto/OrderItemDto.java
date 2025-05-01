package com.capstone.ticketservice.order.dto;

import com.capstone.ticketservice.order.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        return OrderItemDto.builder()
                .orderItemId(orderItem.getOrderItemId())
                .price(orderItem.getPrice())
                .status(orderItem.getStatus())
                .performanceSeatId(orderItem.getPerformanceSeat() != null ?
                        orderItem.getPerformanceSeat().getPerformanceSeatId() : null)
                .sectionName(orderItem.getPerformanceSeat() != null &&
                        orderItem.getPerformanceSeat().getSeat() != null &&
                        orderItem.getPerformanceSeat().getSeat().getSection() != null ?
                        orderItem.getPerformanceSeat().getSeat().getSection().getName() : null)
                .rowName(orderItem.getPerformanceSeat() != null &&
                        orderItem.getPerformanceSeat().getSeat() != null ?
                        orderItem.getPerformanceSeat().getSeat().getRowName() : null)
                .seatNumber(orderItem.getPerformanceSeat() != null &&
                        orderItem.getPerformanceSeat().getSeat() != null ?
                        orderItem.getPerformanceSeat().getSeat().getSeatNumber() : null)
                .eventId(orderItem.getPerformanceSeat() != null &&
                        orderItem.getPerformanceSeat().getEvent() != null ?
                        orderItem.getPerformanceSeat().getEvent().getEventId() : null)
                .eventName(orderItem.getPerformanceSeat() != null &&
                        orderItem.getPerformanceSeat().getEvent() != null ?
                        orderItem.getPerformanceSeat().getEvent().getName() : null)
                .build();
    }
}
