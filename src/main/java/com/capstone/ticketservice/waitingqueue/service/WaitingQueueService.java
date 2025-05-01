package com.capstone.ticketservice.waitingqueue.service;

import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import com.capstone.ticketservice.waitingqueue.repository.WaitingQueueRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Service
@RequiredArgsConstructor
public class WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;

    /**
     * 대기열 등록
     */
    public WaitingQueue addToQueue(WaitingQueue queue) {
        return waitingQueueRepository.save(queue);
    }

    /**
     * 사용자 ID로 대기열 조회
     */
    public Optional<WaitingQueue> getByUserId(Long userId) {
        return waitingQueueRepository.findByUserUserId(userId);
    }

    /**
     * 사용자 앞의 대기열의 인원수
     */
    @Transactional
    public long getRemainingWaitingCount(Long eventId ,Long queueId) {
        return waitingQueueRepository.countWaitingByEventIdAndQueueId(eventId, WaitingQueue.QueueStatus.WAITING,queueId);
    }


    /**
     * 이벤트 ID와 WAITING status로 가장 앞선 대기자 1명 조회
     */
    public Optional<WaitingQueue> getFirstWaitingUser(Long eventId) {
        return waitingQueueRepository.findEarliestByEventIdAndStatus(eventId, WaitingQueue.QueueStatus.WAITING);
    }

    /**
     * 가장 앞 대기자를 PROCESSING 상태로 업데이트
     */
    @Transactional
    public boolean processFirstWaitingUser(Long eventId) {
        Optional<WaitingQueue> optional = getFirstWaitingUser(eventId);
        if (optional.isPresent()) {
            WaitingQueue queue = optional.get();
            queue.setStatus(WaitingQueue.QueueStatus.PROCESSING);
            queue.setProcessingStartedAt(LocalDateTime.now());
            return true;
        }
        return false;
    }

    /**
     * 사용자를 PROCESSING status이면 COMPLETED로 업데이트
     */
    @Transactional
    public boolean completeWaitingStatus(Long userId, Long eventId) {
        Optional<WaitingQueue> optional = waitingQueueRepository
                .findByEventEventIdAndUserUserId(eventId, userId);

        if (optional.isPresent()) {
            WaitingQueue queue = optional.get();
            if (queue.getStatus() == WaitingQueue.QueueStatus.PROCESSING) {
                queue.setStatus(WaitingQueue.QueueStatus.COMPLETED);
                queue.setUpdatedAt(LocalDateTime.now());
                return true;
            }
        }
        return false;
    }



}

