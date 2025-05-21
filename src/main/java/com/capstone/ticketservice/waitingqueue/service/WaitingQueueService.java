package com.capstone.ticketservice.waitingqueue.service;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import com.capstone.ticketservice.waitingqueue.repository.WaitingQueueRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Service
public class    WaitingQueueService {

    private final WaitingQueueRepository waitingQueueRepository;
    @Qualifier("redisQueueTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public WaitingQueueService(
            WaitingQueueRepository waitingQueueRepository,
            @Qualifier("redisQueueTemplate") RedisTemplate<String, String> redisTemplate
    ) {
        this.waitingQueueRepository = waitingQueueRepository;
        this.redisTemplate = redisTemplate;
    }

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
        return waitingQueueRepository.
                countWaitingByEventIdAndQueueId(eventId, WaitingQueue.QueueStatus.WAITING,queueId);
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



    // redis cache 부분
    private String getQueueKey(Long eventId) {
        return "event:" + eventId + ":queue";
    }

    private String getStatusKey(Long eventId, Long userId) {
        return "event:" + eventId + ":status:" + userId;
    }

    public boolean isInQueue(Long eventId, Long userId) {
        String key = "event:" + eventId + ":queue";
        List<String> queue = redisTemplate.opsForList().range(key, 0, -1);
        return queue.contains(userId.toString());
    }

    public void addUserToQueue(Long eventId, Long userId) {
        String key = "event:" + eventId + ":queue";
        redisTemplate.opsForList().rightPush(key, userId.toString());
        redisTemplate.opsForValue().set("event:" + eventId + ":status:" + userId, "WAITING");
    }

    public void updateStatusToProcessing(Long eventId, Long userId) {
        redisTemplate.opsForValue().set("event:" + eventId + ":status:" + userId, "PROCESSING");

        Optional<WaitingQueue> optional = waitingQueueRepository.findByEventEventIdAndUserUserId(eventId, userId);
        optional.ifPresent(queue -> {
            queue.setStatus(WaitingQueue.QueueStatus.PROCESSING);
            queue.setProcessingStartedAt(LocalDateTime.now());
            waitingQueueRepository.save(queue);
        });
    }

    public void saveToDatabase(Long eventId, Users user, WaitingQueue.QueueStatus status) {
        WaitingQueue queue = WaitingQueue.builder()
                .user(user)
                .event(Event.builder().eventId(eventId).build())
                .status(status)
                .entryTime(LocalDateTime.now())
                .build();
        waitingQueueRepository.save(queue);
    }

    public int getUserPosition(Long eventId, Long userId) {
        String queueKey = "event:" + eventId + ":queue";
        List<String> queue = redisTemplate.opsForList().range(queueKey, 0, -1);

        if (queue == null || queue.isEmpty()) {
            return -1; // 큐에 아무도 없음
        }

        int index = queue.indexOf(userId.toString());
        return index >= 0 ? index + 1 : -1; // 존재하지 않으면 -1
    }


    /**
     * Redis와 DB를 모두 확인하고 없을 경우 등록
     */
    @Transactional
    public WaitingQueue joinQueueIfAbsent(Long eventId, Users user) {
        Long userId = user.getUserId();
        String queueKey = getQueueKey(eventId);

        // 1. Redis에 이미 존재하면 pass
        List<String> queue = redisTemplate.opsForList().range(queueKey, 0, -1);
        if (queue.contains(userId.toString())) {
            return waitingQueueRepository.findByUserUserId(userId).orElseThrow();
        }

        // 2. DB에도 없으면 새로 등록
        Optional<WaitingQueue> optionalQueue = waitingQueueRepository.findByUserUserId(userId);
        if (optionalQueue.isPresent()) {
            return optionalQueue.get();
        }

        boolean isQueueEmpty = waitingQueueRepository
                .findEarliestByEventIdAndStatus(eventId, WaitingQueue.QueueStatus.WAITING)
                .isEmpty();

        WaitingQueue.QueueStatus status = isQueueEmpty
                ? WaitingQueue.QueueStatus.PROCESSING
                : WaitingQueue.QueueStatus.WAITING;

        WaitingQueue newQueue = WaitingQueue.builder()
                .user(user)
                .event(Event.builder().eventId(eventId).build())
                .status(status)
                .entryTime(LocalDateTime.now())
                .processingStartedAt(isQueueEmpty ? LocalDateTime.now() : null)
                .build();

        WaitingQueue saved = waitingQueueRepository.save(newQueue);

        // 3. Redis에 추가
        if (status == WaitingQueue.QueueStatus.WAITING) {
            redisTemplate.opsForList().rightPush(queueKey, userId.toString());
            redisTemplate.opsForValue().set(getStatusKey(eventId, userId), "WAITING");
        }

        return saved;
    }





}

