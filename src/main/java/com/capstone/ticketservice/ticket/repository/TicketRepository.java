package com.capstone.ticketservice.ticket.repository;

import com.capstone.ticketservice.ticket.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * 사용자 ID로 티켓을 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @return 사용자가 소유한 티켓 목록
     */
    @Query("SELECT t FROM Ticket t " +
            "JOIN t.orderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.userId = :userId " +
            "ORDER BY t.issuedAt DESC")
    List<Ticket> findByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID와 상태로 티켓을 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @param status 티켓 상태 (ISSUED, USED, CANCELLED 등)
     * @return 조건에 맞는 티켓 목록
     */
    @Query("SELECT t FROM Ticket t " +
            "JOIN t.orderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.userId = :userId " +
            "AND t.status = :status " +
            "ORDER BY t.issuedAt DESC")
    List<Ticket> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 사용자 ID와 상태로 티켓을 페이지네이션하여 조회합니다.
     * @param userId 조회할 사용자의 ID
     * @param status 티켓 상태 (ISSUED, USED, CANCELLED 등)
     * @param pageable 페이지네이션 정보
     * @return 조건에 맞는 티켓 페이지
     */
    @Query("SELECT t FROM Ticket t " +
            "JOIN t.orderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.userId = :userId " +
            "AND t.status = :status " +
            "ORDER BY t.issuedAt DESC")
    Page<Ticket> findByUserIdAndStatus(@Param("userId") Long userId,
                                       @Param("status") String status,
                                       Pageable pageable);

    /**
     * 액세스 코드로 티켓을 조회합니다.
     * @param accessCode 티켓의 고유 액세스 코드
     * @return 해당하는 티켓 (존재하지 않으면 빈 Optional)
     */
    Optional<Ticket> findByAccessCode(Long accessCode);

    /**
     * 주문 항목 ID로 티켓을 조회합니다.
     * @param orderItemId 주문 항목 ID
     * @return 해당하는 티켓 (존재하지 않으면 빈 Optional)
     */
    Optional<Ticket> findByOrderItemOrderItemId(Long orderItemId);
    boolean existsByOrderItemOrderItemId(Long orderItemId);

    /**
     * 이벤트 ID와 상태로 티켓을 조회합니다.
     * @param eventId 이벤트 ID
     * @param status 티켓 상태
     * @return 조건에 맞는 티켓 목록
     */
    @Query("SELECT t FROM Ticket t " +
            "JOIN t.orderItem oi " +
            "JOIN oi.performanceSeat ps " +
            "WHERE ps.event.eventId = :eventId " +
            "AND t.status = :status")
    List<Ticket> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);

    /**
     * 사용되지 않은 유효한 티켓 수를 계산합니다.
     * @param userId 사용자 ID
     * @return 유효한 티켓 수
     */
    @Query("SELECT COUNT(t) FROM Ticket t " +
            "JOIN t.orderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.userId = :userId " +
            "AND t.status = 'ISSUED'")
    long countValidTicketsByUserId(@Param("userId") Long userId);

}
