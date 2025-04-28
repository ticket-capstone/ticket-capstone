package com.capstone.ticketservice.seat.service;

import com.capstone.ticketservice.seat.dto.SeatDto;
import com.capstone.ticketservice.seat.model.Seat;
import com.capstone.ticketservice.seat.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    @Transactional
    public List<SeatDto> getSeatBySectionId(Long sectionId) {
        List<Seat> seats = seatRepository.findBySectionSectionId(sectionId);
        return seats.stream()
                .map(SeatDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SeatDto getSeatById(Long seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다."));
        return SeatDto.fromEntity(seat);
    }
}
