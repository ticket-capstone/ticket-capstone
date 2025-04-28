package com.capstone.ticketservice.section.dto;

import com.capstone.ticketservice.section.model.Section;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SectionDto {

    private Long sectionId;
    private String name;
    private Integer capacity;
    private String priceCategory;
    private String status;

    public static SectionDto fromSection(Section section) {
        return SectionDto.builder()
                .sectionId(section.getSectionId())
                .name(section.getName())
                .capacity(section.getCapacity())
                .priceCategory(section.getPriceCategory())
                .status(section.getStatus())
                .build();
    }
}
