package ru.practicum.main.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.location.model.Location;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class UpdateEventUserRequest {
    @NotBlank
    @Size(min = 20, message = "{validation.annotation.size.too_short}")
    @Size(max = 2000, message = "{validation.annotation.size.too_long}")
    private String annotation;

    @Positive
    private Long category;

    @NotBlank
    @Size(min = 20, message = "{validation.description.size.too_short}")
    @Size(max = 7000, message = "{validation.description.size.too_long}")
    private String description;

    @NotBlank
    private String eventDate;


    private Location location;


    private boolean paid;

    @Min(0)
    private Integer participantLimit;


    private boolean requestModeration;

    @NotBlank
    private String stateAction;


    @NotBlank
    @Size(min = 3, message = "{validation.title.size.too_short}")
    @Size(max = 120, message = "{validation.title.size.too_long}")
    private String title;
}