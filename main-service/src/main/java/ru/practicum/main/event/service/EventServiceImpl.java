package ru.practicum.main.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import ru.practicum.ewm.client.stats.stats.StatsClient;
import ru.practicum.ewm.dto.stats.statsDto.EndpointHitDto;
import ru.practicum.ewm.dto.stats.statsDto.ViewStats;
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
import ru.practicum.main.event.model.Status;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.EventDateException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.exception.OverflowLimitException;
import ru.practicum.main.exception.StateArgumentException;
import ru.practicum.main.exception.StatusPerticipationRequestException;
import ru.practicum.main.location.model.Location;
import ru.practicum.main.location.repository.LocationRepository;
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
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.main.event.service.EventSpecifications.isCategory;
import static ru.practicum.main.event.service.EventSpecifications.isOnlyAvailableEqualsParticipantLimitAndConfirmedRequest;
import static ru.practicum.main.event.service.EventSpecifications.isOnlyAvailableParticipantLimitGreaterConfirmedRequest;
import static ru.practicum.main.event.service.EventSpecifications.isPaid;
import static ru.practicum.main.event.service.EventSpecifications.isSort;
import static ru.practicum.main.event.service.EventSpecifications.isTextAnnotation;
import static ru.practicum.main.event.service.EventSpecifications.isTextDescription;
import static ru.practicum.main.event.service.EventSpecifications.periodTimeEnd;
import static ru.practicum.main.event.service.EventSpecifications.periodTimeStart;


