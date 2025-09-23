package com.capstone.ticketservice.order.repository;

import com.capstone.ticketservice.order.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
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

    /**
     * 여러 주문의 OrderItem들을 한 번에 조회 (N+1 문제 해결)
     */
    @Query("SELECT oi FROM OrderItem oi " +
            "LEFT JOIN FETCH oi.order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH oi.performanceSeat ps " +
            "LEFT JOIN FETCH ps.seat s " +
            "LEFT JOIN FETCH s.section sec " +
            "LEFT JOIN FETCH ps.event e " +
            "WHERE oi.order.orderId IN :orderIds")
    List<OrderItem> findByOrderIdsWithFetch(@Param("orderIds") List<Long> orderIds);
}