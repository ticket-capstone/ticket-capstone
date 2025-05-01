package com.capstone.ticketservice.order.controller;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.service.EventService;
import com.capstone.ticketservice.order.dto.OrderDto;
import com.capstone.ticketservice.order.dto.OrderItemDto;
import com.capstone.ticketservice.order.model.OrderItem;
import com.capstone.ticketservice.order.model.Orders;
import com.capstone.ticketservice.order.repository.OrderItemRepository;
import com.capstone.ticketservice.order.repository.OrderRepository;
import com.capstone.ticketservice.order.service.OrderService;
import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    private final PerformanceSeatService performanceSeatService;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final EventService eventService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;

    @Autowired
    public OrderController(
            PerformanceSeatService performanceSeatService,
            PerformanceSeatRepository performanceSeatRepository,
            EventService eventService,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderService orderService,
            UserRepository userRepository) {
        this.performanceSeatService = performanceSeatService;
        this.performanceSeatRepository = performanceSeatRepository;
        this.eventService = eventService;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    /**
     * 잠금된 좌석으로 주문 생성 페이지
     */
    @GetMapping("/create/{seatId}")
    public String createOrderForm(@PathVariable Long seatId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. 좌석 정보 확인
            PerformanceSeatDto seat = performanceSeatService.getPerformanceSeatById(seatId);

            // 3. 좌석이 잠금 상태인지 확인
            if (!"LOCKED".equals(seat.getStatus()) && !"AVAILABLE".equals(seat.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "선택한 좌석은 현재 예약할 수 없습니다. 다른 좌석을 선택해주세요.");
                return "redirect:/api/events/" + seat.getEventId() + "/sections";
            }

            // 4. 이벤트 정보 조회
            Event event = eventService.getEventById(seat.getEventId());

            // 5. 모델에 정보 추가
            model.addAttribute("seat", seat);
            model.addAttribute("event", event);
            model.addAttribute("user", user);

            // 결제 정보 계산
            int totalAmount = seat.getPrice();
            model.addAttribute("totalAmount", totalAmount);

            return "orders/create";
        } catch (Exception e) {
            log.error("주문 생성 페이지 로딩 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 생성 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/events";
        }
    }

    /**
     * 주문 생성 처리
     */
    @PostMapping("/create")
    public String processOrder(@RequestParam("performanceSeatId") Long performanceSeatId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. 좌석이 예약 가능한지 확인
            PerformanceSeatDto seatDto = performanceSeatService.getPerformanceSeatById(performanceSeatId);

            if (!"AVAILABLE".equals(seatDto.getStatus()) && !"LOCKED".equals(seatDto.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "선택한 좌석은 현재 예약할 수 없습니다. 다른 좌석을 선택해주세요.");
                return "redirect:/api/events/" + seatDto.getEventId() + "/sections";
            }

            // 3. 좌석이 AVAILABLE 상태라면 LOCKED로 변경
            if ("AVAILABLE".equals(seatDto.getStatus())) {
                try {
                    seatDto = performanceSeatService.lockSeat(performanceSeatId, 300);
                    log.info("좌석 상태를 LOCKED로 변경했습니다: {}", performanceSeatId);
                } catch (Exception e) {
                    log.error("좌석 잠금 중 오류 발생", e);
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "좌석 잠금 중 오류가 발생했습니다: " + e.getMessage());
                    return "redirect:/api/orders/create/" + performanceSeatId;
                }
            }

            // 4. 주문 생성 (OrderService 사용)
            try {
                OrderDto orderDto = orderService.createOrder(user.getUserId(), performanceSeatId);
                if (orderDto == null) {
                    throw new RuntimeException("주문 생성에 실패했습니다.");
                }

                // 5. 결제 페이지로 리다이렉트
                return "redirect:/api/orders/" + orderDto.getOrderId() + "/payment";
            } catch (Exception e) {
                log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
                redirectAttributes.addFlashAttribute("errorMessage",
                        "주문 생성 중 오류가 발생했습니다: " + e.getMessage());

                // 좌석을 다시 AVAILABLE 상태로 변경 시도
                try {
                    PerformanceSeat seat = performanceSeatRepository.findById(performanceSeatId).orElse(null);
                    if (seat != null && "LOCKED".equals(seat.getStatus())) {
                        seat.setStatus("AVAILABLE");
                        seat.setLockUntil(null);
                        performanceSeatRepository.save(seat);
                        log.info("좌석 상태를 다시 AVAILABLE로 변경했습니다: {}", performanceSeatId);
                    }
                } catch (Exception ex) {
                    log.error("좌석 상태 복원 중 오류 발생", ex);
                }

                return "redirect:/api/orders/create/" + performanceSeatId;
            }
        } catch (Exception e) {
            log.error("주문 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/orders/create/" + performanceSeatId;
        }
    }

    /**
     * 결제 페이지 - OrderDto 사용하여 데이터 전달 방식 개선
     */
    @GetMapping("/{orderId}/payment")
    public String paymentPage(@PathVariable Long orderId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. OrderService를 통해 OrderDto 객체 조회
            OrderDto orderDto = orderService.getOrderById(orderId);

            // 3. 주문한 사용자와 현재 로그인한 사용자가 동일한지 확인
            if (!orderDto.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/";
            }

            // 4. 모델에 OrderDto 객체 추가 (Thymeleaf 템플릿에서 사용)
            model.addAttribute("order", orderDto);

            // 결제 페이지 반환
            return "orders/payment";
        } catch (Exception e) {
            log.error("결제 페이지 로딩 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 결제 처리
     */
    @PostMapping("/{orderId}/payment")
    @Transactional
    public String processPayment(@PathVariable Long orderId,
                                 @RequestParam String paymentMethod,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. OrderService를 통해 결제 처리
            OrderDto updatedOrder = orderService.completePayment(orderId, paymentMethod);

            // 3. 주문한 사용자와 현재 로그인한 사용자가 동일한지 확인
            if (!updatedOrder.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/";
            }

            // 4. 세션에서 선택한 좌석 정보 제거
            session.removeAttribute("selectedSeat");

            // 5. 성공 메시지 설정
            redirectAttributes.addFlashAttribute("successMessage", "결제가 성공적으로 완료되었습니다.");

            // 6. 완료 페이지로 리다이렉트
            return "redirect:/api/orders/" + orderId + "/complete";
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/orders/" + orderId + "/payment";
        }
    }

    /**
     * 주문 취소
     */
    @GetMapping("/{orderId}/cancel")
    @Transactional
    public String cancelOrder(@PathVariable Long orderId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. 주문 정보 조회 (OrderDto 사용)
            OrderDto orderDto = orderService.getOrderById(orderId);

            // 3. 주문한 사용자와 현재 로그인한 사용자가 동일한지 확인
            if (!orderDto.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/";
            }

            // 4. 결제 완료된 주문은 취소할 수 없음
            if ("COMPLETED".equals(orderDto.getPaymentStatus())) {
                redirectAttributes.addFlashAttribute("errorMessage", "이미 결제가 완료된 주문은 취소할 수 없습니다.");
                return "redirect:/api/orders/" + orderId;
            }

            // 5. OrderService를 통해 주문 취소
            orderService.cancelOrder(orderId);

            // 6. 세션에서 선택한 좌석 정보 제거
            session.removeAttribute("selectedSeat");

            // 7. 성공 메시지 설정
            redirectAttributes.addFlashAttribute("successMessage", "주문이 성공적으로 취소되었습니다.");

            return "redirect:/api/orders/my-orders";
        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 취소 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/orders/" + orderId;
        }
    }

    /**
     * 주문 완료 페이지 - OrderDto 사용
     */
    @GetMapping("/{orderId}/complete")
    public String orderComplete(@PathVariable Long orderId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. OrderService를 통해 OrderDto 객체 조회
            OrderDto orderDto = orderService.getOrderById(orderId);

            // 3. 주문한 유저와 현재 로그인한 유저가 일치하는지 확인
            if (!orderDto.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/";
            }

            // 4. 모델에 정보 추가
            model.addAttribute("order", orderDto);
            model.addAttribute("user", user);

            return "orders/complete";
        } catch (Exception e) {
            log.error("주문 완료 페이지 로딩 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 내 주문 목록 - OrderDto 활용
     */
    @GetMapping("/my-orders")
    public String getMyOrders(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. OrderService를 통해 사용자의 주문 목록 조회
            List<OrderDto> orders = orderService.getOrdersByUserId(user.getUserId());

            // 3. 모델에 정보 추가
            model.addAttribute("orders", orders);

            return "orders/my-orders";
        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 목록을 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * 주문 상세 조회 - OrderDto 활용
     */
    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        // 1. 로그인 확인
        Users user = (Users) session.getAttribute("user");
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요한 서비스입니다.");
            return "redirect:/api/sessions/login";
        }

        try {
            // 2. OrderService를 통해 OrderDto 객체 조회
            OrderDto orderDto = orderService.getOrderById(orderId);

            // 3. 주문한 사용자와 현재 로그인한 사용자가 동일한지 확인
            if (!orderDto.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "접근 권한이 없습니다.");
                return "redirect:/";
            }

            // 4. 모델에 정보 추가
            model.addAttribute("order", orderDto);

            return "orders/detail";
        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류 발생", e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 정보를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/api/orders/my-orders";
        }
    }
}