@Slf4j
@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final LocationRepository locationRepository;
    private final StatsClient statsClient;

    @Autowired
    public EventServiceImpl(EventRepository eventRepository,
                            CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            ParticipationRepository participationRepository,
                            LocationRepository locationRepository,
                            StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.locationRepository = locationRepository;

        this.statsClient = statsClient;
    }

    @Transactional
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
            throw new IllegalArgumentException("Incorrectly  time");
        }
        Location location = locationRepository.save(newEventDto.getLocation());
        newEventDto.setLocation(location);

        Category category = categoryRepository.getById(newEventDto.getCategory());

        User user = userRepository.getUserById(userId);

        Event event = EventMapper.toEvent(newEventDto, user, category);
        event.setEventDate(start);
        event.setConfirmedRequests(0L);
        if (!newEventDto.isPaid() && !newEventDto.isRequestModeration() && newEventDto.getParticipantLimit() == null) {
            event.setPaid(false);
            event.setRequestModeration(true);
            event.setParticipantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit());
        } else {
            event.setPaid(newEventDto.isPaid());
            event.setRequestModeration(newEventDto.isRequestModeration());
            event.setParticipantLimit(newEventDto.getParticipantLimit() == null ? 0 : newEventDto.getParticipantLimit());
        }
        EventFullDto eventFulldto = EventMapper.toEventFullDto(eventRepository.save(event));
        return eventFulldto;
    }

    @Transactional
    @Override
    public EventFullDto getEventPrivate(Long userId, Long eventId) {

        Event event = eventRepository.getEventsByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException("The required object was not found.");
        }
        return EventMapper.toEventFullDto(event);
    }


    @Transactional
    @Override
    public EventFullDto updateEventPrivate(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event oldEvent = eventRepository.getEventsByIdAndInitiatorId(eventId, userId);

        validateUpdateEventPrivate(oldEvent, updateEventUserRequest);

        if (updateEventUserRequest.getLocation() != null) {
            Location location = locationRepository.save(updateEventUserRequest.getLocation());
            updateEventUserRequest.setLocation(location);
        }

        Category newCategory = updateEventUserRequest.getCategory() == null ?
                oldEvent.getCategory() : categoryRepository.getById(updateEventUserRequest.getCategory());

        Event upEvent = oldEvent;
        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals("SEND_TO_REVIEW")) {
                upEvent = EventMapper.toEvent(updateEventUserRequest, oldEvent, newCategory);
                upEvent.setState(State.PENDING);
            }
            if (updateEventUserRequest.getStateAction().equals("CANCEL_REVIEW")) {
                upEvent.setState(State.CANCELED);

            }
        }

        upEvent.setId(eventId);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventRepository.save(upEvent));

        return eventFullDto;
    }

    private void validateUpdateEventPrivate(Event oldEvent, UpdateEventUserRequest updateEventUserRequest) {
        if (oldEvent == null) {
            throw new NotFoundException("The required object was not found.");
        }

        if (oldEvent.getState() != null && oldEvent.getState().equals(State.PUBLISHED)) {
            throw new StateArgumentException("Only pending or canceled events can be changed");
        }

        LocalDateTime start = oldEvent.getEventDate();
        if (updateEventUserRequest.getEventDate() != null) {
            if (LocalDateTime.parse(updateEventUserRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    .isBefore(start.plusHours(2))) {
                throw new IllegalArgumentException("Time start" + start + "before or equals eventDate");
            }
        }
    }

    @Transactional
    @Override
    public List<ParticipationRequestDto> getRequestsEventsUserPrivate(Long userId, Long eventId) {
        List<ParticipationRequestDto> list = participationRepository.getParticipationRequestsByEvent(eventId)
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
        if (Long.valueOf(event.getParticipantLimit()).equals(event.getConfirmedRequests())) {
            throw new OverflowLimitException("The participant limit has been reached");
        }
        Status status = Status.valueOf(eventRequestStatusUpdateRequest.getStatus());

        List<ParticipationRequest> list = participationRepository.getParticipationRequestByIdIn(eventRequestStatusUpdateRequest.getRequestIds());

        List<ParticipationRequest> listPending = new ArrayList<>();
        List<ParticipationRequest> listRejected = new ArrayList<>();
        List<ParticipationRequest> listOld = new ArrayList<>();
        List<ParticipationRequestDto> listDto = new ArrayList<>();
        List<ParticipationRequestDto> listDtoReject = new ArrayList<>();


        if (event.getParticipantLimit() == 0 && !event.isRequestModeration()) {
            return new EventRequestStatusUpdateResult(listDto, listDtoReject);
        } else if (event.getParticipantLimit() > 0 && !event.isRequestModeration()) {
            for (ParticipationRequest participationRequest : list) {
                if (!participationRequest.getStatus().equals(Status.PENDING)) {
                    throw new StatusPerticipationRequestException("The status Request NOT PENDING");
                }
                if (status.equals(Status.CONFIRMED)) {
                    listOld.add(participationRequest);

                    participationRequest.setStatus(Status.CONFIRMED);
                    listPending.add(participationRequest);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    participationRepository.saveAndFlush(participationRequest);

                    if (Long.valueOf(event.getParticipantLimit()).equals(event.getConfirmedRequests())) {
                        list.removeAll(listOld);
                        if (list.size() != 0) {
                            listDto = listPending.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                            listDtoReject = list.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                            return new EventRequestStatusUpdateResult(listDto, listDtoReject);
                        } else {
                            listDto = listPending.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                            return new EventRequestStatusUpdateResult(listDto, new ArrayList<>());
                        }
                    }
                } else {
                    participationRequest.setStatus(Status.REJECTED);
                    listRejected.add(participationRequest);
                    participationRepository.saveAndFlush(participationRequest);
                    listDtoReject = list.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                    return new EventRequestStatusUpdateResult(new ArrayList<>(), listDtoReject);
                }
            }
            listDto = listPending.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
            return new EventRequestStatusUpdateResult(listDto, new ArrayList<>());
        } else if (event.getParticipantLimit() > 0 && event.isRequestModeration()) {
            for (ParticipationRequest participationRequest : list) {
                if (!participationRequest.getStatus().equals(Status.PENDING)) {
                    throw new StatusPerticipationRequestException("The status Request NOT PENDING");
                }
                if (status.equals(Status.CONFIRMED)) {
                    listOld.add(participationRequest);

                    participationRequest.setStatus(Status.CONFIRMED);
                    listPending.add(participationRequest);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    participationRepository.saveAndFlush(participationRequest);

                    if (Long.valueOf(event.getParticipantLimit()).equals(event.getConfirmedRequests())) {
                        list.removeAll(listOld);
                        if (list.size() != 0) {
                            listDto = listPending.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                            listDtoReject = list.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                            return new EventRequestStatusUpdateResult(listDto, listDtoReject);
                        } else {
                            listDto = listPending.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                            return new EventRequestStatusUpdateResult(listDto, new ArrayList<>());
                        }
                    }
                } else {
                    participationRequest.setStatus(Status.REJECTED);
                    listRejected.add(participationRequest);
                    participationRepository.saveAndFlush(participationRequest);
                    listDtoReject = list.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
                    return new EventRequestStatusUpdateResult(new ArrayList<>(), listDtoReject);
                }
            }
        }
        listDto = listPending.stream().map((pr) -> ParticipationMapper.toParticipationRequestDto(pr)).collect(Collectors.toList());
        return new EventRequestStatusUpdateResult(listDto, new ArrayList<>());

    }


    @Transactional
    @Override
    public List<EventFullDto> getEventsAdmin(List<Long> users, List<String> states, List<Long> categories,
                                             String rangeStart, String rangeEnd, Integer from, Integer size) {

        List<EventFullDto> list = null;
        Integer pageNumber = from / size;
        List<State> stateEnum = null;
        if (states != null) {
            stateEnum = states.stream().map((s) -> State.valueOf(s)).collect(Collectors.toList());
        }
        Pageable pageable = PageRequest.of(pageNumber, size);
        if (rangeStart == null && rangeEnd == null) {
            if (users == null && states == null && categories == null) {
                list = eventRepository.findAll(pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());
            } else if (users == null && states == null && categories != null) {
                list = eventRepository.getEventsByCategoryIdIn(categories, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users == null && states != null && categories == null) {
                list = eventRepository.getEventsByStateIn(stateEnum, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states == null && categories == null) {
                list = eventRepository.getEventsByInitiatorIdIn(users, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users == null && states != null && categories != null) {
                list = eventRepository.getEventsByCategoryIdInAndStateIn(categories, stateEnum, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states != null && categories == null) {
                list = eventRepository.getEventsByInitiatorIdInAndStateIn(users, stateEnum, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states == null && categories != null) {
                list = eventRepository.getEventsByInitiatorIdInAndCategoryIdIn(users, categories, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else {
                list = eventRepository.getEventsByInitiatorIdInAndStateInAndCategoryIdIn(users, stateEnum, categories, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());
            }
        } else {
            LocalDateTime start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (start.isAfter(end)) {
                throw new IllegalArgumentException();
            }
            if (users == null && states == null && categories == null) {
                list = eventRepository.getEventsByEventDateAfterAndEventDateBefore(start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users == null && states == null && categories != null) {
                list = eventRepository.getEventsByCategoryIdInAndEventDateAfterAndEventDateBefore(
                        categories, start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users == null && states != null && categories == null) {
                list = eventRepository.getEventsByStateInAndEventDateAfterAndEventDateBefore(
                        stateEnum, start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states == null && categories == null) {
                list = eventRepository.getEventsByInitiatorIdInAndEventDateAfterAndEventDateBefore(
                        users, start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users == null && states != null && categories != null) {
                list = eventRepository.getEventsByStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(
                        stateEnum, categories, start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states != null && categories == null) {
                list = eventRepository.getEventsByInitiatorIdInAndStateInAndEventDateAfterAndEventDateBefore(
                        users, stateEnum, start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states == null && categories != null) {
                list = eventRepository.getEventsByInitiatorIdInAndCategoryIdInAndEventDateAfterAndEventDateBefore(
                        users, categories, start, end, pageable).stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());

            } else if (users != null && states != null && categories != null)
                list = eventRepository.getEventsByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(
                        users, stateEnum, categories, start, end, pageable)
                        .stream()
                        .map((event) -> EventMapper.toEventFullDto(event))
                        .collect(Collectors.toList());
        }

        return list;
    }

    @Transactional
    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event oldEvent = eventRepository.getEventsById(eventId);

        validateUpdateEventAdmin(oldEvent, updateEventAdminRequest);

        if (updateEventAdminRequest.getLocation() != null) {
            Location location = locationRepository.save(updateEventAdminRequest.getLocation());
            updateEventAdminRequest.setLocation(location);
        }

        Category newCategory = updateEventAdminRequest.getCategory() == null ?
                oldEvent.getCategory() : categoryRepository.getById(updateEventAdminRequest.getCategory());

        Event upEvent = oldEvent;
        if (updateEventAdminRequest.getStateAction() != null) {
            if (updateEventAdminRequest.getStateAction().equals("PUBLISH_EVENT")) {
                upEvent = EventMapper.toEvent(updateEventAdminRequest, oldEvent, newCategory);
                upEvent.setPublishedOn(LocalDateTime.now());
                upEvent.setState(State.PUBLISHED);
            }
            if (updateEventAdminRequest.getStateAction().equals("REJECT_EVENT")) {
                upEvent.setState(State.CANCELED);

            }
        }
        upEvent.setId(eventId);

        EventFullDto eventFullDto = EventMapper.toEventFullDto(eventRepository.save(upEvent));
        return eventFullDto;
    }


    private void validateUpdateEventAdmin(Event oldEvent, UpdateEventAdminRequest updateEventAdminRequest) {
        if (oldEvent == null) {
            throw new NotFoundException("The required object was not found.");
        }

        LocalDateTime start = oldEvent.getEventDate();
        if (oldEvent.getPublishedOn() != null && start.isAfter(oldEvent.getPublishedOn().plusHours(1))) {
            throw new EventDateException("Time start" + start + "before eventDate + 1 Hours");
        }
        if (updateEventAdminRequest.getEventDate() != null) {
            LocalDateTime newEventDate = LocalDateTime.parse(updateEventAdminRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime currentTime = LocalDateTime.now();
            if (newEventDate.isBefore(currentTime) || newEventDate.isEqual(currentTime)) {
                throw new IllegalArgumentException("Time start" + start + "before or equals eventDate");
            }
        }

        if (oldEvent.getState() != null && !oldEvent.getState().equals(State.PENDING) && updateEventAdminRequest.getStateAction().equals("PUBLISH_EVENT")) {
            throw new StateArgumentException("Cannot publish the event because it's not in the right state: PUBLISHED OR CANCELED");
        }
        if (oldEvent.getState() != null && oldEvent.getState().equals(State.PUBLISHED) && updateEventAdminRequest.getStateAction().equals("REJECT_EVENT")) {
            throw new StateArgumentException("Cannot reject the event because it's not in the right state: PUBLISHED");
        }
    }

    @Transactional
    @Override
    public List<EventShortDto> getEventsAndStatsPublic(HttpServletRequest request, String
            text, List<Long> categories, boolean paid,
                                                       String rangeStart, String rangeEnd, boolean onlyAvailable,
                                                       String sort, Integer from, Integer size) {
        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        LocalDateTime timeNow = LocalDateTime.now();
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (rangeStart != null && rangeEnd != null) {
            startTime = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Time start" + startTime + " after end " + endTime);
            }
        }


        Specification<Event> specs = Specification.where(EventSpecifications
                .isState(State.PUBLISHED))
                .and(isPaid(paid))
                .and(periodTimeStart(startTime, endTime, timeNow))
                .and(periodTimeEnd(startTime, endTime, timeNow))
                .and(Specification
                        .where(isOnlyAvailableEqualsParticipantLimitAndConfirmedRequest(onlyAvailable))
                        .or(isOnlyAvailableParticipantLimitGreaterConfirmedRequest(onlyAvailable)))
                .and(isSort(sort))
                .and(isCategory(categories))
                .and(Specification
                        .where(isTextAnnotation(text))
                        .or(isTextDescription(text)));


//        List<Event> list = eventRepository.findAll(specs, pageable).getContent();

        List<Event> list;
        if (text != null) {
            text = "%" + text + "%";
        }
        if (rangeStart == null && rangeEnd == null) {
            if (sort != null && sort.equals("EVENT_DATE")) {
                if (onlyAvailable) {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortEventDateAvailableCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsNoPeriodSortEventDateAvailableCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortEventDateAvailableText(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsNoPeriodSortEventDateAvailable(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                pageable);
                    }
                } else {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortEventDateCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsNoPeriodSortEventDateCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortEventDateText(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsNoPeriodSortEventDate(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                pageable);
                    }
                }
            } else if (sort != null && sort.equals("VIEWS")) {
                if (onlyAvailable) {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortViewsAvailableCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsNoPeriodSortViewsAvailableCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortViewsAvailableText(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsNoPeriodSortViewsAvailable(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                pageable);
                    }
                } else {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortViewsCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsNoPeriodSortViewsCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsNoPeriodSortViewsText(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsNoPeriodSortViews(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                pageable);
                    }

                }
            } else {
                if (onlyAvailable) {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsNoPeriodAvailableCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsNoPeriodAvailableCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsNoPeriodAvailableText(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsNoPeriodAvailable(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                pageable);
                    }
                } else {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsNoPeriodCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                text,
                                pageable);
                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsNoPeriodCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                timeNow,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsNoPeriodText(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                text,
                                pageable);

                    } else {
                        list = eventRepository.getEventsNoPeriod(
                                State.PUBLISHED.toString(),
                                paid,
                                timeNow,
                                pageable);
                    }
                }
            }

        } else {
//            LocalDateTime startTime = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//            LocalDateTime endTime = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (startTime.isAfter(endTime)) {
                throw new IllegalArgumentException("Time start" + startTime + " after end " + endTime);
            }
            if (sort != null && sort.equals("EVENT_DATE")) {
                if (onlyAvailable) {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsPeriodSortEventDateAvailableCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsPeriodSortEventDateAvailableCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsPeriodSortEventDateAvailableText(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsPeriodSortEventDateAvailable(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                pageable);
                    }
                } else {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsPeriodSortEventDateCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsPeriodSortEventDateCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsPeriodSortEventDateText(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsPeriodSortEventDate(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                pageable);
                    }
                }
            } else if (sort != null && sort.equals("VIEWS")) {
                if (onlyAvailable) {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsPeriodSortViewsAvailableCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsPeriodSortViewsAvailableCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsPeriodSortViewsAvailableText(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsPeriodSortViewsAvailable(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                pageable);
                    }
                } else {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsPeriodSortViewsCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsPeriodSortViewsCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsPeriodSortViewsText(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsPeriodSortViews(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                pageable);
                    }
                }
            } else {
                if (onlyAvailable) {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsPeriodAvailableCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsPeriodAvailableCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsPeriodAvailableText(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);
                    } else {
                        list = eventRepository.getEventsPeriodAvailable(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                pageable);
                    }

                } else {
                    if (categories != null && text != null) {
                        list = eventRepository.getEventsPeriodCategoryText(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else if (text == null && categories != null) {
                        list = eventRepository.getEventsPeriodCategory(
                                State.PUBLISHED.toString(),
                                categories,
                                paid,
                                startTime,
                                endTime,
                                pageable);

                    } else if (categories == null && text != null) {
                        list = eventRepository.getEventsPeriodText(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                text,
                                pageable);

                    } else {
                        list = eventRepository.getEventsPeriod(
                                State.PUBLISHED.toString(),
                                paid,
                                startTime,
                                endTime,
                                pageable);
                    }
                }
            }
        }


        EndpointHitDto endpointHitDto = new EndpointHitDto(null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                timeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        try {
            statsClient.addRequest(request.getRemoteAddr(), endpointHitDto);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }

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
        String timeStart = event.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String[] uris = {request.getRequestURI()};

        ResponseEntity<Object> response = statsClient.getStats(request.getRequestURI(), timeStart, timeNow, uris, true);
        List<ViewStats> resp = (List<ViewStats>) response.getBody();
        if (resp.size() == 0) {
            event.setViews(event.getViews() + 1);
            eventRepository.save(event);
        }

        EndpointHitDto endpointHitDto = new EndpointHitDto(null,
                "main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                timeNow);

        statsClient.addRequest(request.getRemoteAddr(), endpointHitDto);

        return EventMapper.toEventFullDto(event);
    }
}