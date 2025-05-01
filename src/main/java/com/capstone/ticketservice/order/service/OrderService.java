package com.capstone.ticketservice.order.service;

import com.capstone.ticketservice.order.dto.OrderDto;
import com.capstone.ticketservice.order.model.OrderItem;
import com.capstone.ticketservice.order.model.Orders;
import com.capstone.ticketservice.order.repository.OrderItemRepository;
import com.capstone.ticketservice.order.repository.OrderRepository;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.user.model.Users;
import com.capstone.ticketservice.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PerformanceSeatRepository performanceSeatRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OrderDto createOrder(Long userId, Long performanceSeatId) {
        log.info("주문 생성 시작: userId={}, performanceSeatId={}", userId, performanceSeatId);
        try {
            // 1. 사용자 조회
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
            log.info("사용자 조회 성공: {}", user.getUserId());

            // 2. 공연 좌석 조회
            PerformanceSeat performanceSeat = performanceSeatRepository.findById(performanceSeatId)
                    .orElseThrow(() -> new EntityNotFoundException("공연 좌석을 찾을 수 없습니다."));
            log.info("좌석 조회 성공: {}", performanceSeat.getPerformanceSeatId());

            // 3. 좌석이 예약 가능한지 확인 (AVAILABLE 또는 LOCKED 상태여야 함)
            String seatStatus = performanceSeat.getStatus();
            if (!"LOCKED".equals(seatStatus) && !"AVAILABLE".equals(seatStatus)) {
                throw new IllegalStateException("선택한 좌석은 현재 예약할 수 없습니다. 상태: " + seatStatus);
            }
            log.info("좌석 상태 확인 성공: {}", seatStatus);

            // 4. AVAILABLE 상태라면 LOCKED로 변경
            if ("AVAILABLE".equals(seatStatus)) {
                performanceSeat.lock(300); // 5분간 잠금
                performanceSeat = performanceSeatRepository.save(performanceSeat);
                log.info("좌석 잠금 처리 완료: {}", performanceSeat.getPerformanceSeatId());
            }

            // 5. 주문 엔티티 생성 및 저장
            Orders order = new Orders();
            order.setUser(user);
            order.setTotalAmount(Long.valueOf(performanceSeat.getPrice()));
            order.setPaymentStatus("PENDING");
            order.setOrderStatus("CREATED");
            order.setOrderDate(LocalDateTime.now());
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setOrderItems(new ArrayList<>()); // 빈 리스트로 초기화

            // 주문 저장
            log.info("주문 객체 생성 완료, 저장 시작");
            Orders savedOrder = orderRepository.save(order);
            log.info("주문 저장 완료: orderId={}", savedOrder.getOrderId());

            // 6. 주문 항목 생성 및 저장
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder); // 저장된 주문 엔티티 참조
            orderItem.setPerformanceSeat(performanceSeat);
            orderItem.setPrice(Long.valueOf(performanceSeat.getPrice()));
            orderItem.setStatus("PENDING");
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setUpdatedAt(LocalDateTime.now());

            log.info("주문 항목 객체 생성 완료, 저장 시작");
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);
            log.info("주문 항목 저장 완료: orderItemId={}", savedOrderItem.getOrderItemId());

            // 주문 항목 목록에 추가하지 않고 별도로 유지 - 고아 객체 문제 방지

            // 7. OrderDto 생성 시 별도로 주문 항목을 조회하여 설정
            OrderDto orderDto = new OrderDto();
            orderDto.setOrderId(savedOrder.getOrderId());
            orderDto.setTotalAmount(savedOrder.getTotalAmount());
            orderDto.setPaymentStatus(savedOrder.getPaymentStatus());
            orderDto.setOrderStatus(savedOrder.getOrderStatus());
            orderDto.setOrderDate(savedOrder.getOrderDate());
            orderDto.setPaymentMethod(savedOrder.getPaymentMethod());
            orderDto.setUserId(savedOrder.getUser().getUserId());
            orderDto.setUserName(savedOrder.getUser().getName());

            // 주문 항목 목록 별도 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(savedOrder.getOrderId());
            orderDto.setOrderItems(orderItems.stream()
                    .map(item -> {
                        return com.capstone.ticketservice.order.dto.OrderItemDto.fromEntity(item);
                    })
                    .collect(Collectors.toList()));

            log.info("주문 생성 완료, DTO 반환");
            return orderDto;
        } catch (Exception e) {
            log.error("주문 생성 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(()-> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // 주문 항목 별도 조회 (orderItems 컬렉션에 직접 접근하지 않음)
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);

        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(order.getOrderId());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setPaymentStatus(order.getPaymentStatus());
        orderDto.setOrderStatus(order.getOrderStatus());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setPaymentMethod(order.getPaymentMethod());
        orderDto.setUserId(order.getUser().getUserId());
        orderDto.setUserName(order.getUser().getName());

        orderDto.setOrderItems(orderItems.stream()
                .map(com.capstone.ticketservice.order.dto.OrderItemDto::fromEntity)
                .collect(Collectors.toList()));

        return orderDto;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        List<Orders> orders = orderRepository.findByUserUserId(userId);
        List<OrderDto> orderDtos = new ArrayList<>();

        for (Orders order : orders) {
            OrderDto orderDto = new OrderDto();
            orderDto.setOrderId(order.getOrderId());
            orderDto.setTotalAmount(order.getTotalAmount());
            orderDto.setPaymentStatus(order.getPaymentStatus());
            orderDto.setOrderStatus(order.getOrderStatus());
            orderDto.setOrderDate(order.getOrderDate());
            orderDto.setPaymentMethod(order.getPaymentMethod());
            orderDto.setUserId(order.getUser().getUserId());
            orderDto.setUserName(order.getUser().getName());

            // 각 주문의 항목을 별도로 조회
            List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(order.getOrderId());
            orderDto.setOrderItems(orderItems.stream()
                    .map(com.capstone.ticketservice.order.dto.OrderItemDto::fromEntity)
                    .collect(Collectors.toList()));

            orderDtos.add(orderDto);
        }

        return orderDtos;
    }

    @Transactional
    public OrderDto completePayment(Long orderId, String paymentMethod) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // 결제 처리 로직
        order.setPaymentStatus("COMPLETED");
        order.setOrderStatus("COMPLETED");
        order.setPaymentMethod(paymentMethod);
        orderRepository.save(order);

        // 주문 항목 및 좌석 상태 업데이트 - 별도 조회 및 업데이트
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
        for (OrderItem orderItem : orderItems) {
            orderItem.setStatus("COMPLETED");

            // 좌석 상태 업데이트 (LOCKED -> SOLD)
            PerformanceSeat seat = orderItem.getPerformanceSeat();
            seat.setStatus("SOLD");
            seat.setLockUntil(null);
            performanceSeatRepository.save(seat);

            orderItemRepository.save(orderItem);
        }

        // DTO 반환
        OrderDto orderDto = new OrderDto();
        orderDto.setOrderId(order.getOrderId());
        orderDto.setTotalAmount(order.getTotalAmount());
        orderDto.setPaymentStatus(order.getPaymentStatus());
        orderDto.setOrderStatus(order.getOrderStatus());
        orderDto.setOrderDate(order.getOrderDate());
        orderDto.setPaymentMethod(order.getPaymentMethod());
        orderDto.setUserId(order.getUser().getUserId());
        orderDto.setUserName(order.getUser().getName());

        orderDto.setOrderItems(orderItems.stream()
                .map(com.capstone.ticketservice.order.dto.OrderItemDto::fromEntity)
                .collect(Collectors.toList()));

        return orderDto;
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        // 결제 전인 경우에만 취소 가능
        if ("COMPLETED".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("이미 결제가 완료된 주문은 취소할 수 없습니다.");
        }

        // 주문 상태 업데이트
        order.setOrderStatus("CANCELLED");
        order.setPaymentStatus("CANCELLED");
        orderRepository.save(order);

        // 좌석 상태 다시 가용으로 변경 - 별도 조회 및 업데이트
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);
        for (OrderItem orderItem : orderItems) {
            orderItem.setStatus("CANCELLED");

            // 좌석 상태 업데이트 (LOCKED -> AVAILABLE)
            PerformanceSeat seat = orderItem.getPerformanceSeat();
            seat.setStatus("AVAILABLE");
            seat.setLockUntil(null);
            performanceSeatRepository.save(seat);

            orderItemRepository.save(orderItem);
        }
    }
}