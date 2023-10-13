package ru.practicum.main.event;


import ru.practicum.main.category.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShortDto;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.dto.UpdateEventUserRequest;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;
import ru.practicum.main.user.UserMapper;
import ru.practicum.main.user.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventMapper {
    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .requestModeration(event.isRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .title(event.getTitle())
                .views(event.getViews())
                .build();
    }

    public static Event toEvent(NewEventDto newEventDto, User initiator, Category category) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .category(category)
                .createdOn(LocalDateTime.now())
                .description(newEventDto.getDescription())
                .eventDate(LocalDateTime.parse(newEventDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .initiator(initiator)
                .location(newEventDto.getLocation())
                .paid(newEventDto.isPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.isRequestModeration())
                .state(State.PENDING)
                .title(newEventDto.getTitle())
                .views(0L)
                .build();
    }

    public static Event toEvent(UpdateEventAdminRequest updateEventAdminRequest, Event oldEvent, Category category) {
        return Event.builder()
                .id(oldEvent.getId())
                .annotation(updateEventAdminRequest.getAnnotation())
                .category(category)
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .createdOn(oldEvent.getCreatedOn())
                .description(updateEventAdminRequest.getDescription())
                .eventDate(LocalDateTime.parse(updateEventAdminRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .initiator(oldEvent.getInitiator())
                .location(updateEventAdminRequest.getLocation())
                .paid(updateEventAdminRequest.isPaid())
                .participantLimit(updateEventAdminRequest.getParticipantLimit())
                .requestModeration(updateEventAdminRequest.isRequestModeration())
                .publishedOn(oldEvent.getPublishedOn())
                .state(oldEvent.getState())///////////////////////////////////////////////////
                .title(updateEventAdminRequest.getTitle())
                .views(oldEvent.getViews())
                .build();
    }

    public static Event toEvent(UpdateEventUserRequest updateEventUserRequest, Event oldEvent, Category category) {
        return Event.builder()
                .id(oldEvent.getId())
                .annotation(updateEventUserRequest.getAnnotation())
                .category(category)
                .confirmedRequests(oldEvent.getConfirmedRequests())
                .createdOn(oldEvent.getCreatedOn())
                .description(updateEventUserRequest.getDescription())
                .eventDate(LocalDateTime.parse(updateEventUserRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .initiator(oldEvent.getInitiator())
                .location(updateEventUserRequest.getLocation())
                .paid(updateEventUserRequest.isPaid())
                .participantLimit(updateEventUserRequest.getParticipantLimit())
                .requestModeration(updateEventUserRequest.isRequestModeration())
                .publishedOn(oldEvent.getPublishedOn())
                .state(oldEvent.getState())///////////////////////////////////////////////////
                .title(updateEventUserRequest.getTitle())
                .views(oldEvent.getViews())
                .build();
    }

}