package com.capstone.ticketservice.order.repository;

import com.capstone.ticketservice.order.model.OrderItem;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 주문 ID로 주문 아이템 목록 조회
    List<OrderItem> findByOrderOrderId(Long orderId);
    List<OrderItem> findByPerformanceSeatPerformanceSeatId(Long performanceSeatId);
}