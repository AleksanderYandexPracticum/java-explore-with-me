package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.dto.UpdateEventUserRequest;
import ru.practicum.main.participation.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsPrivate(Long userId, Integer from, Integer size);

    EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getEventsPrivate(Long userId, Long eventId, Integer from, Integer size);

    EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequestsEventsUserPrivate(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);


    List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<EventShortDto> getEventsAndStatsPublic(HttpServletRequest request, String text, List<Long> categories, boolean paid,
                                                String rangeStart, String rangeEnd, boolean onlyAvailable,
                                                String sort, Integer from, Integer size);

    EventFullDto getEventByIdAndStatsPublic(HttpServletRequest request, Long Id);
}