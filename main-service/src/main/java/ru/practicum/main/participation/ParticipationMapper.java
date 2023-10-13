package ru.practicum.main.participation;


import ru.practicum.main.event.model.Status;
import ru.practicum.main.participation.dto.ParticipationRequestDto;
import ru.practicum.main.participation.model.ParticipationRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ParticipationMapper {
    public static ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest) {
        return ParticipationRequestDto.builder()
                .created(participationRequest.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .event(participationRequest.getEvent())
                .requester(participationRequest.getRequester())
                .status(participationRequest.getStatus().toString())
                .build();
    }

    public static ParticipationRequest toParticipationRequest(ParticipationRequestDto participationRequestDto) {
        return ParticipationRequest.builder()
                .created(LocalDateTime.parse(participationRequestDto.getCreated(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .event(participationRequestDto.getEvent())
                .requester(participationRequestDto.getRequester())
                .status(Status.valueOf(participationRequestDto.getStatus()))
                .build();
    }
}