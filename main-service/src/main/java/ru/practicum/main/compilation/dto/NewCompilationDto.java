package ru.practicum.main.compilation.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class NewCompilationDto {

    private List<Long> events;
    private boolean pinned;

    @NotBlank
    @Size(min = 1, message = "{validation.title.size.too_short}")
    @Size(max = 50, message = "{validation.title.size.too_long}")
    private String title;
}