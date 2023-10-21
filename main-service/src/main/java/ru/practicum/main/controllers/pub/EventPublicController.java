package ru.practicum.main.controllers.pub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.event.service.EventServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@Validated
public class EventPublicController {

    private final EventService eventService;


    @Autowired
    public EventPublicController(EventServiceImpl eventServiceImpl) {
        this.eventService = eventServiceImpl;

    }

    @GetMapping
    public List<EventShortDto> getEventsAndStatsPublic(HttpServletRequest request,
                                                       @RequestParam(required = false) String text,
                                                       @RequestParam(required = false) List<Long> categories,
                                                       @RequestParam(required = false) boolean paid,
                                                       @RequestParam(required = false) String rangeStart,
                                                       @RequestParam(required = false) String rangeEnd,
                                                       @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                                       @RequestParam(required = false) String sort,
                                                       @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                       @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get users with text={}, from={}, size={}", text, from, size);
        return eventService.getEventsAndStatsPublic(request, text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{Id}")
    public EventFullDto getEventByIdAndStatsPublic(HttpServletRequest request,
                                                   @Positive @PathVariable("Id") Long eventId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get userById with Id={}", eventId);
        return eventService.getEventByIdAndStatsPublic(request, eventId);
    }
}