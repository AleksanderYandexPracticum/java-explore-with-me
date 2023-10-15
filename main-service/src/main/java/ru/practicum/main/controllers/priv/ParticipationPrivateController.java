package ru.practicum.main.controllers.priv;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.participation.dto.ParticipationRequestDto;
import ru.practicum.main.participation.service.ParticipationService;
import ru.practicum.main.participation.service.ParticipationServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@Validated
public class ParticipationPrivateController {

    private final ParticipationService participationService;

    @Autowired
    public ParticipationPrivateController(ParticipationServiceImpl participationServiceImpl) {
        this.participationService = participationServiceImpl;
    }

    @GetMapping
    public List<ParticipationRequestDto> getParticipationRequestPrivate(HttpServletRequest request,
                                                                        @NotNull @Positive @PathVariable Long userId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get ParticipationRequest with userId={}", userId);
        return participationService.getParticipationRequestPrivate(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequestPrivate(HttpServletRequest request,
                                                                  @Positive @PathVariable(required = false) Long userId,
                                                                  @Positive @RequestParam(required = false) Long eventId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Create ParticipationRequest with userId={}, eventId={}", userId, eventId);

        return participationService.addParticipationRequestPrivate(userId, eventId);
    }


    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto updateRejectedParticipationRequestPrivate(HttpServletRequest request,
                                                                             @NotNull @Positive @PathVariable Long userId,
                                                                             @NotNull @Positive @PathVariable(required = true, name = "requestId") Long requestId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Rejected ParticipationRequest with userId={}, requestId={}", userId, requestId);
        return participationService.updateRejectedParticipationRequestPrivate(userId, requestId);
    }
}