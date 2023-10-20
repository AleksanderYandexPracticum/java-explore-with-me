package ru.practicum.main.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.location.model.Location;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class UpdateEventUserRequest {
    @Size(min = 20, message = "{validation.annotation.size.too_short}")
    @Size(max = 2000, message = "{validation.annotation.size.too_long}")
    private String annotation;

    @Positive
    private Long category;

    @Size(min = 20, message = "{validation.description.size.too_short}")
    @Size(max = 7000, message = "{validation.description.size.too_long}")
    private String description;

    private String eventDate;


    private Location location;


    private boolean paid;

    @Positive
    private Integer participantLimit;


    private boolean requestModeration;

    private String stateAction;


    @Size(min = 3, message = "{validation.title.size.too_short}")
    @Size(max = 120, message = "{validation.title.size.too_long}")
    private String title;
}