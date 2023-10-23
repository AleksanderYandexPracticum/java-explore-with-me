package ru.practicum.main.event.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.user.model.User;


@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CommentDto {

    private Long id;
    private String text;
    private Event event;
    private User author;
    private String created;
}
