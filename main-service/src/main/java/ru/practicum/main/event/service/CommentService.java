package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.CommentDto;

public interface CommentService {
    CommentDto addCommentPrivate(Long userId, Long eventId, CommentDto commentDto);

    void deleteCommentPrivate(Long commentId);
}
