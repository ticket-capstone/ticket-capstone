package com.capstone.ticketservice.booking.controller;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.service.EventService;
import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.dto.SeatDto;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import com.capstone.ticketservice.seat.service.SeatService;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.waitingqueue.model.WaitingQueue;
import com.capstone.ticketservice.waitingqueue.service.WaitingQueueService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/api")
@Slf4j
public class BookingController {

    private final EventService eventService;
    private final SeatService seatService;
    private final PerformanceSeatService performanceSeatService;
    private final WaitingQueueService waitingQueueService;
    //todo: 대기열 구현 필요?

    @Autowired
    public BookingController(EventService eventService,
                             SeatService seatService,
                             PerformanceSeatService performanceSeatService,
                             WaitingQueueService waitingQueueService
                             ) {
        this.eventService = eventService;
        this.seatService = seatService;
        this.performanceSeatService = performanceSeatService;
        this.waitingQueueService =  waitingQueueService;
        //todo: 생성자 추가
    }

    /**
     * 예매 프로세스 시작 - 대기열 확인 후 적절한 페이지로 리다이렉트
     */
    /**
     * 예매 프로세스 시작 - 대기열 확인 후 분기 처리
     */

    /**
     * redis cache를 활용한 대기열 큐
     */

    @GetMapping("/events/{eventId}/booking/start")
    public String startsBooking(@PathVariable Long eventId,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            Long userId = user.getUserId();

            // Redis & DB 통합 대기열 등록/조회
            WaitingQueue queue = waitingQueueService.joinQueueIfAbsent(eventId, user);
            log.debug(">>> joinQueueIfAbsent 실행됨: userId = {}", user.getUserId());

            if (queue.getStatus() == WaitingQueue.QueueStatus.PROCESSING) {
                return "redirect:/api/events/" + eventId + "/sections";
            }

            // WAITING 상태 → Redis 큐에서 순번 확인
            int myPosition = waitingQueueService.getUserPosition(eventId, userId) + 1;

            model.addAttribute("myPosition", myPosition);
            model.addAttribute("eventId", eventId);

            return "/waitingQueue/waitingQueue"; // 대기열 상태 HTML 렌더링

        } catch (Exception e) {
            log.error("대기열 처리 중 예외 발생: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "대기열 처리 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }



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

            // 좌석이 잠긴 상태이면 현재 사용자가 잠금 소유자인지 확인
            if ("LOCKED".equals(performanceSeat.getStatus())) {
                if (performanceSeat.getLockedByUserId() == null ||
                        !performanceSeat.getLockedByUserId().equals(user.getUserId())) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "이 좌석은 다른 사용자에 의해 잠겨 있습니다. 다른 좌석을 선택해주세요.");
                    return "redirect:/api/sections/" + seat.getSectionId() + "/seats";
                }
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
            log.warn("⚠️ 세션에 user 없음. 로그인 필요");
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }
        try {
            // 2. 좌석 잠금 처리 (5분 동안)
            PerformanceSeatDto lockedSeat = performanceSeatService.lockSeat(performanceSeatId, 300, user);
            // 3. 세션에 선택한 좌석 정보 저장 (결제 단계에서 사용)
            session.setAttribute("selectedSeat", lockedSeat);

            // 좌석 상태 확인
            PerformanceSeatDto seatDto = performanceSeatService.getPerformanceSeatById(performanceSeatId);

            // 좌석이 이미 잠긴 상태이면 현재 사용자가 잠금 소유자인지 확인
            if ("LOCKED".equals(seatDto.getStatus())) {
                // 잠금 소유자 확인
                if (seatDto.getLockedByUserId() == null || !seatDto.getLockedByUserId().equals(user.getUserId())) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "이 좌석은 다른 사용자에 의해 잠겨 있습니다. 다른 좌석을 선택해주세요.");
                    return "redirect:/api/events/" + eventId + "/sections";
                }
            }


            // 4. 결제 페이지로 이동
            return "redirect:/api/orders/create/" + performanceSeatId;
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
    @PostMapping("/events/{eventId}/booking/complete")
    public String completeBooking(@PathVariable Long eventId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        Users user = (Users) session.getAttribute("user");

        try {
            // 주문/결제 로직 생략 (예: OrderService.createOrder(...))

            // 1. 현재 사용자를 COMPLETED로 재설정
            waitingQueueService.completeWaitingStatus(user.getUserId(), eventId);

            // 2. 다음 대기자 → PROCESSING으로 진입시킴
            waitingQueueService.processFirstWaitingUser(eventId);

            return "redirect:/api/events/" + eventId + "/booking/complete";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "예매 완료 처리 중 오류가 발생했습니다.");
            return "redirect:/";
        }
    }


    //todo: 예매 완료페이지 (양동현 <- 할일) order 엔티티 구현이 된 후 구현예정
}