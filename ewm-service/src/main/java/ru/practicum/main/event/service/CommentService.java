package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addCommentPrivate(Long userId, Long eventId, CommentDto commentDto);

    void deleteCommentPrivate(Long commentId);

    CommentDto updateCommentPrivate(Long eventId, Long userId, CommentDto commentDto);

    List<CommentDto> getCommentsPrivate(Long eventId, Integer from, Integer size);
}
