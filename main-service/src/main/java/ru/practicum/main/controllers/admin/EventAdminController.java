package ru.practicum.main.controllers.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.service.EventServiceImpl;
import ru.practicum.main.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@Validated
public class EventAdminController {

    private final EventService eventService;

    @Autowired
    public EventAdminController(EventServiceImpl eventServiceImpl) {
        this.eventService = eventServiceImpl;
    }

    @GetMapping
    public List<EventFullDto> getEventsAdmin(HttpServletRequest request,
                                             @RequestParam(required = false) List<Long> users,
                                             @RequestParam(required = false) List<String> states,
                                             @RequestParam(required = false) List<Long> categories,
                                             @RequestParam(required = false) String rangeStart,
                                             @RequestParam(required = false) String rangeEnd,
                                             @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        List<EventFullDto> list = eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
        return list;
    }


    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(HttpServletRequest request,
                                         @NotNull @PathVariable(required = false) Long eventId,
                                         @NotNull @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        EventFullDto eventFullDto = eventService.updateEventAdmin(eventId, updateEventAdminRequest);
        return eventFullDto;
    }
}