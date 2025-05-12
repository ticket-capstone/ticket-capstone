package com.capstone.ticketservice.seat.repository;

import com.capstone.ticketservice.seat.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySectionSectionId(Long sectionId);
    @Query("SELECT s FROM Seat s WHERE s.section.name = :sectionName " +
            "AND s.rowName = :rowName " +
            "AND s.seatNumber = :seatNumber")
    Optional<Seat> findBySeatDetail(@Param("sectionName") String sectionName,
                                @Param("rowName") String rowName,
                                @Param("seatNumber") String seatNumber);

}
