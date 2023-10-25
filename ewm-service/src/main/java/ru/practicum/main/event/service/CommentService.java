package ru.practicum.main.event.service;

import ru.practicum.main.event.dto.CommentDto;

import java.util.List;

public interface CommentService {
    CommentDto addCommentPrivate(CommentDto commentDto);

    void deleteCommentByIdPrivate(Long userId, Long commentId);

    CommentDto updateCommentPrivate(Long commentId, CommentDto commentDto);

    List<CommentDto> getCommentsPrivate(Long eventId, Integer from, Integer size);

    CommentDto getCommentByIdPrivate(Long commentId);

    void deleteCommentByIdAdmin(Long userId, Long commentId);

    CommentDto updateCommentAdmin(Long commentId, CommentDto commentDto);
}