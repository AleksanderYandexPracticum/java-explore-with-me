package ru.practicum.main.event.repository;

import org.springframework.data.domain.Pageable;
import ru.practicum.main.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<Event> searchWithParametrs(String state, List<Long> category, boolean paid, LocalDateTime timeStart, LocalDateTime timeEnd, String text, Pageable pageable);
}
