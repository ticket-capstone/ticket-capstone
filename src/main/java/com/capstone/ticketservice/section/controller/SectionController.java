package com.capstone.ticketservice.section.controller;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.service.EventService;
import com.capstone.ticketservice.section.dto.SectionDto;
import com.capstone.ticketservice.section.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/api")
public class SectionController {

    private final SectionService sectionService;
    private final EventService eventService;

    @Autowired
    public SectionController(SectionService sectionService, EventService eventService) {
        this.sectionService = sectionService;
        this.eventService = eventService;
    }

    // 구역 선택 화면 (시각적 맵)
    @GetMapping("/events/{eventId}/sections")
    public String getSectionMap(@PathVariable Long eventId, Model model) {
        Event event = eventService.getEventById(eventId);
        List<SectionDto> sections = sectionService.getAllSections();
        model.addAttribute("event", event);
        model.addAttribute("sections", sections);
        return "section/map";
    }
}
