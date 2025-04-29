package com.capstone.ticketservice.booking.controller;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.service.EventService;
import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.dto.SeatDto;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import com.capstone.ticketservice.seat.service.SeatService;
import com.capstone.ticketservice.user.model.Users;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/api")
@Slf4j
public class BookingController {

    private final EventService eventService;
    private final SeatService seatService;
    private final PerformanceSeatService performanceSeatService;
    //todo: 대기열 구현 필요?

    @Autowired
    public BookingController(EventService eventService,
                             SeatService seatService,
                             PerformanceSeatService performanceSeatService
                             ) {
        this.eventService = eventService;
        this.seatService = seatService;
        this.performanceSeatService = performanceSeatService;
        //todo: 생성자 추가
    }

    /**
     * 예매 프로세스 시작 - 대기열 확인 후 적절한 페이지로 리다이렉트
     */
    //todo: 예매 프로세스 시작

    /**
     * 좌석 선택 페이지
     */
    @GetMapping("/events/{eventId}/booking/select-seat")
    public String selectSeat(@PathVariable Long eventId,
                             @RequestParam Long seatId,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        // 1. 현재 사용자 확인 (로그인 필요)
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }
        try {
            // 2. 이벤트 및 좌석 정보 조회
            Event event = eventService.getEventById(eventId);
            SeatDto seat = seatService.getSeatById(seatId);

            // 3. 공연 좌석 정보 조회 (가격 정보 포함)
            PerformanceSeatDto performanceSeat = performanceSeatService.getPerformanceSeatBySeatIdAndEventId(seatId, eventId);

            // 4. 좌석 상태 확인
            if (!"AVAILABLE".equals(performanceSeat.getStatus()) && !"LOCKED".equals(performanceSeat.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 예약된 좌석이거나 선택할 수 없는 좌석입니다.");
                return "redirect:/api/sections/" + seat.getSectionId() + "/seats";
            }

            // 5. 모델에 정보 추가
            model.addAttribute("event", event);
            model.addAttribute("seat", seat);
            model.addAttribute("performanceSeat", performanceSeat);

            return "booking/select-seat";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "좌석 선택 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/api/events/" + eventId + "/sections";
        }
    }

    /**
     * 좌석 선택 확인 및 잠금 처리
     */
    @PostMapping("/events/{eventId}/booking/confirm-seat")
    public String confirmSeat(@PathVariable Long eventId,
                              @RequestParam Long performanceSeatId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        //todo: 아직 좌석잠금 오류 해결해야함.

        // 1. 현재 사용자 확인 (로그인 필요)
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }
        try {
            // 2. 좌석 잠금 처리 (5분 동안)
            PerformanceSeatDto lockedSeat = performanceSeatService.lockSeat(performanceSeatId, 300);
            // 3. 세션에 선택한 좌석 정보 저장 (결제 단계에서 사용)
            session.setAttribute("selectedSeat", lockedSeat);

            // 4. 결제 페이지로 이동
            return "redirect:/api/events/" + eventId + "/booking/payment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "좌석 잠금 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/events/" + eventId + "/booking/select-seat?seatId=" + performanceSeatId;
        }
    }

    /**
     * 결제 페이지
     */
    // todo: 결제페이지 (@GetMapping)

    /**
     * 결제 처리
     */
    //todo:결제 처리페이지 (@PostMapping)

    /**
     * 예매 완료 페이지
     */
    //todo: 예매 완료페이지 (양동현 <- 할일) order 엔티티 구현이 된 후 구현예정
}