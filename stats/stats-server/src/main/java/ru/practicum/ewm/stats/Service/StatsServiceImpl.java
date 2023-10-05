package ru.practicum.ewm.stats.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.stats.statsDto.EndpointHitDto;
import ru.practicum.ewm.dto.stats.statsDto.ViewStats;
import ru.practicum.ewm.stats.StatsMapper;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Autowired
    public StatsServiceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Transactional
    @Override
    public EndpointHitDto addRequest(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = StatsMapper.toEndpointHit(endpointHitDto);

        return StatsMapper.toEndpointHitDto(statsRepository.save(endpointHit));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ViewStats> getStats(String start, String end, String[] uris, boolean unique) {

        LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<ViewStats> list = null;

        if (unique == false) { //Нужно ли учитывать только уникальные посещения (только с уникальным ip)
            if (uris != null) {
                list = statsRepository.requestStats(startTime, endTime, uris);
            } else {
                list = statsRepository.requestAllStats(startTime, endTime);
            }
        } else {
            if (uris != null) {
                list = statsRepository.requestUniqueIpStats(startTime, endTime, uris);
            } else {
                list = statsRepository.requestUniqueIpAllStats(startTime, endTime);
            }
        }
        return list;
    }

}