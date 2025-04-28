package com.capstone.ticketservice.seat.controller;

import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api")
public class PerformanceSeatController {

    private final PerformanceSeatService performanceSeatService;

    @Autowired
    public PerformanceSeatController(PerformanceSeatService performanceSeatService) {
        this.performanceSeatService = performanceSeatService;
    }

    @GetMapping("/events/{eventId}/seats")
    public String getPerformanceSeatsByEventId(@PathVariable Long eventId, Model model) {
        List<PerformanceSeatDto> seats = performanceSeatService.getPerformanceSeatsByEventId(eventId);
        model.addAttribute("seats", seats);
        model.addAttribute("eventId", eventId);
        return "seat/performance-list";
    }

    @GetMapping("/performance-seats/{id}")
    public String getPerformanceSeatById(@PathVariable("id") Long performanceSeatId, Model model) {
        PerformanceSeatDto seat = performanceSeatService.getPerformanceSeatById(performanceSeatId);
        model.addAttribute("seat", seat);
        return "seat/performance-detail";
    }

    @PostMapping("/performance-seats/{id}/lock")
    public String lockSeat(@PathVariable("id") Long performanceSeatId, RedirectAttributes redirectAttributes) {
        try {
            // 기본적으로 5분(300초) 동안 좌석 잠금
            PerformanceSeatDto seat = performanceSeatService.lockSeat(performanceSeatId, 300);
            redirectAttributes.addFlashAttribute("successMessage", "좌석이 성공적으로 잠겼습니다.");
            return "redirect:/api/performance-seats/" + performanceSeatId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "좌석 잠금에 실패했습니다: " + e.getMessage());
            return "redirect:/api/performance-seats/" + performanceSeatId;
        }
    }
}
