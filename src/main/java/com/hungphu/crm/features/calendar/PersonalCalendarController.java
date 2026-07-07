package com.hungphu.crm.features.calendar;

import com.hungphu.crm.features.calendar.dto.PersonalEventRequest;
import com.hungphu.crm.features.calendar.dto.PersonalEventResponse;
import com.hungphu.crm.shared.response.ApiResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar/events")
@RequiredArgsConstructor
public class PersonalCalendarController {

    private final PersonalCalendarService calendarService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PersonalEventResponse>>> findEvents(
            @AuthenticationPrincipal UserDetailsImpl currentUser,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.findEvents(currentUser, from, to)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PersonalEventResponse>> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.findById(id, currentUser)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PersonalEventResponse>> createEvent(
            @Valid @RequestBody PersonalEventRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        calendarService.createEvent(request, currentUser),
                        "Tạo sự kiện thành công"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PersonalEventResponse>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody PersonalEventRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(ApiResponse.success(
                calendarService.updateEvent(id, request, currentUser),
                "Cập nhật sự kiện thành công"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        calendarService.deleteEvent(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success(null, "Xoá sự kiện thành công"));
    }
}