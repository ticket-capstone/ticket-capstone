package com.capstone.ticketservice.seat.controller;

import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.dto.SeatDto;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import com.capstone.ticketservice.seat.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller
@RequestMapping("/api")
public class SeatController {

    private final SeatService seatService;
    private final PerformanceSeatService performanceSeatService;

    @Autowired
    public SeatController(SeatService seatService, PerformanceSeatService performanceSeatService) {
        this.seatService = seatService;
        this.performanceSeatService = performanceSeatService;
    }

    @GetMapping("/sections/{id}/seats")
    public String getSeatsBySectionId(@PathVariable("id") Long sectionId, Model model) {
        List<SeatDto> seats = seatService.getSeatBySectionId(sectionId);

        // 행별로 좌석 그룹화
        Map<String, List<SeatDto>> seatsByRow = new TreeMap<>();
        for (SeatDto seat : seats) {
            seatsByRow.computeIfAbsent(seat.getRowName(), k -> new ArrayList<>()).add(seat);
        }

        model.addAttribute("seats", seats);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("sectionId", sectionId);
        return "seat/list";
    }

    @GetMapping("/seats/{id}")
    public String getSeatById(@PathVariable("id") Long seatId,
                              @RequestParam(required = false) Long eventId, // URL에서 eventId 파라미터를 받음
                              Model model) {
        SeatDto seat = seatService.getSeatById(seatId);
        model.addAttribute("seat", seat);

        // eventId가 제공되었다면 모델에 추가
        if (eventId != null) {
            model.addAttribute("eventId", eventId);
        } else {
            // 기본값 설정 (테스트용)
            model.addAttribute("eventId", 1L);
        }

        return "seat/detail";
    }
}
