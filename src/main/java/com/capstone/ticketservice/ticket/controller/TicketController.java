package com.capstone.ticketservice.ticket.controller;

import com.capstone.ticketservice.ticket.dto.TicketDto;
import com.capstone.ticketservice.ticket.dto.TicketResponseDto;
import com.capstone.ticketservice.ticket.service.TicketService;
import com.capstone.ticketservice.user.model.Users;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;


@Controller
@RequestMapping("/api/ticket")
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * 사용자의 티켓 목록 페이지를 반환합니다.
     */
    @GetMapping("/my-tickets")
    public String getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 기본값으로 "ISSUED" 상태 설정
            if (status == null || status.isEmpty()) {
                status = "ISSUED";
            }

            // 페이지네이션을 위한 Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size, Sort.by("issuedAt").descending());

            // 티켓 목록 조회
            Page<TicketDto> ticketsPage = ticketService.getTicketsByUserIdAndStatus(user.getUserId(), status, pageable);

            // 모델에 데이터 추가
            model.addAttribute("tickets", ticketsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", ticketsPage.getTotalPages());
            model.addAttribute("totalItems", ticketsPage.getTotalElements());
            model.addAttribute("status", status);

            // 통계 정보 추가
            long validTicketsCount = ticketService.countValidTicketsByUserId(user.getUserId());
            model.addAttribute("validTicketsCount", validTicketsCount);

            return "ticket/list";
        } catch (Exception e) {
            log.error("티켓 목록 조회 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "티켓 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 특정 티켓의 상세 정보 페이지를 반환합니다.
     */
    @GetMapping("/{ticketId}")
    public String getTicketDetail(
            @PathVariable Long ticketId,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 티켓 정보 조회
            TicketDto ticket = ticketService.getTicketById(ticketId);

            // 티켓 소유자 확인
            if (!ticket.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/api/tickets/my-tickets";
            }

            // 모델에 티켓 정보 추가
            model.addAttribute("ticket", ticket);

            // 현재 시간 추가 (템플릿에서 만료 여부 확인용)
            model.addAttribute("currentDateTime", LocalDateTime.now());

            return "ticket/detail";
        } catch (Exception e) {
            log.error("티켓 상세 조회 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "티켓 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/ticket/my-tickets";
        }
    }

    /**
     * 티켓 취소를 처리합니다.
     */
    @PostMapping("/{ticketId}/cancel")
    public String cancelTicket(
            @PathVariable Long ticketId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 티켓 정보 조회
            TicketDto ticket = ticketService.getTicketById(ticketId);

            // 티켓 소유자 확인
            if (!ticket.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/api/ticket/my-tickets";
            }

            // 티켓 상태 확인
            if (!"ISSUED".equals(ticket.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "이미 취소되었거나 사용된 티켓입니다. 상태: " + ticket.getStatus());
                return "redirect:/api/ticket/" + ticketId;
            }

            /**
             * 환불 처리 가능 추가?
             */


            // 티켓 취소 처리
            ticketService.cancelTicket(ticketId);

            redirectAttributes.addFlashAttribute("successMessage", "티켓이 성공적으로 취소되었습니다.");
            return "redirect:/api/ticket/my-tickets";
        } catch (Exception e) {
            log.error("티켓 취소 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "티켓 취소 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/ticket/" + ticketId;
        }
    }

}