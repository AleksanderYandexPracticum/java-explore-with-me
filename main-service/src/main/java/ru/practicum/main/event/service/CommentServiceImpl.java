package ru.practicum.main.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.CommentRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.repository.UserRepository;

import javax.transaction.Transactional;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService{
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public CommentDto addCommentPrivate(Long userId, Long eventId, CommentDto commentDto) {
        validateIdUser(userId);
        validateIdEvent(eventId);
        Event event = eventRepository.getEventsById(eventId);
        return commentDto;
    }

    private void validateIdUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.info("There is no such owner ID");
            throw new NotFoundException(String.format("There is no such user ID № %s", userId));
        }
    }

    private void validateIdEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            log.info("There is no such identifier");
            throw new NotFoundException(String.format("There is no such identifier № %s", eventId));
        }
    }

    @Transactional
    @Override
    public void deleteCommentPrivate(Long commentId) {
    }

}
