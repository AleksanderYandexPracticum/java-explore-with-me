package ru.practicum.main.event.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CommentDto {

    private Long id;
    private String text;
    private Long event;
    private Long author;
    private String created;
}
