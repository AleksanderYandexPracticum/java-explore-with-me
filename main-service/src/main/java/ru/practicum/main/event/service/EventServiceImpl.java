package ru.practicum.main.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.stats.stats.StatsClient;
import ru.practicum.ewm.dto.stats.statsDto.EndpointHitDto;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.EventMapper;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.dto.UpdateEventUserRequest;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;
import ru.practicum.main.event.model.StateAction;
import ru.practicum.main.event.model.Status;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.EventDateException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.OverflowParticipiantLimitException;
import ru.practicum.main.exception.StateArgumentException;
import ru.practicum.main.exception.StatusPerticipationRequestException;
import ru.practicum.main.participation.ParticipationMapper;
import ru.practicum.main.participation.dto.ParticipationRequestDto;
import ru.practicum.main.participation.model.ParticipationRequest;
import ru.practicum.main.participation.repository.ParticipationRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final StatsClient statsClient;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            ParticipationRepository participationRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;

        this.statsClient = statsClient;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventShortDto> getEventsPrivate(Long userId, Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        List<EventShortDto> list = eventRepository.getEventsByInitiatorId(userId, pageable)
                .stream()
                .map((event) -> EventMapper.toEventShortDto(event))
                .collect(Collectors.toList());
        return list;
    }

    @Transactional
    @Override
    public EventFullDto addEventPrivate(Long userId, NewEventDto newEventDto) {
        LocalDateTime start = LocalDateTime.parse(newEventDto.getEventDate(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (start.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("Timeeeeeeeeeeeeeeeeeeeeee");
        }
        Category category = categoryRepository.getById(newEventDto.getCategory());
        User user = userRepository.getUserById(userId);
        Event event = EventMapper.toEvent(newEventDto, user, category);
        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsPrivate(Long userId, Long eventId, Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        List<EventFullDto> list = eventRepository.getEventsByInitiatorIdAndId(userId, eventId, pageable)
                .stream()
                .map((event) -> EventMapper.toEventFullDto(event))
                .collect(Collectors.toList());
        if (list.size() == 0) {
            throw new NotFoundException("The required object was not found.");
        }
        return list;
    }


    @Transactional
    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event oldEvent = eventRepository.getEventsByIdAndInitiatorId(eventId, userId);
        if (oldEvent == null) {
            throw new NotFoundException("The required object was not found.");
        }

        if (oldEvent.getState().equals(State.PUBLISHED)) {
            throw new StateArgumentException("Only pending or canceled events can be changed");
        }

        LocalDateTime start = LocalDateTime.parse(updateEventUserRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (start.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new EventDateException("Time start" + start + "before eventDate + 2 Hours");
        }

        Category newCategory = updateEventUserRequest.getCategory() == null ?
                oldEvent.getCategory() : categoryRepository.getById(updateEventUserRequest.getCategory());
        Event upEvent = EventMapper.toEvent(updateEventUserRequest, oldEvent, newCategory);


        upEvent.setAnnotation(upEvent.getAnnotation() == null ? oldEvent.getAnnotation() : upEvent.getAnnotation());
        upEvent.setDescription(upEvent.getDescription() == null ? oldEvent.getDescription() : upEvent.getDescription());
        upEvent.setEventDate(upEvent.getEventDate() == null ? oldEvent.getEventDate() : upEvent.getEventDate());
        upEvent.setLocation(upEvent.getLocation() == null ? oldEvent.getLocation() : upEvent.getLocation());
        upEvent.setPaid(upEvent.isPaid());
        upEvent.setParticipantLimit(upEvent.getParticipantLimit() == null ? oldEvent.getParticipantLimit() : upEvent.getParticipantLimit());
        upEvent.setRequestModeration(upEvent.isRequestModeration());


        upEvent.setState(State.valueOf(updateEventUserRequest.getStateAction().equals(StateAction.SEND_TO_REVIEW) ?
                State.PENDING.toString() : State.CANCELED.toString()));

        upEvent.setTitle(upEvent.getTitle() == null ? oldEvent.getTitle() : upEvent.getTitle());

        upEvent.setId(eventId);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventRepository.save(upEvent));

        return eventFullDto;
    }

    @Transactional(readOnly = true)
    @Override
    public List<ParticipationRequestDto> getRequestsEventsUserPrivate(Long userId, Long eventId) {

        List<ParticipationRequestDto> list = participationRepository.getParticipationRequestsByRequesterAndEvent(userId, eventId)
                .stream()
                .map((participationRequest) -> ParticipationMapper.toParticipationRequestDto(participationRequest))
                .collect(Collectors.toList());
        return list;
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateEventRequestStatusPrivate(Long userId,
                                                                          Long eventId,
                                                                          EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        Event event = eventRepository.getEventsByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException("The required object was not found.");
        }
        Status status = Status.valueOf(eventRequestStatusUpdateRequest.getStatus());

        List<ParticipationRequest> list = participationRepository.getParticipationRequestByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {  //////////////////// ИЛИ ПРОСТО ВЕРНУТЬ !!!!!!!!!!!!
            List<ParticipationRequestDto> listDto = list.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
            EventRequestStatusUpdateResult eventRequestStatusUpdateResult = new EventRequestStatusUpdateResult(
                    listDto, new ArrayList<>());

//            list = participationRepository.getParticipationRequestByIdIn(eventRequestStatusUpdateRequest.getRequestIds())
//                    .stream()
//                    .map((participationRequest) -> {
//                        participationRequest.setStatus(status);
//                        ParticipationRequest p = participationRepository.save(participationRequest);
//                        return p;
//                    }).collect(Collectors.toList());
            return eventRequestStatusUpdateResult;
        }

        if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {
            throw new OverflowParticipiantLimitException("The participant limit has been reached"); // 409 ЛИМИТ///////////////////////////////////
        }

        List<ParticipationRequest> listPending = new ArrayList<>();
        List<ParticipationRequest> rejectedList = new ArrayList<>();

        for (ParticipationRequest participationRequest : list) {
            if (participationRequest.getStatus().equals(Status.PENDING)) {

                participationRequest.setStatus(status);
                listPending.add(participationRequest);

                participationRepository.save(participationRequest);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);
            } else {

                rejectedList.add(participationRequest);
            }

            if (event.getParticipantLimit().equals(event.getConfirmedRequests())) {


                for (ParticipationRequest pRequest : rejectedList) {
                    pRequest.setStatus(Status.REJECTED);
                    participationRepository.save(pRequest);
                }
            }
        }
        if (listPending.size() == 0) {
            throw new StatusPerticipationRequestException("The status Request NOT PENDING"); // 409 ЛИМИТ///////////////////////////////////
        }

        List<ParticipationRequestDto> listPendingDto = listPending
                .stream()
                .map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
        List<ParticipationRequestDto> rejectedListDto = listPending
                .stream()
                .map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult(listPendingDto, rejectedListDto);
        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<EventFullDto> getEventsAdmin(Long[] users, Long[] states, Long[] categories,
                                             String rangeStart, String rangeEnd, Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        LocalDateTime start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (start.isAfter(end)) {
            throw new IllegalArgumentException();
        }

        return eventRepository.getEventsByInitiatorInAndStateInAndCategoryInAndEventDateAfterAndEventDateBefore(users, states, categories, start, end, pageable)
                .stream()
                .map((event) -> EventMapper.toEventFullDto(event))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event oldEvent = eventRepository.getEventsById(eventId);
        if (oldEvent == null) {
            throw new NotFoundException("The required object was not found.");
        }

        LocalDateTime start = LocalDateTime.parse(updateEventAdminRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        if (start.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new EventDateException("Time start" + start + "before eventDate + 1 Hours");
        }

        if (!oldEvent.getState().equals(State.PENDING) && updateEventAdminRequest.getStateAction().equals(StateAction.PUBLISHED_EVENT)) {
            throw new StateArgumentException("Cannot publish the event because it's not in the right state: PUBLISHED OR CANCELED");
        }
        if (oldEvent.getState().equals(State.PUBLISHED) && updateEventAdminRequest.getStateAction().equals(StateAction.REJECTED_EVENT)) {
            throw new StateArgumentException("Cannot reject the event because it's not in the right state: PUBLISHED");
        }


        Category newCategory = updateEventAdminRequest.getCategory() == null ?
                oldEvent.getCategory() : categoryRepository.getById(updateEventAdminRequest.getCategory());
        Event upEvent = EventMapper.toEvent(updateEventAdminRequest, oldEvent, newCategory);


        upEvent.setAnnotation(upEvent.getAnnotation() == null ? oldEvent.getAnnotation() : upEvent.getAnnotation());
        upEvent.setDescription(upEvent.getDescription() == null ? oldEvent.getDescription() : upEvent.getDescription());
        upEvent.setEventDate(upEvent.getEventDate() == null ? oldEvent.getEventDate() : upEvent.getEventDate());
        upEvent.setLocation(upEvent.getLocation() == null ? oldEvent.getLocation() : upEvent.getLocation());
        upEvent.setPaid(upEvent.isPaid());
        upEvent.setParticipantLimit(upEvent.getParticipantLimit() == null ? oldEvent.getParticipantLimit() : upEvent.getParticipantLimit());
        upEvent.setRequestModeration(upEvent.isRequestModeration());


        upEvent.setState(State.valueOf(updateEventAdminRequest.getStateAction().equals(StateAction.PUBLISHED_EVENT) ?
                State.PUBLISHED.toString() : State.CANCELED.toString()));

        upEvent.setTitle(upEvent.getTitle() == null ? oldEvent.getTitle() : upEvent.getTitle());

        upEvent.setId(eventId);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventRepository.save(upEvent));
        return eventFullDto;
    }


    @Transactional
    @Override
    public List<EventShortDto> getEventsAndStatsPublic(HttpServletRequest request, String text, Long[] categories, boolean paid,
                                                       String rangeStart, String rangeEnd, boolean onlyAvailable,
                                                       String sort, Integer from, Integer size) {
        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        List<Event> list;
        LocalDateTime timeNow = LocalDateTime.now();

        if (rangeStart == null && rangeEnd == null) {
            if (sort.equals("EVENT_DATE")) {
                if (onlyAvailable) {
                    list = eventRepository.getAllEventSortEventDateParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            timeNow,
                            text,
                            pageable);
                } else {
                    list = eventRepository.getAllEventSortEventDateNoLimitParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            timeNow,
                            text,
                            pageable);
                }
            } else {
                if (onlyAvailable) {
                    list = eventRepository.getAllEventSortViewsParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            timeNow,
                            text,
                            pageable);
                } else {
                    list = eventRepository.getAllEventSortViewsNoLimitParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            timeNow,
                            text,
                            pageable);
                }
            }

        } else {
            LocalDateTime startTime = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTime = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (sort.equals("EVENT_DATE")) {
                if (onlyAvailable) {
                    list = eventRepository.getAllEventSortEventDatePeriodDateParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            startTime,
                            endTime,
                            text,
                            pageable);
                } else {
                    list = eventRepository.getAllEventSortEventDateNoLimitPeriodDateParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            startTime,
                            endTime,
                            text,
                            pageable);
                }
            } else {
                if (onlyAvailable) {
                    list = eventRepository.getAllEventSortViewsPeriodDateParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            startTime,
                            endTime,
                            text,
                            pageable);
                } else {
                    list = eventRepository.getAllEventSortViewsNoLimitPeriodDateParameters(
                            State.PUBLISHED,
                            Arrays.asList(categories),
                            paid,
                            startTime,
                            endTime,
                            text,
                            pageable);
                }
            }
        }
        EndpointHitDto endpointHitDto = new EndpointHitDto(null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                timeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        statsClient.addRequest(request.getRemoteAddr(), endpointHitDto);

        if (list.size() == 0) {
            return new ArrayList<>();
        }

        List<EventShortDto> listShortDto = list.stream().map((event) -> EventMapper.toEventShortDto(event))
                .collect(Collectors.toList());
        return listShortDto;
    }

    @Transactional
    @Override
    public EventFullDto getEventByIdAndStatsPublic(HttpServletRequest request, Long Id) {
        Event event = eventRepository.getEventByIdAndState(Id, State.PUBLISHED);
        if (event == null) {
            throw new NotFoundException("The required object was not found.");
        }


        LocalDateTime timeNow = LocalDateTime.now();
        EndpointHitDto endpointHitDto = new EndpointHitDto(null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                timeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        statsClient.addRequest(request.getRemoteAddr(), endpointHitDto);

        event.setViews(event.getViews() + 1);
        eventRepository.save(event);

        return EventMapper.toEventFullDto(event);
    }

}