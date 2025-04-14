package com.capstone.ticketservice.event.service;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        List<Event> events = eventRepository.findAll();

        return events;
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("이벤트를 찾을 수 없습니다."));
    }

}
