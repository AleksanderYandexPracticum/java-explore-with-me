package ru.practicum.main.participation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;
import ru.practicum.main.event.model.Status;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.RepeatParticipationRequestException;
import ru.practicum.main.participation.ParticipationMapper;
import ru.practicum.main.participation.dto.ParticipationRequestDto;
import ru.practicum.main.participation.model.ParticipationRequest;
import ru.practicum.main.participation.repository.ParticipationRepository;
import ru.practicum.main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository participationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public ParticipationServiceImpl(ParticipationRepository participationRepository, EventRepository eventRepository, UserRepository userRepository) {
        this.participationRepository = participationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getParticipationRequestPrivate(Long userId) {
        if (userRepository.getUserById(userId) == null) {
            throw new NotFoundException("The required object was not found.");
        }
        List<Long> eventIdList = eventRepository.getEventsByInitiatorId(userId)
                .stream()
                .map((event) -> event.getId())
                .collect(Collectors.toList());
        List<ParticipationRequest> list = participationRepository.getParticipationRequestsByRequesterAndEventNotIn(userId, eventIdList);

        return list.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ParticipationRequestDto addParticipationRequestPrivate(Long userId, Long eventId) {
        Event event = eventRepository.getEventsById(eventId);
        if (event == null) {
            throw new NotFoundException("The required object was not found.");
        } else if (event.getInitiator().equals(userId)) {
            throw new RepeatParticipationRequestException("Request self userId ");
        } else if (!event.getState().equals(State.PUBLISHED)) {
            throw new RepeatParticipationRequestException("Request not PUBLISED ");
        } else if (event.getConfirmedRequests().equals(event.getParticipantLimit())) {
            throw new RepeatParticipationRequestException("Request overflow ParticipantLimit ");
        }
        List<ParticipationRequest> participationRequestList = participationRepository.getParticipationRequestsByRequesterAndEvent(userId, eventId);
        if (participationRequestList.size() != 0) {
            throw new RepeatParticipationRequestException("Request has been");
        }


        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(eventId)
                .requester(userId)
                .status(event.isRequestModeration() ? Status.PENDING : Status.CONFIRMED)
                .build();

        ParticipationRequestDto participationRequestDto = ParticipationMapper
                .toParticipationRequestDto(participationRepository.save(participationRequest));

        return participationRequestDto;
    }


    @Transactional
    @Override
    public ParticipationRequestDto updateRejectedParticipationRequestPrivate(Long userId, Long requestId) {

        ParticipationRequest participationRequest = participationRepository.getParticipationRequestByIdAndRequester(requestId, userId);
        if (participationRequest == null) {
            throw new NotFoundException("The required object was not found.");
        }
        if (!participationRequest.getStatus().equals(Status.CONFIRMED)) {
            participationRequest.setStatus(Status.REJECTED);
        } else {
            Event event = eventRepository.getEventsById(participationRequest.getEvent());
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
            participationRequest.setStatus(Status.REJECTED);
        }

        ParticipationRequestDto participationRequestDto = ParticipationMapper
                .toParticipationRequestDto(participationRepository.save(participationRequest));

        return participationRequestDto;
    }
}