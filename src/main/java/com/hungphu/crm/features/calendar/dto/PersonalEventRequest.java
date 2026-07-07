package com.hungphu.crm.features.calendar.dto;

import com.hungphu.crm.shared.enums.RepeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PersonalEventRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Ngày không được để trống")
    private LocalDate eventDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String color = "#2563EB";
    private boolean reminder = false;
    private RepeatType repeatType = RepeatType.NONE;
}