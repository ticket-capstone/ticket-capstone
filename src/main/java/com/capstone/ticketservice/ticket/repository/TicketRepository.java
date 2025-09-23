package com.capstone.ticketservice.ticket.repository;

import com.capstone.ticketservice.ticket.model.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /**
     * 사용자 ID와 상태로 티켓을 조회합니다. (JOIN FETCH로 N+1 해결)
     * 반환 타입을 List로 변경, 페이징은 Service에서 처리
     */
    @Query("SELECT DISTINCT t FROM Ticket t " +
            "JOIN FETCH t.orderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH o.user u " +
            "JOIN FETCH oi.performanceSeat ps " +
            "JOIN FETCH ps.event e " +
            "JOIN FETCH ps.seat s " +
            "JOIN FETCH s.section sec " +
            "WHERE u.userId = :userId " +
            "AND t.status = :status " +
            "ORDER BY t.issuedAt DESC")
    List<Ticket> findByUserIdAndStatus(@Param("userId") Long userId,
                                       @Param("status") String status);

    /**
     * 페이징을 위한 카운트 쿼리 추가
     */
    @Query("SELECT COUNT(t) FROM Ticket t " +
            "JOIN t.orderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.user.userId = :userId " +
            "AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * 액세스 코드로 티켓을 조회합니다.
     * @param accessCode 티켓의 고유 액세스 코드
     * @return 해당하는 티켓 (존재하지 않으면 빈 Optional)
     */
    Optional<Ticket> findByAccessCode(Long accessCode);

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

    /**
     * OrderItem ID로 티켓을 조회합니다.
     * 연관 엔티티도 함께 로딩하여 추가 쿼리를 방지합니다.
     * @param orderItemId OrderItem의 ID
     * @return 해당하는 티켓 (존재하지 않으면 빈 Optional)
     */
    @EntityGraph(attributePaths = {
            "orderItem",
            "orderItem.order",
            "orderItem.order.user",
            "orderItem.performanceSeat",
            "orderItem.performanceSeat.event",
            "orderItem.performanceSeat.seat",
            "orderItem.performanceSeat.seat.section"
    })
    Optional<Ticket> findByOrderItemOrderItemId(Long orderItemId);

    /**
     * 티켓 ID로 티켓을 조회합니다. (JOIN FETCH로 N+1 해결)
     * 모든 연관 엔티티를 한 번의 쿼리로 가져와서 N+1 문제를 방지합니다.
     * @param ticketId 티켓 ID
     * @return 해당하는 티켓 (존재하지 않으면 빈 Optional)
     */
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.orderItem oi " +
            "JOIN FETCH oi.order o " +
            "JOIN FETCH o.user u " +
            "JOIN FETCH oi.performanceSeat ps " +
            "JOIN FETCH ps.event e " +
            "JOIN FETCH ps.seat s " +
            "JOIN FETCH s.section sec " +
            "WHERE t.ticketId = :ticketId")
    Optional<Ticket> findByIdWithAllDetails(@Param("ticketId") Long ticketId);
}
