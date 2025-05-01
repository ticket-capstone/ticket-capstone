package com.capstone.ticketservice.order.dto;

import com.capstone.ticketservice.order.model.Orders;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long orderId;
    private Long totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private LocalDateTime orderDate;
    private String paymentMethod;
    private Long userId;
    private String userName;
    private List<OrderItemDto> orderItems;

    public static OrderDto fromEntity(Orders order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .paymentMethod(order.getPaymentMethod())
                .userId(order.getUser().getUserId())
                .userName(order.getUser().getName())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
