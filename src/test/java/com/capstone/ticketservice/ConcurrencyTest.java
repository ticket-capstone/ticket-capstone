// src/test/java/com/capstone/ticketservice/ConcurrencyTest.java

package com.capstone.ticketservice;

import com.capstone.ticketservice.order.dto.OrderDto;
import com.capstone.ticketservice.order.service.OrderService;
import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(ConcurrencyTest.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private PerformanceSeatService performanceSeatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;

    private List<Users> testUsers;
    private Long testPerformanceSeatId = 1L; // 테스트할 좌석 ID

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 조회
        testUsers = new ArrayList<>();

        // 모든 사용자 조회해서 추가
        List<Users> allUsers = userRepository.findAll();

        testUsers.addAll(allUsers);

        // 테스트할 좌석이 AVAILABLE 상태인지 확인
        try {
            PerformanceSeatDto seat = performanceSeatService.getPerformanceSeatById(testPerformanceSeatId);
            log.info("테스트 좌석 상태: {}", seat.getStatus());

            // 좌석이 AVAILABLE이 아니면 강제로 AVAILABLE로 변경
            if (!"AVAILABLE".equals(seat.getStatus())) {
                log.warn("테스트 좌석이 AVAILABLE 상태가 아닙니다. 상태를 변경합니다.");
                seat.setStatus("AVAILABLE");
            }
        } catch (Exception e) {
            log.error("테스트 좌석 조회 실패: {}", e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후 좌석 상태 원복
        try {
            PerformanceSeat seat = performanceSeatRepository.findById(testPerformanceSeatId)
                    .orElse(null);
            if (seat != null) {
                seat.setStatus("AVAILABLE");
                seat.setLockedByUser(null);
                seat.setLockUntil(null);
                performanceSeatRepository.save(seat);
            }
        } catch (Exception e) {
            log.error("테스트 후 정리 실패: {}", e.getMessage());
        }
    }

    @Test
    public void 동시_좌석_잠금_테스트() throws InterruptedException {
        // Given - 실제 사용자 플로우에 맞는 테스트: 좌석 클릭 시 잠금
        if (testUsers.isEmpty()) {
            log.error("테스트 사용자가 없습니다!");
            return;
        }

        int threadCount = Math.min(100, testUsers.size());
        log.info("실제 테스트할 스레드 수: {}", threadCount);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount); // threadCount = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        CyclicBarrier barrier = new CyclicBarrier(threadCount);

        // 결과 수집을 위한 동기화된 컬렉션들
        List<PerformanceSeatDto> successfulLocks = Collections.synchronizedList(new ArrayList<>());
        List<String> failureReasons = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger lockSuccessCount = new AtomicInteger(0);
        AtomicInteger lockFailureCount = new AtomicInteger(0);

        // 성능 측정
        long startTime = System.currentTimeMillis();

        // When - 100명이 동시에 같은 좌석 잠금 시도 (좌석 클릭 시뮬레이션)
        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i % testUsers.size();

            executor.submit(() -> {
                try {
                    barrier.await();
                    Users user = testUsers.get(userIndex);

                    try {
                        // 좌석 잠금 시도 (5분간)
                        PerformanceSeatDto lockedSeat = performanceSeatService.lockSeat(
                                testPerformanceSeatId, 300, user);

                        if (lockedSeat != null && "LOCKED".equals(lockedSeat.getStatus())) {
                            successfulLocks.add(lockedSeat);
                            lockSuccessCount.incrementAndGet();
                            log.info("좌석 잠금 성공 - 사용자: {}, 잠금시간: {}",
                                    user.getUserId(), lockedSeat.getLockUntil());
                        }

                    } catch (Exception e) {
                        failureReasons.add(e.getMessage());
                        lockFailureCount.incrementAndGet();
                        log.debug("좌석 잠금 실패 - 사용자: {}, 이유: {}", user.getUserId(), e.getMessage());
                    }

                } catch (Exception e) {
                    log.error("배리어 대기 중 오류: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 완료 대기 (최대 30초)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        // Then - 결과 검증 및 분석
        log.info("=== 동시 좌석 잠금 테스트 결과 ===");
        log.info("실행 시간: {}ms", duration);
        log.info("잠금 성공 수: {}", lockSuccessCount.get());
        log.info("잠금 실패 수: {}", lockFailureCount.get());
        log.info("전체 시도 수: {}", threadCount);
        log.info("잠금 성공률: {}%", (double) lockSuccessCount.get() / threadCount * 100);

        // 실패 이유 분석
        log.info("=== 잠금 실패 이유 분석 ===");
        failureReasons.stream()
                .distinct()
                .forEach(reason -> {
                    long count = failureReasons.stream().filter(r -> r.equals(reason)).count();
                    log.info("실패 이유: {} ({}번)", reason, count);
                });

        // 검증 - 동시성 제어가 올바르게 작동했는지 확인
        assertThat(completed).isTrue();
        assertThat(lockSuccessCount.get()).isEqualTo(1); // 정확히 1명만 잠금 성공해야 함
        assertThat(lockFailureCount.get()).isEqualTo(threadCount - 1); // 나머지는 모두 실패해야 함
        assertThat(successfulLocks).hasSize(1);

        // 성공한 잠금 정보 확인
        if (!successfulLocks.isEmpty()) {
            PerformanceSeatDto lockedSeat = successfulLocks.get(0);
            log.info("잠금 성공 정보: 좌석ID={}, 잠금사용자ID={}, 잠금해제시간={}",
                    lockedSeat.getPerformanceSeatId(),
                    lockedSeat.getLockedByUserId(),
                    lockedSeat.getLockUntil());
        }
    }

    @Test
    public void 좌석_잠금_타임아웃_테스트() throws InterruptedException {
        // Given - 좌석 잠금 후 타임아웃 테스트
        if (testUsers.size() < 2) {
            log.error("테스트에 필요한 최소 사용자 수가 부족합니다. 현재: {}명, 필요: 2명", testUsers.size());
            return; // 테스트 스킵
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger lockSuccessCount = new AtomicInteger(0);

        Users user1 = testUsers.get(0);
        Users user2 = testUsers.get(1);

        log.info("테스트 시작 - 사용자1: {}, 사용자2: {}", user1.getUserId(), user2.getUserId());

        // When - 첫 번째 사용자가 좌석 잠금
        executor.submit(() -> {
            try {
                PerformanceSeatDto lockedSeat = performanceSeatService.lockSeat(testPerformanceSeatId, 5, user1); // 5초 잠금
                if (lockedSeat != null) {
                    lockSuccessCount.incrementAndGet();
                    log.info("사용자1이 좌석 잠금 성공: {}", lockedSeat.getStatus());

                    // 잠금 상태에서 주문 시도
                    try {
                        Thread.sleep(2000); // 2초 대기
                        OrderDto order = orderService.createOrder(user1.getUserId(), testPerformanceSeatId);
                        log.info("사용자1 주문 성공: {}", order.getOrderId());
                    } catch (Exception e) {
                        log.info("사용자1 주문 실패: {}", e.getMessage());
                    }
                }
            } catch (Exception e) {
                log.info("사용자1 잠금 실패: {}", e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        // 두 번째 사용자가 같은 좌석 잠금 시도 (1초 후)
        executor.submit(() -> {
            try {
                Thread.sleep(1000); // 1초 후 시도
                PerformanceSeatDto lockedSeat = performanceSeatService.lockSeat(testPerformanceSeatId, 5, user2);
                if (lockedSeat != null) {
                    lockSuccessCount.incrementAndGet();
                    log.info("사용자2가 좌석 잠금 성공: {}", lockedSeat.getStatus());
                }
            } catch (Exception e) {
                log.info("사용자2 잠금 실패 (예상됨): {}", e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        log.info("=== 좌석 잠금 타임아웃 테스트 결과 ===");
        log.info("잠금 성공 횟수: {}", lockSuccessCount.get());

        assertThat(completed).isTrue();
        assertThat(lockSuccessCount.get()).isEqualTo(1); // 하나의 잠금만 성공해야 함
    }

    @Test
    public void 성능_벤치마크_테스트() throws InterruptedException {
        // Given - 성능 측정을 위한 대량 동시 요청 테스트
        int[] threadCounts = {10, 50, 100, 200}; // 다양한 동시 접속자 수로 테스트

        for (int threadCount : threadCounts) {
            log.info("=== {}명 동시 접속 테스트 시작 ===", threadCount);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);
            List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());

            long startTime = System.currentTimeMillis();

            // When - 동시 요청 실행
            for (int i = 0; i < threadCount; i++) {
                final int userIndex = i;

                executor.submit(() -> {
                    long requestStart = System.currentTimeMillis();

                    try {
                        if (userIndex < testUsers.size()) {
                            Users user = testUsers.get(userIndex);
                            // 여러 좌석 중 랜덤 선택 (성공 가능성 높이기)
                            Long seatId = (long) ((userIndex % 10) + 1); // 1~10번 좌석 중 선택

                            OrderDto order = orderService.createOrder(user.getUserId(), seatId);
                            if (order != null) {
                                successCount.incrementAndGet();
                            }
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        long responseTime = System.currentTimeMillis() - requestStart;
                        responseTimes.add(responseTime);
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(60, TimeUnit.SECONDS);
            long totalDuration = System.currentTimeMillis() - startTime;
            executor.shutdown();

            // Then - 성능 분석
            if (completed && !responseTimes.isEmpty()) {
                double avgResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                long maxResponseTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
                long minResponseTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
                double tps = (double) threadCount / (totalDuration / 1000.0);

                log.info("동시 접속자: {}명", threadCount);
                log.info("총 실행 시간: {}ms", totalDuration);
                log.info("성공: {}건, 실패: {}건", successCount.get(), failureCount.get());
                log.info("평균 응답시간: {:.2f}ms", avgResponseTime);
                log.info("최대 응답시간: {}ms", maxResponseTime);
                log.info("최소 응답시간: {}ms", minResponseTime);
                log.info("TPS (초당 처리량): {:.2f}", tps);
                log.info("성공률: {:.2f}%", (double) successCount.get() / threadCount * 100);
                log.info("==========================================");
            }
        }
    }
}