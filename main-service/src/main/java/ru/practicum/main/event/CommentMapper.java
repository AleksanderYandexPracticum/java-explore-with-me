package ru.practicum.main.event;


import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.model.Comment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .event(comment.getEvent())
                .author(comment.getAuthor())
                .created(comment.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    public static Comment toComment(CommentDto commentDto) {
        return Comment.builder()
                .text(commentDto.getText())
                .event(commentDto.getEvent())
                .author(commentDto.getAuthor())
                .created(LocalDateTime.parse(commentDto.getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }
}