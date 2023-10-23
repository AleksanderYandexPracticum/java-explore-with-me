package ru.practicum.main.event.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.main.event.CommentMapper;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.model.Comment;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.CommentRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl implements CommentService {
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
        User user = userRepository.getUserById(userId);

        Comment comment = CommentMapper.toComment(commentDto, user, event);

        commentDto = CommentMapper.toCommentDto(commentRepository.save(comment));
        return commentDto;
    }

    @Transactional
    @Override
    public void deleteCommentPrivate(Long commentId) {
        if (commentRepository.findById(commentId) == null) {
            throw new NotFoundException("The required object was not found.");
        }
        commentRepository.deleteById(commentId);
    }

    @Transactional
    @Override
    public CommentDto updateCommentPrivate(Long eventId, Long userId, CommentDto commentDto) {
        validateIdEventAndIdUser(commentDto.getId(), userId);
        Comment oldComment = commentRepository.getCommentById(commentDto.getId());

        Event event = commentDto.getEvent() == null ? eventRepository.getEventsById(eventId) : commentDto.getEvent();
        User user = commentDto.getAuthor() == null ? userRepository.getUserById(userId) : commentDto.getAuthor();

        Comment upComment = CommentMapper.toComment(commentDto, user, event);

        upComment.setId(oldComment.getId());
        upComment.setText((upComment.getText() == null || upComment.getText().isBlank()) ? oldComment.getText() : upComment.getText());
        upComment.setCreated(upComment.getCreated() == null ? oldComment.getCreated() : upComment.getCreated());

        return CommentMapper.toCommentDto(commentRepository.save(upComment));
    }


    @Transactional
    @Override
    public List<CommentDto> getCommentsPrivate(Long eventId, Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        List<CommentDto> commentDtos = commentRepository.getCommentByEventId(eventId, pageable).stream()
                .map(comment -> CommentMapper.toCommentDto(comment))
                .collect(Collectors.toList());
        if (commentDtos.size() == 0) {
            throw new NotFoundException(String.format("There is no such comment for Event ID № %s", eventId));
        }
        return commentDtos;
    }

    private void validateIdUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("There is no such user ID № %s", userId));
        }
    }

    private void validateIdEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException(String.format("There is no such identifier № %s", eventId));
        }
    }

    private void validateIdEventAndIdUser(Long commentId, Long userId) {
        if (!commentRepository.getCommentById(commentId).getAuthor().getId().equals(userId)) {
            throw new NotFoundException(String.format("Owner  № %s doesn't have an comment with an ID  № %s", userId, commentId));
        }
    }

}