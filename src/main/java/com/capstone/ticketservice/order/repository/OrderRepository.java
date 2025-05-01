package com.capstone.ticketservice.order.repository;

import com.capstone.ticketservice.order.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByUserUserId(Long userId);
}
