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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/admin/events")
@Validated
public class AdminEventController {

    private final EventService eventService;

    @Autowired
    public AdminEventController(EventServiceImpl eventServiceImpl) {
        this.eventService = eventServiceImpl;
    }

    @GetMapping
    public List<EventFullDto> getEventsAdmin(HttpServletRequest request,
                                             @NotEmpty @RequestParam Long[] users,
                                             @NotEmpty @RequestParam Long[] states,
                                             @NotEmpty @RequestParam Long[] categories,
                                             @NotBlank @RequestParam String rangeStart,
                                             @NotBlank @RequestParam String rangeEnd,
                                             @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                             @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get user with userId={}, from={}, size={}", users.toString(), from, size);
        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }


    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(HttpServletRequest request,
                                         @Positive @PathVariable Long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        return eventService.updateEventAdmin(eventId, updateEventAdminRequest);
    }
}