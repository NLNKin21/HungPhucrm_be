package com.hungphu.crm.features.calendar.dto;

import com.hungphu.crm.shared.enums.RepeatType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
public class PersonalEventResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String color;
    private boolean reminder;
    private RepeatType repeatType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}