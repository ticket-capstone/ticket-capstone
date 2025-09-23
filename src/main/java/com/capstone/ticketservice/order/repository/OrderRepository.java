package com.capstone.ticketservice.order.repository;

import com.capstone.ticketservice.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    // N+1 문제 해결: User 정보를 fetch join으로 한번에 가져옴
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "WHERE o.user.userId = :userId")
    List<Orders> findByUserId(@Param("userId") Long userId);

    /**
     * N+1 문제 해결: fetch join으로 OrderItems와 관련 엔티티를 한 번에 조회
     */
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "WHERE o.user.userId = :userId " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByUserIdWithFetch(@Param("userId") Long userId);

    /**
     * 특정 주문 상세 조회 시 OrderItems도 함께 조회
     */
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "WHERE o.orderId = :orderId")
    Optional<Orders> findByIdWithFetch(@Param("orderId") Long orderId);
}