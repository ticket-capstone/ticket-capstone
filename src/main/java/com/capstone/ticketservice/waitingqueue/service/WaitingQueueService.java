package com.capstone.ticketservice.waitingqueue.service;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import com.capstone.ticketservice.waitingqueue.repository.WaitingQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.capstone.ticketservice.waitingqueue.model.WaitingQueue.QueueStatus.*;

@Service
public class    WaitingQueueService {

    private static final int MAX_PROCESSING_USERS = 10;
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
                countWaitingByEventIdAndQueueId(eventId, WAITING,queueId);
    }


    /**
     * 이벤트 ID와 WAITING status로 가장 앞선 대기자 1명 조회
     */
    public Optional<WaitingQueue> getFirstWaitingUser(Long eventId) {
        return waitingQueueRepository.findEarliestByEventIdAndStatus(eventId, WAITING);
    }

    /**
     * 가장 앞 대기자를 PROCESSING 상태로 업데이트
     */
    @Transactional
    public boolean processFirstWaitingUser(Long eventId) {
        Optional<WaitingQueue> optional = getFirstWaitingUser(eventId);
        if (optional.isPresent()) {
            WaitingQueue queue = optional.get();
            queue.setStatus(PROCESSING);
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
            if (queue.getStatus() == PROCESSING) {
                queue.setStatus(COMPLETED);
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

    public int getUserPosition(Long eventId, Long userId) {
        String queueKey = "event:" + eventId + ":queue";
        List<String> queue = redisTemplate.opsForList().range(queueKey, 0, -1);

        if (queue == null || queue.isEmpty()) {
            return -1; // 큐에 아무도 없음
        }else{
            int index = queue.indexOf(userId.toString());
            return index >= 0 ? index + 1 : -1; // 존재하지 않으면 -1
        }

    }

    /**
     * db만 활용한 큐
     */
    @Transactional
    public WaitingQueue DBjoinQueueIfAbsent(Long eventId, Users user) {
        Long userId = user.getUserId();

        // 1. 이미 DB에 존재하면 반환
        Optional<WaitingQueue> optionalQueue = waitingQueueRepository.findByEventEventIdAndUserUserId(eventId, userId);
        if (optionalQueue.isPresent()) return optionalQueue.get();

        // 2. 현재 PROCESSING 인원 수 확인
        int processingCount = waitingQueueRepository.countByEventEventIdAndStatus(eventId, WaitingQueue.QueueStatus.PROCESSING);

        // 3. 상태 결정
        WaitingQueue.QueueStatus status = processingCount < MAX_PROCESSING_USERS
                ? WaitingQueue.QueueStatus.PROCESSING
                : WaitingQueue.QueueStatus.WAITING;

        // 4. 대기열 등록
        WaitingQueue newQueue = WaitingQueue.builder()
                .user(user)
                .event(Event.builder().eventId(eventId).build())
                .status(status)
                .entryTime(LocalDateTime.now())
                .processingStartedAt(status == WaitingQueue.QueueStatus.PROCESSING ? LocalDateTime.now() : null)
                .build();

        return waitingQueueRepository.save(newQueue);
    }


    /**
     * Redis와 DB를 모두 확인하고 없을 경우 등록
     */
    @Transactional
    public WaitingQueue joinQueueIfAbsent(Long eventId, Users user) {
        Long userId = user.getUserId();
        String queueKey = getQueueKey(eventId);
        String statusKey = getStatusKey(eventId, userId);

        // 1. 캐시에 상태가 있으면 → 캐시 정보 반환
        String cachedStatus = redisTemplate.opsForValue().get(statusKey);
        if (cachedStatus != null) {
            return waitingQueueRepository.findByUserUserId(userId).orElseThrow();
        }

        // 2. DB에 있는지 확인
        Optional<WaitingQueue> optionalQueue = waitingQueueRepository.findByUserUserId(userId);
        if (optionalQueue.isPresent()) {
            WaitingQueue queue = optionalQueue.get();
            // Redis에도 다시 캐싱
            redisTemplate.opsForList().rightPush(queueKey, userId.toString());
            redisTemplate.opsForValue().set(statusKey, queue.getStatus().name(), Duration.ofMinutes(30));
            return queue;
        }

        // 3. Redis로 processingCount
        String processingCountKey = "processingCount:" + eventId;
        long processingCount = 0L;
        String num = redisTemplate.opsForValue().get(processingCountKey);
        if (num != null) processingCount = Long.parseLong(num);

        // 4. 상태 결정
        WaitingQueue.QueueStatus status = processingCount < MAX_PROCESSING_USERS
                ? WaitingQueue.QueueStatus.PROCESSING
                : WaitingQueue.QueueStatus.WAITING;

        // 5. Redis 등록
        redisTemplate.opsForList().rightPush(queueKey, userId.toString());
        redisTemplate.opsForValue().set(statusKey, status.name(), Duration.ofMinutes(30));

        if (status == WaitingQueue.QueueStatus.PROCESSING) {
            redisTemplate.opsForValue().increment(processingCountKey);
        }

        // 6. DB 저장
        WaitingQueue newQueue = WaitingQueue.builder()
                .user(user)
                .event(Event.builder().eventId(eventId).build())
                .status(status)
                .entryTime(LocalDateTime.now())
                .processingStartedAt(status == WaitingQueue.QueueStatus.PROCESSING ? LocalDateTime.now() : null)
                .build();

        return waitingQueueRepository.save(newQueue);
    }


    public void processNextInQueue(Long eventId) {
        int processingCount = waitingQueueRepository.countByEventEventIdAndStatus(eventId, PROCESSING);
        if (processingCount >= MAX_PROCESSING_USERS) return;

        Optional<WaitingQueue> next = waitingQueueRepository.findEarliestByEventIdAndStatus(eventId, WAITING);
        next.ifPresent(queue -> {
            queue.setStatus(PROCESSING);
            queue.setProcessingStartedAt(LocalDateTime.now());
            queue.setUpdatedAt(LocalDateTime.now());
            waitingQueueRepository.save(queue);

            // Redis 상태도 갱신
            redisTemplate.opsForValue().set(getStatusKey(eventId, queue.getUser().getUserId()), "PROCESSING");
        });
    }

    // cache의 부담을 덜기위한 cache warming
    public void warmUpStatusCache(long eventId) {
        List<WaitingQueue> waitingList = waitingQueueRepository.findAllByEventEventIdAndStatus(eventId , WaitingQueue.QueueStatus.WAITING);
        for (WaitingQueue queue : waitingList) {
            Long userId = queue.getUser().getUserId();
            redisTemplate.opsForValue().set(getStatusKey(eventId, userId), "WAITING");
        }
    }

    //queue 삭제
    public void resetQueueOrder(Long eventId) {
        String queueKey = getQueueKey(eventId);
        redisTemplate.delete(queueKey);
    }

    // queue에서 첫번째 user가 complete일 경우 cache 삭제
    public void removeFirstUserIfCompleted(Long eventId) {
        String queueKey = getQueueKey(eventId);
        String firstUserId = redisTemplate.opsForList().index(queueKey, 0);

        if (firstUserId == null) {
            return;
        }

        Long userId = Long.parseLong(firstUserId);
        Optional<WaitingQueue> optionalQueue = waitingQueueRepository.findByEventEventIdAndUserUserId(eventId, userId);

        if (optionalQueue.isPresent() && optionalQueue.get().getStatus() == COMPLETED) {
            String statusKey = getStatusKey(eventId, userId);
            redisTemplate.opsForList().remove(queueKey, 0, userId.toString());
            redisTemplate.delete(statusKey);
        }
    }



}

