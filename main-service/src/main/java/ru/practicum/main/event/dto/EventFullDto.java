package ru.practicum.main.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.user.dto.UserShortDto;


@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class EventFullDto {

    private Long id;
    private String title;
    private String annotation;
    private CategoryDto category;
    private boolean paid;
    private String eventDate;
    private UserShortDto initiator;
    private Long confirmedRequests;
    private String description;
    private Integer participantLimit;
    private String state;
    private String createdOn;
    private LocationDto location;
    private boolean requestModeration;
    private String publishedOn;
    private Long views;
}