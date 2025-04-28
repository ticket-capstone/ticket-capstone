package com.capstone.ticketservice.seat.service;

import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceSeatService {

    private final PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    public PerformanceSeatService(PerformanceSeatRepository performanceSeatRepository) {
        this.performanceSeatRepository = performanceSeatRepository;
    }

    @Transactional(readOnly = true)
    public List<PerformanceSeatDto> getPerformanceSeatsByEventId(Long eventId) {
        List<PerformanceSeat> performanceSeats = performanceSeatRepository.findByEventEventId(eventId);
        return performanceSeats.stream()
                .map(PerformanceSeatDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PerformanceSeatDto getPerformanceSeatById(Long performanceSeatId) {
        PerformanceSeat performanceSeat = performanceSeatRepository.findById(performanceSeatId)
                .orElseThrow(() -> new RuntimeException("공연 좌석을 찾을 수 없습니다."));
        return PerformanceSeatDto.fromEntity(performanceSeat);
    }

    @Transactional
    public PerformanceSeatDto lockSeat(Long performanceSeatId, int lockTimeInSeconds) {
        // 비관적 락을 사용하여 동시성 문제 해결
        PerformanceSeat performanceSeat = performanceSeatRepository.findByIdWithPessimisticLock(performanceSeatId)
                .orElseThrow(() -> new RuntimeException("공연 좌석을 찾을 수 없습니다."));

        if (performanceSeat.isLocked()) {
            throw new RuntimeException("이미 잠긴 좌석입니다.");
        }

        performanceSeat.lock(lockTimeInSeconds);
        PerformanceSeat savedSeat = performanceSeatRepository.save(performanceSeat);

        return PerformanceSeatDto.fromEntity(savedSeat);
    }

    // 스케줄러를 사용하여 만료된 락을 자동으로 해제
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        List<PerformanceSeat> expiredLocks = performanceSeatRepository.findExpiredLocks(now);

        for (PerformanceSeat seat : expiredLocks) {
            seat.unlock();
        }

        if (!expiredLocks.isEmpty()) {
            performanceSeatRepository.saveAll(expiredLocks);
        }
    }

}
