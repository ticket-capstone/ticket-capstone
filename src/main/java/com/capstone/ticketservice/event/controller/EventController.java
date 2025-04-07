package com.capstone.ticketservice.event.controller;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/events")
public class EventController {

    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{id}")
    public String getEventDetail(@PathVariable Long id, Model model) {
        try {
            // 먼저 이벤트 목록을 조회하여 샘플 데이터가 생성되도록 함
            if (eventService.getEvents().isEmpty()) {
                throw new RuntimeException("이벤트 데이터가 없습니다.");
            }

            Event event = eventService.getEventById(id);
            model.addAttribute("event", event);
            return "event/detail";
        } catch (Exception e) {
            System.err.println("이벤트 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
