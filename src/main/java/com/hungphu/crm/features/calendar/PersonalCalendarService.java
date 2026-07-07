package com.hungphu.crm.features.calendar;

import com.hungphu.crm.features.calendar.dto.PersonalEventRequest;
import com.hungphu.crm.features.calendar.dto.PersonalEventResponse;
import com.hungphu.crm.shared.security.UserDetailsImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PersonalCalendarService {

    List<PersonalEventResponse> findEvents(
            UserDetailsImpl currentUser, LocalDate from, LocalDate to);

    PersonalEventResponse findById(UUID id, UserDetailsImpl currentUser);

    PersonalEventResponse createEvent(
            PersonalEventRequest request, UserDetailsImpl currentUser);

    PersonalEventResponse updateEvent(
            UUID id, PersonalEventRequest request, UserDetailsImpl currentUser);

    void deleteEvent(UUID id, UserDetailsImpl currentUser);
}