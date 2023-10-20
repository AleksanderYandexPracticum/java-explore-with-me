package ru.practicum.ewm.stats.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.stats.statsDto.EndpointHitDto;
import ru.practicum.ewm.dto.stats.statsDto.ViewStats;
import ru.practicum.ewm.stats.Service.StatsService;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@RestController
public class StatsServerController {

    private final StatsService statsService;

    @Autowired
    public StatsServerController(StatsService statsService) {
        this.statsService = statsService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/hit")
    public EndpointHitDto addUser(
            @RequestBody EndpointHitDto endpointHitDto) {
        log.info("Creating request {}, app={}, ip={}", endpointHitDto.getApp(), endpointHitDto.getIp());
        return statsService.addRequest(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(@RequestParam(name = "start") String start,
                                    @RequestParam(name = "end") String end,
                                    @RequestParam(required = false, name = "uris") String[] uris,
                                    @RequestParam(name = "unique", defaultValue = "false") boolean unique) throws UnsupportedEncodingException {
        log.info("Get stats");
        return statsService.getStats(start, end, uris, unique);
    }

}
