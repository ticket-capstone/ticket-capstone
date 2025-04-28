package com.capstone.ticketservice.section.service;

import com.capstone.ticketservice.section.dto.SectionDto;
import com.capstone.ticketservice.section.model.Section;
import com.capstone.ticketservice.section.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    @Autowired
    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Transactional(readOnly = true)
    public List<SectionDto> getAllSections() {
        List<Section> sections = sectionRepository.findAll();
        return sections.stream()
                .map(SectionDto::fromSection)
                .collect(Collectors.toList());
    }
}
