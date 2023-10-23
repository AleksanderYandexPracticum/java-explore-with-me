package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addCommentPrivate(Long userId, Long eventId, CommentDto commentDto);

    void deleteCommentByIdPrivate(Long userId, Long commentId);

    CommentDto updateCommentPrivate(Long eventId, Long userId, Long commentId, CommentDto commentDto);

    List<CommentDto> getCommentsPrivate(Long eventId, Integer from, Integer size);

    CommentDto getCommentByIdPrivate(Long commentId);

    void deleteCommentByIdAdmin(Long userId, Long commentId);

    CommentDto updateCommentAdmin(Long userId, Long eventId, CommentDto commentDto);
}