package com.capstone.ticketservice.seat.controller;

import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
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
    public String getPerformanceSeatsByEventId(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long sectionId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priceRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        try {
            // 가격 범위 파싱 (제공된 경우)
            Integer minPrice = null;
            Integer maxPrice = null;
            if (priceRange != null && !priceRange.isEmpty()) {
                String[] prices = priceRange.split("-");
                if (prices.length == 2) {
                    minPrice = Integer.parseInt(prices[0]);
                    maxPrice = Integer.parseInt(prices[1]);
                }
            }

            // 검색 기록을 로깅합니다
            System.out.println("검색 매개변수: eventId=" + eventId +
                    ", sectionId=" + sectionId +
                    ", status=" + status +
                    ", minPrice=" + minPrice +
                    ", maxPrice=" + maxPrice +
                    ", page=" + page);

            // 상태가 null이거나 비어있으면 빈 문자열로 설정하여 모든 상태를 가져옵니다
            if (status == null) {
                status = "";  // 이 빈 문자열은 위의 리포지토리 쿼리에서 처리됩니다
            }

            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size);

            // 필터링되고 페이지화된 결과 가져오기
            Page<PerformanceSeatDto> seatsPage = performanceSeatService.getFilteredPerformanceSeats(
                    eventId, sectionId, status, minPrice, maxPrice, pageable);

            // 모델에 속성 추가
            model.addAttribute("seats", seatsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", seatsPage.getTotalPages());
            model.addAttribute("totalItems", seatsPage.getTotalElements());
            model.addAttribute("eventId", eventId);

            // 필터 파라미터도 모델에 추가하여 페이지 간 이동 시 유지되도록 함
            model.addAttribute("sectionId", sectionId);
            model.addAttribute("status", status.isEmpty() ? null : status);  // 템플릿에서 빈 문자열을 null로 처리
            model.addAttribute("priceRange", priceRange);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "좌석 목록을 가져오는 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("seats", new ArrayList<>());
            model.addAttribute("totalPages", 1);
            model.addAttribute("currentPage", 0);
        }

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
