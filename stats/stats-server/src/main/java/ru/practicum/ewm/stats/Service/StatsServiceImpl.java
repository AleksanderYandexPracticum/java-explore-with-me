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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        try {
            startTime = LocalDateTime.parse(URLDecoder.decode(start, StandardCharsets.UTF_8.toString()), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            endTime = LocalDateTime.parse(URLDecoder.decode(end, StandardCharsets.UTF_8.toString()), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End before start");
        }

        List<ViewStats> list = null;

        if (!unique) { //Нужно ли учитывать только уникальные посещения (только с уникальным ip)
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