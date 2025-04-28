package com.capstone.ticketservice.seat.repository;

import com.capstone.ticketservice.seat.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySectionSectionId(Long sectionId);
}
