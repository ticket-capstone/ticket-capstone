package com.capstone.ticketservice.order.service;

import com.capstone.ticketservice.order.dto.OrderDto;
import com.capstone.ticketservice.order.dto.OrderItemDto;
import com.capstone.ticketservice.order.model.OrderItem;
import com.capstone.ticketservice.order.model.Orders;
import com.capstone.ticketservice.order.repository.OrderItemRepository;
import com.capstone.ticketservice.order.repository.OrderRepository;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.ticket.dto.TicketDto;
import com.capstone.ticketservice.ticket.service.TicketService;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final UserRepository userRepository;
    private final TicketService ticketService;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PerformanceSeatRepository performanceSeatRepository,
                        UserRepository userRepository,
                        TicketService ticketService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.userRepository = userRepository;
        this.ticketService = ticketService;
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // OrderItem 목록 명시적으로 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);

        // 기본 OrderDto 생성
        OrderDto orderDto = OrderDto.fromEntity(order);

        // OrderItem 정보 명시적 설정
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemDto> orderItemDtos = orderItems.stream()
                    .map(OrderItemDto::fromEntity)
                    .collect(Collectors.toList());
            orderDto.setOrderItems(orderItemDtos);
        } else {
            // 비어있는 리스트로 초기화 (null 방지)
            orderDto.setOrderItems(new ArrayList<>());
        }

        return orderDto;
    }

    public List<OrderItemDto> getOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
        return orderItems.stream()
                .map(OrderItemDto::fromEntity)
                .collect(Collectors.toList());
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

            // 주문 저장
            Orders savedOrder = orderRepository.save(order);
            log.info("주문 저장 완료: orderId={}", savedOrder.getOrderId());

            // 5. 주문 상세 생성
            OrderItem orderItem = OrderItem.builder()
                    .price(savedOrder.getTotalAmount())
                    .status("CREATED")
                    .order(savedOrder)
                    .performanceSeat(performanceSeat)
                    .build();

            // 주문 상세 저장
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            log.info("주문 상세 저장 완료: orderItemId={}", savedOrderItem.getOrderItemId());

            // 6. OrderDto 생성 (orderItems 포함)
            OrderDto orderDto = OrderDto.fromEntity(savedOrder);

            // 7. OrderDto에 orderItems 설정
            List<OrderItemDto> orderItemDtos = new ArrayList<>();
            orderItemDtos.add(OrderItemDto.fromEntity(savedOrderItem));
            orderDto.setOrderItems(orderItemDtos);

            return orderDto;
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
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
            }

            OrderDto orderDto = OrderDto.fromEntity(order);

            orderDto.setOrderItems(orderItems.stream()
                    .map(OrderItemDto::fromEntity)
                    .collect(Collectors.toList()));

            // 3. 최종 주문 정보 조회 및 반환
            return orderDto;
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

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        List<Orders> orders = orderRepository.findByUserId(userId);
        List<OrderDto> orderDtos = new ArrayList<>();

        for (Orders order : orders) {
            // 각 주문에 대한 OrderDto 생성
            OrderDto orderDto = OrderDto.fromEntity(order);

            // 각 주문의 OrderItem을 명시적으로 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(order.getOrderId());

            // OrderItem을 OrderItemDto로 변환하여 설정
            List<OrderItemDto> orderItemDtos = orderItems.stream()
                    .map(item -> {
                        OrderItemDto itemDto = OrderItemDto.fromEntity(item);
                        // 디버깅 로그 추가
                        log.debug("Converting OrderItem: id={}, price={}, seatInfo={}, eventName={}",
                                item.getOrderItemId(),
                                item.getPrice(),
                                (item.getPerformanceSeat() != null && item.getPerformanceSeat().getSeat() != null) ?
                                        item.getPerformanceSeat().getSeat().getRowName() + item.getPerformanceSeat().getSeat().getSeatNumber() : "null",
                                (item.getPerformanceSeat() != null && item.getPerformanceSeat().getEvent() != null) ?
                                        item.getPerformanceSeat().getEvent().getName() : "null");
                        return itemDto;
                    })
                    .collect(Collectors.toList());

            orderDto.setOrderItems(orderItemDtos);
            orderDtos.add(orderDto);
        }

        return orderDtos;
    }
}