package com.capstone.ticketservice.event.service;

import com.capstone.ticketservice.event.model.Event;
import com.capstone.ticketservice.event.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    @Autowired
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Cacheable(value = "events")
    public List<Event> getEvents() {
        List<Event> events = eventRepository.findAll();
        return events;
    }

    @Cacheable(value = "event", key = "#eventId")
    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("이벤트를 찾을 수 없습니다."));
    }

}
