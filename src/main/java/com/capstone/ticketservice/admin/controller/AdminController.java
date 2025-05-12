package com.capstone.ticketservice.admin.controller;


import com.capstone.ticketservice.order.dto.OrderItemDto;
import com.capstone.ticketservice.order.service.OrderService;
import com.capstone.ticketservice.seat.dto.PerformanceSeatDto;
import com.capstone.ticketservice.seat.dto.SeatDto;
import com.capstone.ticketservice.seat.service.PerformanceSeatService;
import com.capstone.ticketservice.seat.service.SeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/dashboard")
@Slf4j
public class AdminController {
    private final OrderService orderService;
    private final PerformanceSeatService performanceSeatService;
    private final SeatService seatService;

    @Autowired
    public AdminController(OrderService orderService , PerformanceSeatService performanceSeatService , SeatService seatService) {
        this.orderService = orderService;
        this.performanceSeatService = performanceSeatService;
        this.seatService = seatService;
    }

    @GetMapping("")
    public String searchSeatInfo(Model model) {
        return "/admin/dashboard";
    }

    @PostMapping("/search")
    public String getOrderItemBySeatDetail(@RequestParam String sectionName,
                                       @RequestParam String rowName,
                                       @RequestParam String seatNumber, Model model) {

        try {
            SeatDto SeatDto = seatService.getSeatByDetail(rowName,seatNumber,sectionName);
            Long seatId = SeatDto.getSeatId();

            List<PerformanceSeatDto> performanceSeatDtos = performanceSeatService.getPerformanceSeatsBySeatId(seatId);
            List<List<OrderItemDto>> orderItemDtos = new ArrayList<>();

            for(PerformanceSeatDto performanceSeatDto : performanceSeatDtos) {
                orderItemDtos.add(orderService.getOrderItemByPerformanceSeatId(performanceSeatDto.getPerformanceSeatId()));
            }

            model.addAttribute("orderItemDtos", orderItemDtos);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage() );
        }
        return "/admin/dashboard";
    }
}
