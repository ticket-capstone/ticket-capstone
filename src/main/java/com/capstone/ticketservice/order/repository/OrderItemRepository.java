package com.capstone.ticketservice.order.repository;

import com.capstone.ticketservice.order.model.OrderItem;
import jakarta.persistence.LockModeType;
import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 주문 ID로 주문 아이템 목록 조회
    @Query("SELECT oi FROM OrderItem oi " +
            "LEFT JOIN FETCH oi.order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH oi.performanceSeat ps " +
            "LEFT JOIN FETCH ps.seat s " +
            "LEFT JOIN FETCH s.section " +
            "LEFT JOIN FETCH ps.event " +
            "WHERE oi.order.orderId = :orderId")
    List<OrderItem> findByOrderOrderId(@Param("orderId") Long orderId);
    List<OrderItem> findByPerformanceSeatPerformanceSeatId(Long performanceSeatId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderItemId = :id")
    Optional<OrderItem> findByIdWithLock(@Param("id") Long id);
}