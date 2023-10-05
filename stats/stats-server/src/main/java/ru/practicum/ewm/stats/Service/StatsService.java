package ru.practicum.ewm.stats.Service;

import ru.practicum.ewm.dto.stats.statsDto.EndpointHitDto;
import ru.practicum.ewm.dto.stats.statsDto.ViewStats;

import java.util.List;

public interface StatsService {

    EndpointHitDto addRequest(EndpointHitDto endpointHitDto);

    List<ViewStats> getStats(String start, String end, String[] uris, boolean unique);
}
