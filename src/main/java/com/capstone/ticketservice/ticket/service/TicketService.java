package com.capstone.ticketservice.ticket.service;

import com.capstone.ticketservice.order.model.OrderItem;
import com.capstone.ticketservice.order.repository.OrderItemRepository;
import com.capstone.ticketservice.ticket.dto.TicketDto;
import com.capstone.ticketservice.ticket.dto.TicketResponseDto;
import com.capstone.ticketservice.ticket.model.Ticket;
import com.capstone.ticketservice.ticket.repository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OrderItemRepository orderItemRepository;
    private final Random random = new Random();

    @Autowired
    public TicketService(TicketRepository ticketRepository, OrderItemRepository orderItemRepository) {
        this.ticketRepository = ticketRepository;
        this.orderItemRepository = orderItemRepository;
    }

    /**
     * 주문 항목에 대한 티켓을 발급합니다.
     * @param orderItemId 주문 항목 ID
     * @return 발급된 티켓의 DTO
     */
    @Transactional
    public TicketDto issueTicket(Long orderItemId) {
        log.info("티켓 발급 시작: orderItemId={}", orderItemId);

        // 이미 티켓이 발급되었는지 확인
        Optional<Ticket> existingTicket = ticketRepository.findByOrderItemOrderItemId(orderItemId);
        if (existingTicket.isPresent()) {
            log.info("이미 발급된 티켓이 있습니다: ticketId={}", existingTicket.get().getTicketId());
            return convertToDto(existingTicket.get());
        }

        // 주문 항목 조회
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("주문 항목을 찾을 수 없습니다: " + orderItemId));

        // 주문 항목의 상태 확인
        if (!"COMPLETED".equals(orderItem.getStatus())) {
            throw new IllegalStateException("결제가 완료되지 않은 주문 항목입니다: " + orderItemId);
        }

        // 티켓 생성
        Ticket ticket = new Ticket();
        ticket.setOrderItem(orderItem);
        ticket.setAccessCode(generateNumericAccessCode());
        ticket.setStatus("ISSUED"); // 발급됨
        ticket.setIssuedAt(LocalDateTime.now());
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // 티켓 저장
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("티켓이 성공적으로 발급되었습니다: ticketId={}", savedTicket.getTicketId());

        return convertToDto(savedTicket);
    }

    /**
     * 주문의 모든 주문 항목에 대해 티켓을 발급합니다.
     * @param orderId 주문 ID
     * @return 발급된 티켓 DTO 목록
     */
    @Transactional
    public List<TicketDto> issueTicketsForOrder(Long orderId) {
        log.info("주문에 대한 티켓 발급 시작: orderId={}", orderId);

        // 주문에 속한 모든 주문 항목 조회
        List<OrderItem> orderItems = orderItemRepository.findByOrderOrderId(orderId);

        // 각 주문 항목에 대해 티켓 발급
        return orderItems.stream()
                .map(item -> issueTicket(item.getOrderItemId()))
                .collect(Collectors.toList());
    }

    /**
     * 티켓 ID로 티켓을 조회합니다.
     * @param ticketId 티켓 ID
     * @return 티켓 DTO
     */
    @Transactional(readOnly = true)
    public TicketDto getTicketById(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("티켓을 찾을 수 없습니다: " + ticketId));

        return convertToDto(ticket);
    }

    /**
     * 액세스 코드로 티켓을 조회합니다.
     * @param accessCode 액세스 코드
     * @return 티켓 DTO
     */
    @Transactional(readOnly = true)
    public TicketDto getTicketByAccessCode(Long accessCode) {
        Ticket ticket = ticketRepository.findByAccessCode(accessCode)
                .orElseThrow(() -> new EntityNotFoundException("액세스 코드로 티켓을 찾을 수 없습니다: " + accessCode));

        return convertToDto(ticket);
    }

    /**
     * 사용자 ID로 티켓 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 티켓 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByUserId(Long userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);

        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID와 상태로 티켓 목록을 조회합니다.
     * @param userId 사용자 ID
     * @param status 티켓 상태
     * @return 티켓 DTO 목록
     */
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByUserIdAndStatus(Long userId, String status) {
        List<Ticket> tickets = ticketRepository.findByUserIdAndStatus(userId, status);

        return tickets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID와 상태로 티켓 목록을 페이지네이션하여 조회합니다.
     * @param userId 사용자 ID
     * @param status 티켓 상태
     * @param pageable 페이지네이션 정보
     * @return 티켓 DTO 페이지
     */
    @Transactional(readOnly = true)
    public Page<TicketDto> getTicketsByUserIdAndStatus(Long userId, String status, Pageable pageable) {
        Page<Ticket> ticketPage = ticketRepository.findByUserIdAndStatus(userId, status, pageable);

        return ticketPage.map(this::convertToDto);
    }

    /**
     * 티켓을 사용 처리합니다.
     * @param ticketId 티켓 ID
     * @return 업데이트된 티켓 DTO
     */
    @Transactional
    public TicketDto useTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException("티켓을 찾을 수 없습니다: " + ticketId));

        // 이미 사용된 티켓인지 확인
        if ("USED".equals(ticket.getStatus())) {
            throw new IllegalStateException("이미 사용된 티켓입니다: " + ticketId);
        }

        // 취소된 티켓인지 확인
        if ("CANCELLED".equals(ticket.getStatus())) {
            throw new IllegalStateException("취소된 티켓입니다: " + ticketId);
        }

        // 티켓 사용 처리
        ticket.setStatus("USED");
        ticket.setUsedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("티켓이 사용 처리되었습니다: ticketId={}", updatedTicket.getTicketId());

        return convertToDto(updatedTicket);
    }

    /**
     * 티켓을 취소합니다.
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

        // 티켓 취소 처리
        ticket.setStatus("CANCELLED");
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        log.info("티켓이 취소되었습니다: ticketId={}", updatedTicket.getTicketId());

        return convertToDto(updatedTicket);
    }

    /**
     * 특정 이벤트에 대한 유효한 티켓 수를 조회합니다.
     * @param eventId 이벤트 ID
     * @return 유효한 티켓 수
     */
    @Transactional(readOnly = true)
    public long countValidTicketsByEventId(Long eventId) {
        List<Ticket> validTickets = ticketRepository.findByEventIdAndStatus(eventId, "ISSUED");
        return validTickets.size();
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