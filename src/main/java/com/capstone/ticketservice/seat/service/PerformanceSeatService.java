package com.capstone.ticketservice.seat.service;

import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.user.model.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

        // 좌석 상태가 LOCKED이고 잠금 시간이 만료되었으면 자동으로 잠금 해제
        if ("LOCKED".equals(performanceSeat.getStatus()) &&
                performanceSeat.getLockUntil() != null &&
                performanceSeat.getLockUntil().isBefore(LocalDateTime.now())) {

            performanceSeat.unlock();
            performanceSeat = performanceSeatRepository.save(performanceSeat);
        }

        return PerformanceSeatDto.fromEntity(performanceSeat);
    }

    @Transactional
    public PerformanceSeatDto lockSeat(Long performanceSeatId, int lockTimeInSeconds, Users user) {
        // 비관적 락을 사용하여 동시성 문제 해결
        PerformanceSeat performanceSeat = performanceSeatRepository.findByIdWithPessimisticLock(performanceSeatId)
                .orElseThrow(() -> new RuntimeException("공연 좌석을 찾을 수 없습니다."));

        if (performanceSeat.isLocked()) {
            throw new RuntimeException("이미 잠긴 좌석입니다.");
        }

        performanceSeat.lock(lockTimeInSeconds, user);
        PerformanceSeat savedSeat = performanceSeatRepository.save(performanceSeat);

        return PerformanceSeatDto.fromEntity(savedSeat);
    }

    // 스케줄러를 사용하여 만료된 락을 자동으로 해제
    @Scheduled(fixedRate = 30000    )
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

    @Transactional(readOnly = true)
    public PerformanceSeatDto getPerformanceSeatBySeatIdAndEventId(Long seatId, Long eventId) {
        PerformanceSeat performanceSeat = performanceSeatRepository.findBySeatIdAndEventId(seatId, eventId)
                .orElseThrow(() -> new RuntimeException("해당 이벤트의 좌석을 찾을 수 없습니다."));
        return PerformanceSeatDto.fromEntity(performanceSeat);
    }

    @Transactional(readOnly = true)
    public List<PerformanceSeatDto> getPerformanceSeatsBySeatId(Long seatId) {
        List<PerformanceSeat> performanceSeats = performanceSeatRepository.findBySeatSeatId(seatId);
        return performanceSeats.stream()
                .map(PerformanceSeatDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 필터링되고 페이지화된 결과를 위한 새 메소드
    @Transactional(readOnly = true)
    public Page<PerformanceSeatDto> getFilteredPerformanceSeats(
            Long eventId, Long sectionId, String status,
            Integer minPrice, Integer maxPrice, Pageable pageable) {

        return performanceSeatRepository.findFilteredSeats(
                        eventId, sectionId, status, minPrice, maxPrice, pageable)
                .map(PerformanceSeatDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public boolean validateSeatLock(Long performanceId, Long userId) {
        return performanceSeatRepository.findById(performanceId)
                .map(seat -> {
                    if (!seat.isLocked()) {
                        return false; // 좌석이 잠겨있지 않음
                    }

                    // 잠근 사용자가 없거나 현재 사용자와 일치하지 않음
                    if (seat.getLockedByUser() ==  null || !seat.getLockedByUser().getUserId().equals(userId)) {
                        return false;
                    }

                    return true;
                })
                .orElse(false);
    }

    //seatid로 performaceSeatDto 얻는 함수
    @Transactional(readOnly = true)
    public List<PerformanceSeatDto> getPerformanceSeatId(Long seatId) {
        List<PerformanceSeat> performanceSeats = performanceSeatRepository.findBySeatSeatId(seatId);
        List<PerformanceSeatDto> performanceSeatDtos = new ArrayList<>();
        for(PerformanceSeat seat : performanceSeats) {
            performanceSeatDtos.add(PerformanceSeatDto.fromEntity(seat));
        }
        return performanceSeatDtos;
    }
}
