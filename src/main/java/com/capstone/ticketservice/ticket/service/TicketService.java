package com.capstone.ticketservice.ticket.service;

import com.capstone.ticketservice.order.model.OrderItem;
import com.capstone.ticketservice.order.repository.OrderItemRepository;
import com.capstone.ticketservice.seat.model.PerformanceSeat;
import com.capstone.ticketservice.seat.repository.PerformanceSeatRepository;
import com.capstone.ticketservice.ticket.dto.TicketDto;
import com.capstone.ticketservice.ticket.model.Ticket;
import com.capstone.ticketservice.ticket.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OrderItemRepository orderItemRepository;
    private final Random random = new Random();
    private final EntityManager entityManager;
    private final PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository, OrderItemRepository orderItemRepository, EntityManager entityManager, PerformanceSeatRepository performanceSeatRepository) {
        this.ticketRepository = ticketRepository;
        this.orderItemRepository = orderItemRepository;
        this.entityManager = entityManager;
        this.performanceSeatRepository = performanceSeatRepository;
    }

    /**
     * 주문 항목에 대한 티켓을 발급합니다.
     * @param orderItemId 주문 항목 ID
     * @return 발급된 티켓의 DTO
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TicketDto issueTicket(Long orderItemId) {
        log.info("티켓 발급 시작 (새 트랜잭션): orderItemId={}", orderItemId);

        // 이미 티켓이 발급되었는지 확인
        Optional<Ticket> existingTicket = ticketRepository.findByOrderItemOrderItemId(orderItemId);
        if (existingTicket.isPresent()) {
            log.info("이미 발급된 티켓이 있습니다: ticketId={}", existingTicket.get().getTicketId());
            return convertToDto(existingTicket.get());
        }


        // EntityManager를 통해 직접 OrderItem 로드 - 캐시 미사용
        OrderItem orderItem = entityManager.find(OrderItem.class, orderItemId);
        if (orderItem == null) {
            // 직접 SQL 쿼리로 확인
            TypedQuery<OrderItem> query = entityManager.createQuery(
                    "SELECT o FROM OrderItem o WHERE o.orderItemId = :id", OrderItem.class);
            query.setParameter("id", orderItemId);

            List<OrderItem> results = query.getResultList();
            if (results.isEmpty()) {
                log.error("OrderItem을 찾을 수 없습니다: {}", orderItemId);
                throw new EntityNotFoundException("주문 항목을 찾을 수 없습니다: " + orderItemId);
            }

            orderItem = results.get(0);
            log.info("네이티브 쿼리로 OrderItem 로드: id={}, status={}",
                    orderItem.getOrderItemId(), orderItem.getStatus());
        } else {
            log.info("EntityManager로 OrderItem 로드: id={}, status={}",
                    orderItem.getOrderItemId(), orderItem.getStatus());
        }

        // 티켓 생성
        Ticket ticket = new Ticket();
        ticket.setOrderItem(orderItem);
        ticket.setAccessCode(generateNumericAccessCode());
        ticket.setStatus("ISSUED");
        ticket.setIssuedAt(LocalDateTime.now());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // 저장 전에 연관관계 유효성 확인
        if (ticket.getOrderItem() == null || ticket.getOrderItem().getOrderItemId() == null) {
            log.error("티켓의 OrderItem 연관관계가 없습니다");
            throw new IllegalStateException("티켓에 OrderItem이 연결되어 있지 않습니다");
        }

        log.info("티켓 저장 시도: orderItemId={}", ticket.getOrderItem().getOrderItemId());

        // 티켓 저장
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("티켓이 성공적으로 발급되었습니다: ticketId={}", savedTicket.getTicketId());

        return convertToDto(savedTicket);
    }

    /**
     * 티켓 ID로 티켓을 조회합니다.
     * @param ticketId 티켓 ID
     * @return 티켓 DTO
     */
    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findByIdWithAllDetails(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("티켓을 찾을 수 없습니다: " + ticketId));

        return convertToDto(ticket);
    }


    @Transactional(readOnly = true)
    public Page<TicketDto> getTicketsByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        // 1. 전체 개수 조회
        long totalCount = ticketRepository.countByUserIdAndStatus(userId, status);

        if (totalCount == 0) {
            return Page.empty(pageable);
        }

        // 2. 모든 데이터 조회 (JOIN FETCH로 N+1 해결)
        List<Ticket> allTickets = ticketRepository.findByUserIdAndStatus(userId, status);

        // 3. 메모리에서 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allTickets.size());

        List<Ticket> pagedTickets = start >= allTickets.size() ?
                Collections.emptyList() : allTickets.subList(start, end);

        // 4. 기존 변환 로직 그대로 사용
        List<TicketDto> ticketDtos = pagedTickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new PageImpl<>(ticketDtos, pageable, totalCount);
    }

    /**
     * 티켓을 취소하고 관련 좌석을 다시 예약 가능한 상태로 변경합니다.
     * @param ticketId 티켓 ID
     * @return 업데이트된 티켓 DTO
     */
    @Transactional
    public TicketDto cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("티켓을 찾을 수 없습니다: " + ticketId));

        // 이미 사용된 티켓인지 확인
        if ("USED".equals(ticket.getStatus())) {
            throw new IllegalStateException("이미 사용된 티켓은 취소할 수 없습니다: " + ticketId);
        }

        // 이미 취소된 티켓인지 확인
        if ("CANCELLED".equals(ticket.getStatus())) {
            throw new IllegalStateException("이미 취소된 티켓입니다: " + ticketId);
        }

        try {
            // 1. 티켓 취소 처리
            ticket.setStatus("CANCELLED");
            ticket.setUpdatedAt(LocalDateTime.now());
            Ticket updatedTicket = ticketRepository.save(ticket);
            log.info("티켓이 취소되었습니다: ticketId={}", updatedTicket.getTicketId());

            // 2. 연관된 OrderItem 취소 처리
            OrderItem orderItem = ticket.getOrderItem();
            if (orderItem != null) {
                orderItem.setStatus("CANCELLED");
                orderItem.setUpdatedAt(LocalDateTime.now());
                orderItemRepository.save(orderItem);
                log.info("주문 항목이 취소되었습니다: orderItemId={}", orderItem.getOrderItemId());

                // 3. 연관된 PerformanceSeat를 다시 예약 가능한 상태로 변경
                PerformanceSeat performanceSeat = orderItem.getPerformanceSeat();
                if (performanceSeat != null) {
                    // 좌석 상태를 AVAILABLE로 변경
                    performanceSeat.setStatus("AVAILABLE");
                    performanceSeat.setLockUntil(null);
                    performanceSeat.setLockedByUser(null);
                    performanceSeat.setUpdatedAt(LocalDateTime.now());

                    performanceSeatRepository.save(performanceSeat);
                    log.info("좌석 상태가 복원되었습니다: performanceSeatId={}, status=AVAILABLE",
                            performanceSeat.getPerformanceSeatId());
                }
            }

            return convertToDto(updatedTicket);

        } catch (Exception e) {
            log.error("티켓 취소 처리 중 오류 발생: ticketId={}, error={}", ticketId, e.getMessage(), e);
            throw new RuntimeException("티켓 취소 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }



    /**
     * 사용자가 보유한 유효한 티켓 수를 조회합니다.
     * @param userId 사용자 ID
     * @return 유효한 티켓 수
     */
    @Transactional(readOnly = true)
    public long countValidTicketsByUserId(Long userId) {
        return ticketRepository.countValidTicketsByUserId(userId);
    }

    /**
     * Ticket 엔티티를 TicketDto로 변환합니다.
     * @param ticket Ticket 엔티티
     * @return TicketDto
     */
    private TicketDto convertToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setTicketId(ticket.getTicketId());
        dto.setAccessCode(ticket.getAccessCode());
        dto.setStatus(ticket.getStatus());
        dto.setIssuedAt(ticket.getIssuedAt());
        dto.setUsedAt(ticket.getUsedAt());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());

        // OrderItem 정보 설정
        OrderItem orderItem = ticket.getOrderItem();
        dto.setOrderItemId(orderItem.getOrderItemId());
        dto.setOrderId(orderItem.getOrder().getOrderId());
        dto.setPrice(orderItem.getPrice());

        // 사용자 정보 설정
        dto.setUserId(orderItem.getOrder().getUser().getUserId());
        dto.setUserName(orderItem.getOrder().getUser().getName());
        dto.setTicketHolderName(orderItem.getOrder().getUser().getName());

        // 이벤트 정보 설정
        dto.setEventId(orderItem.getPerformanceSeat().getEvent().getEventId());
        dto.setEventName(orderItem.getPerformanceSeat().getEvent().getName());
        dto.setEventStartDate(orderItem.getPerformanceSeat().getEvent().getStartDate());

        // 좌석 정보 설정
        dto.setSectionName(orderItem.getPerformanceSeat().getSeat().getSection().getName());
        dto.setRowName(orderItem.getPerformanceSeat().getSeat().getRowName());
        dto.setSeatNumber(orderItem.getPerformanceSeat().getSeat().getSeatNumber());

        // 바코드 URL 생성 (실제 구현은 별도 서비스로 분리할 수 있음)
        dto.setBarcodeImageUrl("/api/tickets/barcode/" + ticket.getAccessCode());

        // 기타 정보 설정
        dto.setVenueName("올림픽 체조경기장"); // 실제로는 이벤트 정보에서 가져와야 함
        dto.setVenueAddress("서울특별시 송파구 올림픽로 424"); // 실제로는 이벤트 정보에서 가져와야 함

        /**
         * 환불?
         */


        return dto;
    }

    /**
     * 고유한 숫자 액세스 코드를 생성합니다.
     * @return 생성된 액세스 코드
     */
    private Long generateNumericAccessCode() {
        // 12자리 숫자 액세스 코드 생성
        long min = 100000000000L; // 12자리 숫자의 최소값
        long max = 999999999999L; // 12자리 숫자의 최대값

        // 랜덤 숫자 생성
        long accessCode = min + ((long)(random.nextDouble() * (max - min)));

        // 중복 검사 (실제 구현에서는 더 효율적인 방법 고려 필요)
        while (ticketRepository.findByAccessCode(accessCode).isPresent()) {
            accessCode = min + ((long)(random.nextDouble() * (max - min)));
        }

        return accessCode;
    }
}