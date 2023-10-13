package ru.practicum.main.controllers.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.event.service.EventServiceImpl;
import ru.practicum.main.participation.dto.ParticipationRequestDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users/{userId}/events")
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @Autowired
    public PrivateEventController(EventServiceImpl privateEventService) {
        this.eventService = privateEventService;
    }

    @GetMapping
    public List<EventShortDto> getEvents(HttpServletRequest request,
                                         @Positive @PathVariable Long userId,
                                         @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get user with userId={}, from={}, size={}", userId, from, size);
        return eventService.getEventsPrivate(userId, from, size);
    }

    @PostMapping
    public EventFullDto addEvent(HttpServletRequest request,
                                 @Positive @PathVariable Long userId,
                                 @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Create event with userId={}", userId);
        return eventService.addEventPrivate(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public List<EventFullDto> getEvents(HttpServletRequest request,
                                        @Positive @PathVariable Long userId,
                                        @Positive @PathVariable Long eventId,
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                        @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get user with userId={}, eventId={}, from={}, size={}", userId, eventId, from, size);
        return eventService.getEventsPrivate(userId, eventId, from, size);
    }


    @PatchMapping("/{eventId}")
    public EventFullDto updateEventUserRequest(HttpServletRequest request,
                                               @Positive @PathVariable Long userId,
                                               @Positive @PathVariable Long eventId,
                                               @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Update event with userId={} and eventId={}", userId, eventId);
        return eventService.updateEventPrivate(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsEventsUser(HttpServletRequest request,
                                                               @Positive @PathVariable Long userId,
                                                               @Positive @PathVariable Long eventId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get requests with userId={}, eventId={}", userId, eventId);
        return eventService.getRequestsEventsUserPrivate(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(HttpServletRequest request,
                                                                   @Positive @PathVariable Long userId,
                                                                   @Positive @PathVariable Long eventId,
                                                                   @Valid @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Update request status with userId={} and eventId={}", userId, eventId);
        return eventService.updateEventRequestStatusPrivate(userId, eventId, eventRequestStatusUpdateRequest);
    }
}