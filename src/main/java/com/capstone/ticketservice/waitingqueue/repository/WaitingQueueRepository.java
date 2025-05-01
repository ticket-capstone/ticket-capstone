package com.capstone.ticketservice.waitingqueue.repository;

import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitingQueueRepository extends JpaRepository<WaitingQueue, Long> {

    //대기열 첫번째 사용자 찾기
    @Query("SELECT w FROM WaitingQueue w " +
            "WHERE w.event.eventId = :eventId AND w.status = :status " +
            "ORDER BY w.queueId ASC")
    Optional<WaitingQueue> findEarliestByEventIdAndStatus(@Param("eventId") Long eventId,
                                                          @Param("status") WaitingQueue.QueueStatus status);
    //사용자id로 찾기
    Optional<WaitingQueue> findByUserUserId(Long userId);

    //이벤트와 사용자 id로 찾기
    Optional<WaitingQueue> findByEventEventIdAndUserUserId(Long eventId, Long userId);

    //eventid로 WAITING 상태 수 찾기
    @Query("SELECT COUNT(w) FROM WaitingQueue w WHERE w.event.eventId = :eventId AND w.status = :status AND w.queueId < :position")
    long countWaitingByEventIdAndQueueId(@Param("eventId") Long eventId, @Param("status") WaitingQueue.QueueStatus status, @Param("queueId") Long queueId);


}
