package com.capstone.ticketservice.order.service;

import com.capstone.ticketservice.order.dto.OrderDto;
import com.capstone.ticketservice.order.dto.OrderItemDto;
import com.capstone.ticketservice.order.model.OrderItem;
import com.capstone.ticketservice.order.model.Orders;
import com.capstone.ticketservice.order.repository.OrderItemRepository;
import com.capstone.ticketservice.order.repository.OrderRepository;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.ticket.model.Ticket;
import com.capstone.ticketservice.ticket.repository.TicketRepository;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 주문 항목 생성 완료 이벤트 클래스
    public static class OrderItemCreatedEvent {
        private final Long orderItemId;

        public OrderItemCreatedEvent(Long orderItemId) {
            this.orderItemId = orderItemId;
        }

        public Long getOrderItemId() {
            return orderItemId;
        }
    }

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PerformanceSeatRepository performanceSeatRepository,
                        UserRepository userRepository,
                        TicketRepository ticketRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OrderDto createOrder(Long userId, Long performanceSeatId) {
        log.info("주문 생성 시작: userId={}, performanceSeatId={}", userId, performanceSeatId);
        try {
            // 1. 사용자 조회
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
            log.info("사용자 조회 성공: {}", user.getUserId());

            // 2. 공연 좌석 조회
            PerformanceSeat performanceSeat = performanceSeatRepository.findByIdWithPessimisticLock(performanceSeatId)
                    .orElseThrow(() -> new EntityNotFoundException("공연 좌석을 찾을 수 없습니다."));
            log.info("좌석 조회 성공: {}", performanceSeat.getPerformanceSeatId());

            // 3. 좌석 상태 확인 및 잠금
            String seatStatus = performanceSeat.getStatus();
            if (!"LOCKED".equals(seatStatus) && !"AVAILABLE".equals(seatStatus)) {
                throw new IllegalStateException("선택한 좌석은 현재 예약할 수 없습니다. 상태: " + seatStatus);
            }

            if ("AVAILABLE".equals(seatStatus)) {
                performanceSeat.lock(300, user);
                performanceSeat = performanceSeatRepository.save(performanceSeat);
            }

            // 4. 주문 생성 (빌더 사용)
            Orders order = Orders.builder()
                    .user(user)
                    .totalAmount(Long.valueOf(performanceSeat.getPrice()))
                    .paymentStatus("PENDING")
                    .orderStatus("CREATED")
                    .orderDate(LocalDateTime.now())
                    .build();

            // 5. 주문 항목 생성 (빌더 사용)
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .performanceSeat(performanceSeat)
                    .price(Long.valueOf(performanceSeat.getPrice()))
                    .status("PENDING")
                    .build();

            // 주문에 주문 항목 추가
            order.addOrderItem(orderItem);

            // 주문 저장
            Orders savedOrder = orderRepository.save(order);
            log.info("주문 저장 완료: orderId={}", savedOrder.getOrderId());

            // DTO 반환 - fromEntity 사용
            return OrderDto.fromEntity(savedOrder);
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 주문 항목 생성 완료 이벤트를 수신하여 티켓 생성 처리
     * 이 메소드는 현재 트랜잭션이 커밋된 후에 실행됨
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderItemCreated(OrderItemCreatedEvent event) {
        log.info("주문 항목 생성 이벤트 수신: orderItemId={}", event.getOrderItemId());

        try {
            // 티켓이 이미 존재하는지 확인
            if (ticketRepository.existsByOrderItemOrderItemId(event.getOrderItemId())) {
                log.info("이미 티켓이 존재합니다: orderItemId={}", event.getOrderItemId());
                return;
            }

            // 주문 항목 조회 - 새 트랜잭션이므로 데이터베이스에서 다시 조회
            OrderItem orderItem = orderItemRepository.findById(event.getOrderItemId())
                    .orElseThrow(() -> new EntityNotFoundException("주문 항목을 찾을 수 없습니다: " + event.getOrderItemId()));

            // 티켓 생성
            Ticket ticket = new Ticket();
            ticket.setOrderItem(orderItem);
            ticket.setAccessCode(generateAccessCode());
            ticket.setStatus("ISSUED");
            ticket.setIssuedAt(LocalDateTime.now());
            ticket.setCreatedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            Ticket savedTicket = ticketRepository.save(ticket);
            log.info("티켓 생성 및 저장 완료: ticketId={}", savedTicket.getTicketId());
        } catch (Exception e) {
            log.error("티켓 생성 중 오류 발생: {}", e.getMessage(), e);
            // 이벤트 처리 실패는 상위 트랜잭션에 영향을 주지 않음
        }
    }

    // 랜덤 액세스 코드 생성 (12자리)
    private Long generateAccessCode() {
        Random random = new Random();
        long min = 100000000000L; // 12자리 최소값
        long max = 999999999999L; // 12자리 최대값
        return min + ((long)(random.nextDouble() * (max - min)));
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(()-> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // 지연 로딩된 관계 명시적 초기화
        Hibernate.initialize(order.getOrderItems());
        for (OrderItem item : order.getOrderItems()) {
            if (item.getPerformanceSeat() != null) {
                Hibernate.initialize(item.getPerformanceSeat());
                if (item.getPerformanceSeat().getSeat() != null) {
                    Hibernate.initialize(item.getPerformanceSeat().getSeat());
                    if (item.getPerformanceSeat().getSeat().getSection() != null) {
                        Hibernate.initialize(item.getPerformanceSeat().getSeat().getSection());
                    }
                }
                if (item.getPerformanceSeat().getEvent() != null) {
                    Hibernate.initialize(item.getPerformanceSeat().getEvent());
                }
            }
        }

        // 초기화된 엔티티로 DTO 생성 - fromEntity 사용
        try {
            return OrderDto.fromEntity(order);
        } catch (Exception e) {
            log.error("주문 DTO 변환 중 오류 발생: {}", e.getMessage(), e);
            // 기본 정보만 포함된 DTO 생성 - 대체 방안
            OrderDto fallbackDto = new OrderDto();
            fallbackDto.setOrderId(order.getOrderId());
            fallbackDto.setTotalAmount(order.getTotalAmount());
            fallbackDto.setPaymentStatus(order.getPaymentStatus());
            fallbackDto.setOrderStatus(order.getOrderStatus());
            fallbackDto.setOrderDate(order.getOrderDate());
            fallbackDto.setPaymentMethod(order.getPaymentMethod());
            fallbackDto.setUserId(order.getUser().getUserId());
            fallbackDto.setUserName(order.getUser().getName());

            // 주문 항목은 안전하게 변환
            List<OrderItemDto> items = order.getOrderItems().stream()
                    .map(item -> {
                        try {
                            return OrderItemDto.fromEntity(item);
                        } catch (Exception ex) {
                            log.warn("주문 항목 변환 중 오류: {}", ex.getMessage());
                            return OrderItemDto.builder()
                                    .orderItemId(item.getOrderItemId())
                                    .price(item.getPrice())
                                    .status(item.getStatus())
                                    .build();
                        }
                    })
                    .collect(Collectors.toList());
            fallbackDto.setOrderItems(items);

            return fallbackDto;
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        List<Orders> orders = orderRepository.findByUserUserId(userId);

        return orders.stream()
                .map(order -> {
                    try {
                        // 주문 항목 조회 및 초기화 (N+1 문제 방지)
                        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(order.getOrderId());
                        order.setOrderItems(orderItems);

                        // DTO 변환
                        return OrderDto.fromEntity(order);
                    } catch (Exception e) {
                        log.warn("주문 DTO 변환 중 오류: {}", e.getMessage());
                        // 기본 정보만 포함된 DTO 생성
                        OrderDto fallbackDto = new OrderDto();
                        fallbackDto.setOrderId(order.getOrderId());
                        fallbackDto.setTotalAmount(order.getTotalAmount());
                        fallbackDto.setPaymentStatus(order.getPaymentStatus());
                        fallbackDto.setOrderStatus(order.getOrderStatus());
                        fallbackDto.setOrderDate(order.getOrderDate());
                        fallbackDto.setPaymentMethod(order.getPaymentMethod());
                        fallbackDto.setUserId(order.getUser().getUserId());
                        fallbackDto.setUserName(order.getUser().getName());

                        // 주문 항목 변환
                        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(order.getOrderId());
                        fallbackDto.setOrderItems(orderItems.stream()
                                .map(item -> {
                                    try {
                                        return OrderItemDto.fromEntity(item);
                                    } catch (Exception ex) {
                                        return OrderItemDto.builder()
                                                .orderItemId(item.getOrderItemId())
                                                .price(item.getPrice())
                                                .status(item.getStatus())
                                                .build();
                                    }
                                })
                                .collect(Collectors.toList()));

                        return fallbackDto;
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto completePayment(Long orderId, String paymentMethod) {
        try {
            log.info("결제 처리 시작: orderId={}, paymentMethod={}", orderId, paymentMethod);

            // 1. 주문 정보 조회 및 업데이트
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

            // 결제 상태 업데이트
            order.setPaymentStatus("COMPLETED");
            order.setOrderStatus("COMPLETED");
            order.setPaymentMethod(paymentMethod);
            order.setUpdatedAt(LocalDateTime.now());

            Orders savedOrder = orderRepository.save(order);
            log.info("주문 결제 상태 업데이트 완료: orderId={}, status={}",
                    savedOrder.getOrderId(), savedOrder.getPaymentStatus());

            // 2. 주문 항목 및 좌석 상태 업데이트
            List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
            log.info("업데이트할 주문 항목 개수: {}", orderItems.size());

            for (OrderItem orderItem : orderItems) {
                // 주문 항목 상태 업데이트
                orderItem.setStatus("COMPLETED");
                orderItem.setUpdatedAt(LocalDateTime.now());
                OrderItem savedItem = orderItemRepository.save(orderItem);

                // 좌석 상태 업데이트
                PerformanceSeat seat = orderItem.getPerformanceSeat();
                seat.setStatus("SOLD");
                seat.setLockUntil(null);
                seat.setUpdatedAt(LocalDateTime.now());
                performanceSeatRepository.save(seat);

                log.info("주문 항목 및 좌석 상태 업데이트 완료: orderItemId={}, seatId={}",
                        savedItem.getOrderItemId(), seat.getPerformanceSeatId());

                // 티켓이 없으면 이벤트 발행
                if (!ticketRepository.existsByOrderItemOrderItemId(savedItem.getOrderItemId())) {
                    eventPublisher.publishEvent(new OrderItemCreatedEvent(savedItem.getOrderItemId()));
                    log.info("티켓 생성을 위한 이벤트 발행: orderItemId={}", savedItem.getOrderItemId());
                }
            }

            // 3. 최종 주문 정보 조회 및 반환
            return getOrderById(orderId);
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        try {
            Orders order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

            // 결제 전인 경우에만 취소 가능
            if ("COMPLETED".equals(order.getPaymentStatus())) {
                throw new IllegalStateException("이미 결제가 완료된 주문은 취소할 수 없습니다.");
            }

            // 주문 상태 업데이트
            order.setOrderStatus("CANCELLED");
            order.setPaymentStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("주문 취소 처리 완료: orderId={}", orderId);

            // 좌석 상태 업데이트
            List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
            for (OrderItem orderItem : orderItems) {
                orderItem.setStatus("CANCELLED");
                orderItem.setUpdatedAt(LocalDateTime.now());
                orderItemRepository.save(orderItem);

                // 좌석 상태 업데이트 (LOCKED -> AVAILABLE)
                PerformanceSeat seat = orderItem.getPerformanceSeat();
                seat.setStatus("AVAILABLE");
                seat.setLockUntil(null);
                seat.setUpdatedAt(LocalDateTime.now());
                performanceSeatRepository.save(seat);

                log.info("좌석 상태 복원 완료: seatId={}", seat.getPerformanceSeatId());
            }
        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}