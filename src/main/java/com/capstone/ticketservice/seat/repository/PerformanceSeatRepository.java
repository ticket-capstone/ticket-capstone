package com.capstone.ticketservice.seat.repository;

import com.capstone.ticketservice.seat.model.PerformanceSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {

    List<PerformanceSeat> findByEventEventId(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ps FROM PerformanceSeat ps WHERE ps.performanceSeatId = :id")
    Optional<PerformanceSeat> findByIdWithPessimisticLock(@Param("id") Long id);

    @Query("SELECT ps FROM PerformanceSeat ps WHERE ps.lockUntil < :now AND ps.status = 'LOCKED'")
    List<PerformanceSeat> findExpiredLocks(@Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM PerformanceSeat WHERE seat_id = :seatId AND event_id = :eventId", nativeQuery = true)
    Optional<PerformanceSeat> findBySeatIdAndEventId(@Param("seatId") Long seatId, @Param("eventId") Long eventId);

    List<PerformanceSeat> findBySeatSeatId(Long seatId);
}
