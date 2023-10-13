package ru.practicum.main.controllers.pub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.service.CompilationService;
import ru.practicum.main.compilation.service.CompilationServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/compilations")
@Validated
public class PublicCompilationController {

    private final CompilationService compilationService;

    @Autowired
    public PublicCompilationController(CompilationServiceImpl publicCompilationServiceImpl) {
        this.compilationService = publicCompilationServiceImpl;
    }

    @GetMapping()
    public List<CompilationDto> getCompilationsPublic(HttpServletRequest request,
                                                      @NotNull @RequestParam(name = "pinned") boolean pinned,
                                                      @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                      @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get Compilations with pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getCompilationsPublic(pinned, from, size);
    }

    @GetMapping("/{compId}")

    public CompilationDto getCompilationByIdPublic(HttpServletRequest request,
                                                   @Positive @PathVariable Long compId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get Compilation with compId={}", compId);
        return compilationService.getCompilationByIdPublic(compId);
    }

}