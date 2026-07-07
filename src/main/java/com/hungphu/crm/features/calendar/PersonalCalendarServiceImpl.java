package com.hungphu.crm.features.calendar;

import com.hungphu.crm.features.calendar.dto.PersonalEventRequest;
import com.hungphu.crm.features.calendar.dto.PersonalEventResponse;
import com.hungphu.crm.features.calendar.entity.PersonalEvent;
import com.hungphu.crm.features.calendar.repository.PersonalEventRepository;
import com.hungphu.crm.features.user.repository.UserRepository;
import com.hungphu.crm.shared.exception.BusinessException;
import com.hungphu.crm.shared.exception.ResourceNotFoundException;
import com.hungphu.crm.shared.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalCalendarServiceImpl implements PersonalCalendarService {

    private final PersonalEventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PersonalEventResponse> findEvents(UserDetailsImpl currentUser,
                                                   LocalDate from, LocalDate to) {
        // Default: lấy tháng hiện tại nếu không truyền
        LocalDate start = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate end = to != null ? to : start.plusMonths(1).minusDays(1);

        return eventRepository
                .findByUserIdAndDateRange(currentUser.getId(), start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalEventResponse findById(UUID id, UserDetailsImpl currentUser) {
        PersonalEvent event = findOwnedEventOrThrow(id, currentUser.getId());
        return toResponse(event);
    }

    @Override
    @Transactional
    public PersonalEventResponse createEvent(PersonalEventRequest request,
                                             UserDetailsImpl currentUser) {
        validateTime(request);

        PersonalEvent event = new PersonalEvent();
        event.setUser(userRepository.getReferenceById(currentUser.getId()));
        applyRequest(event, request);

        PersonalEvent saved = eventRepository.save(event);
        log.info("Personal event created by user {}: {} on {}",
                currentUser.getId(), saved.getTitle(), saved.getEventDate());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public PersonalEventResponse updateEvent(UUID id, PersonalEventRequest request,
                                             UserDetailsImpl currentUser) {
        PersonalEvent event = findOwnedEventOrThrow(id, currentUser.getId());
        validateTime(request);
        applyRequest(event, request);

        PersonalEvent saved = eventRepository.save(event);
        log.info("Personal event {} updated by user {}", id, currentUser.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id, UserDetailsImpl currentUser) {
        PersonalEvent event = findOwnedEventOrThrow(id, currentUser.getId());
        eventRepository.delete(event);
        log.info("Personal event {} deleted by user {}", id, currentUser.getId());
    }

    // ══════════════════════════════════════════════════════════════
    // Private helpers
    // ══════════════════════════════════════════════════════════════

    private PersonalEvent findOwnedEventOrThrow(UUID id, UUID userId) {
        return eventRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sự kiện", id));
    }

    private void validateTime(PersonalEventRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (!request.getEndTime().isAfter(request.getStartTime())) {
                throw new BusinessException(
                        "Giờ kết thúc phải sau giờ bắt đầu",
                        HttpStatus.BAD_REQUEST, "CAL_001");
            }
        }
    }

    private void applyRequest(PersonalEvent event, PersonalEventRequest request) {
        event.setTitle(request.getTitle().trim());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setColor(request.getColor() != null ? request.getColor() : "#2563EB");
        event.setReminder(request.isReminder());
        event.setRepeatType(request.getRepeatType() != null
                ? request.getRepeatType()
                : com.hungphu.crm.shared.enums.RepeatType.NONE);
    }

    private PersonalEventResponse toResponse(PersonalEvent event) {
        return PersonalEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .color(event.getColor())
                .reminder(event.isReminder())
                .repeatType(event.getRepeatType())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}