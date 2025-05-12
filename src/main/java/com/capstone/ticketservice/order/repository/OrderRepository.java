package com.capstone.ticketservice.order.repository;

import com.capstone.ticketservice.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    @Query("SELECT o FROM Orders o WHERE o.user.userId = :userId")
    List<Orders> findByUserId(Long userId);
}