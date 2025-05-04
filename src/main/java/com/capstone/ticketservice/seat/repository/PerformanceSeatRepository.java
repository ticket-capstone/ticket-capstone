package com.capstone.ticketservice.seat.repository;

import com.capstone.ticketservice.seat.model.PerformanceSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("SELECT ps FROM PerformanceSeat ps WHERE ps.performanceSeatId = :performanceSeatId")
    Optional<PerformanceSeat> findByIdWithPessimisticLock(@Param("performanceSeatId") Long performanceSeatId);

    @Query("SELECT ps FROM PerformanceSeat ps WHERE ps.lockUntil < :now AND ps.status = 'LOCKED'")
    List<PerformanceSeat> findExpiredLocks(@Param("now") LocalDateTime now);

    @Query(value = "SELECT * FROM PerformanceSeat WHERE seat_id = :seatId AND event_id = :eventId", nativeQuery = true)
    Optional<PerformanceSeat> findBySeatIdAndEventId(@Param("seatId") Long seatId, @Param("eventId") Long eventId);

    List<PerformanceSeat> findBySeatSeatId(Long seatId);

    // 페이지네이션과 함께 필터링하는 새 메소드
    @Query("SELECT ps FROM PerformanceSeat ps " +
            "WHERE ps.event.eventId = :eventId " +
            "AND (:sectionId IS NULL OR ps.seat.section.sectionId = :sectionId) " +
            "AND (:status IS NULL OR :status = '' OR ps.status = :status) " + // 빈 문자열도 처리
            "AND (:minPrice IS NULL OR ps.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR ps.price <= :maxPrice)")
    Page<PerformanceSeat> findFilteredSeats(
            @Param("eventId") Long eventId,
            @Param("sectionId") Long sectionId,
            @Param("status") String status,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable);
}
