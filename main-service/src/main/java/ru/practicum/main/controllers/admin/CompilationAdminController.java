package ru.practicum.main.controllers.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.service.CompilationService;
import ru.practicum.main.compilation.service.CompilationServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@RestController
@RequestMapping(path = "/admin/compilations")
@Validated
public class CompilationAdminController {

    private final CompilationService compilationService;

    @Autowired
    public CompilationAdminController(CompilationServiceImpl compilationServiceImpl) {
        this.compilationService = compilationServiceImpl;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CompilationDto addCompilationAdmin(HttpServletRequest request,
                                              @Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Add Compilation with name={}", newCompilationDto.getTitle());
        return compilationService.addCompilationAdmin(newCompilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{compId}")
    public void deleteCompilationByIdAdmin(HttpServletRequest request,
                                           @Positive @PathVariable("compId") Long compId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        compilationService.deleteCompilationByIdAdmin(compId);
    }

    @PatchMapping("/{compId}")
    public CompilationDto updateCompilationByIdAdmin(HttpServletRequest request,
                                                     @Positive @PathVariable Long compId,
                                                     @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        return compilationService.updateCompilationByIdAdmin(compId, updateCompilationRequest);
    }

}