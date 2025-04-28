package com.capstone.ticketservice.section.repository;

import com.capstone.ticketservice.section.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

//    @Query(value = "SELECT s FROM Section s JOIN PerformanceSeat ps ON ps.seat.section.sectionId = s.sectionId " +
//            "WHERE ps.event.eventId = :eventId GROUP BY s.sectionId")
//    List<Section> findByEventId(@Param("eventId") Long eventId);
}
