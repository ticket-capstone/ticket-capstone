package com.capstone.ticketservice.event.service;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    //todo: sectionrepostiroy 나중에 주입받기 (좌석 정보)

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> getEvents() {
        // DB 조회 결과가 없을 경우, 샘플 데이터 반환
        List<Event> events = eventRepository.findAll();

        if (events.isEmpty()) {
            events = createSampleEvents();
        }

        return events;
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("이벤트를 찾을 수 없습니다."));
    }

    private List<Event> createSampleEvents() {
        List<Event> events = new ArrayList<>();

        Event event1 = new Event();
        event1.setEventId(1l);
        event1.setName("2025 IU HEREH WORLD TOUR CONCERT : THE WINNING");
        event1.setDescription("2024 아이유 HEREH 월드투어 콘서트");
        event1.setEventType("CONCERT");
        event1.setStartDate(LocalDateTime.now().plusDays(7));
        event1.setEndDate(LocalDateTime.now().plusDays(30));
        event1.setCategory("콘서트");
        event1.setStatus("ACTIVE");

        Event event2 = new Event();
        event2.setEventId(2l);
        event2.setName("아이유의 10주년 전국·아시아 투어 콘서트 : 이 지금 dlwlrma");
        event2.setDescription("아이유의 데뷔 10주년 기념 투어 콘서트");
        event2.setEventType("CONCERT");
        event2.setStartDate(LocalDateTime.now().plusDays(14));
        event2.setEndDate(LocalDateTime.now().plusDays(16));
        event2.setCategory("콘서트");
        event2.setStatus("ACTIVE");

        Event event3 = new Event();
        event3.setEventId(3l);
        event3.setName("팬텀");
        event3.setDescription("뮤지컬 〈팬텀〉 10주년 기념 공연");
        event3.setEventType("MUSICAL");
        event3.setStartDate(LocalDateTime.now().plusDays(21));
        event3.setEndDate(LocalDateTime.now().plusDays(45));
        event3.setCategory("뮤지컬");
        event3.setStatus("ACTIVE");

        // 데이터베이스에 저장
        events.add(eventRepository.save(event1));
        events.add(eventRepository.save(event2));
        events.add(eventRepository.save(event3));

        return events;
    }
}
