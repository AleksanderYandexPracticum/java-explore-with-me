package ru.practicum.main.event.service;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

public class EventSpecifications {
    public static Specification<Event> isState(State state) {
        return (root, query, cb) -> cb.equal(root.get("state"), state);
    }

    public static Specification<Event> isPaid(boolean paid) {
        return (root, query, cb) -> cb.equal(root.get("paid"), paid);
    }

    public static Specification<Event> periodTimeStart(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime timeNow) {
        if (startTime == null || endTime == null) {
            return (root, query, cb) -> cb.greaterThan(root.<LocalDateTime>get("eventDate").as(LocalDateTime.class), timeNow);
        }
        return (root, query, cb) -> {
            return cb.and(cb.greaterThan(root.<LocalDateTime>get("eventDate").as(LocalDateTime.class), startTime));
        };
    }

    public static Specification<Event> periodTimeEnd(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime timeNow) {
        if (startTime == null || endTime == null) {
            return (root, query, cb) -> cb.greaterThan(root.<LocalDateTime>get("eventDate").as(LocalDateTime.class), timeNow);
        }
        return (root, query, cb) -> {
            return cb.and(cb.lessThan(root.<LocalDateTime>get("eventDate").as(LocalDateTime.class), endTime));//cb.literal(endTime)
        };
    }

    public static Specification<Event> betweenTimeEnd(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime timeNow) {
        if (startTime == null || endTime == null) {
            return (root, query, cb) -> cb.greaterThan(root.get("eventDate"), timeNow);
        }
        return (root, query, cb) -> cb.between(root.get("eventDate"), startTime, endTime);
    }

    public static Specification<Event> isOnlyAvailableEqualsParticipantLimitAndConfirmedRequest(boolean onlyAvailable) {
        if (!onlyAvailable) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("participantLimit"), 0);
    }

    public static Specification<Event> isOnlyAvailableParticipantLimitGreaterConfirmedRequest(boolean onlyAvailable) {
        if (!onlyAvailable) {
            return null;
        }
        return (root, query, cb) -> cb.greaterThan(root.get("participantLimit"), root.get("confirmedRequests"));
    }

    public static Specification<Event> isSort(String sort) {
        if (sort == null) {
            return null;
        }
        if (sort.equals("VIEWS")) {
            return (root, query, cb) -> cb.like(root.get("views"), sort);
        }
        return (root, query, cb) -> cb.like(root.get("eventDate"), sort);
    }

    public static Specification<Event> isCategory(List<Long> categories) {
        if (categories == null) {
            return null;
        }
        return (root, query, cb) -> root.get("category").in(categories);
    }

    public static Specification<Event> isTextAnnotation(String text) {
        if (text == null) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("annotation")), ("%" + text + "%").toLowerCase());
    }

    public static Specification<Event> isTextDescription(String text) {
        if (text == null) {
            return null;
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("description")), ("%" + text + "%").toLowerCase());
    }
}