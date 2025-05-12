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
        return OrderItemDto.builder()
                .orderItemId(orderItem.getOrderItemId())
                .price(orderItem.getPrice())
                .status(orderItem.getStatus())
                .performanceSeatId(orderItem.getPerformanceSeat().getPerformanceSeatId())
                .sectionName(orderItem.getPerformanceSeat().getSeat().getSection().getName())
                .rowName(orderItem.getPerformanceSeat().getSeat().getRowName())
                .seatNumber(orderItem.getPerformanceSeat().getSeat().getSeatNumber())
                .eventId(orderItem.getPerformanceSeat().getEvent().getEventId())
                .eventName(orderItem.getPerformanceSeat().getEvent().getName())
                .build();
    }
}