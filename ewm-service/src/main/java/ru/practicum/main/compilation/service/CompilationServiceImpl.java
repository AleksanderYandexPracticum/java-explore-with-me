package ru.practicum.main.compilation.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.main.compilation.CompilationMapper;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.model.Compilation;
import ru.practicum.main.compilation.repository.CompilationRepository;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.DuplicateNameException;
import ru.practicum.main.exception.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Autowired
    public CompilationServiceImpl(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public List<CompilationDto> getCompilationsPublic(Boolean pinned, Integer from, Integer size) {

        Integer pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);
        if (pinned != null) {
            return compilationRepository.getCompilationByPinnedIs(pinned, pageable).stream()
                    .map((compilation) -> CompilationMapper.toCompilationDto(compilation))
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAll(pageable).stream()
                .map((compilation) -> CompilationMapper.toCompilationDto(compilation))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CompilationDto getCompilationByIdPublic(Long compId) {
        CompilationDto compilationDto = CompilationMapper
                .toCompilationDto(compilationRepository.getCompilationById(compId));
        if (compilationDto == null) {
            throw new NotFoundException("The required object was not found.");
        }
        return compilationDto;
    }


    @Transactional
    @Override
    public CompilationDto addCompilationAdmin(NewCompilationDto newCompilationDto) {
        Set<Event> listEvent = new HashSet<>();
        if (newCompilationDto.getEvents() != null && newCompilationDto.getEvents().size() != 0) {
            listEvent = eventRepository.getEventsByIdIn(newCompilationDto.getEvents());
        }

        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, listEvent);
        CompilationDto compilationDto;
        try {
            compilationDto = CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        } catch (DataIntegrityViolationException e) {
            log.info("Duplicate of the compilation title");
            throw new DuplicateNameException("Duplicate of the compilation title");
        }
        return compilationDto;
    }


    @Transactional
    @Override
    public void deleteCompilationByIdAdmin(Long compId) {
        if (compilationRepository.getCompilationById(compId) == null) {
            throw new NotFoundException("The required object was not found.");
        }
        compilationRepository.removeCompilationById(compId);
    }


    @Transactional
    @Override
    public CompilationDto updateCompilationByIdAdmin(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation oldCompilation = compilationRepository.getCompilationById(compId);
        if (oldCompilation == null) {
            throw new NotFoundException("The required object was not found.");
        }
        Set<Event> listEvent = new HashSet<>();
        if (updateCompilationRequest.getEvents() != null) {
            listEvent = eventRepository.getEventsByIdIn(updateCompilationRequest.getEvents());
        }
        Compilation compilation = CompilationMapper.toCompilation(updateCompilationRequest, listEvent);
        compilation.setTitle(updateCompilationRequest.getTitle() == null ? oldCompilation.getTitle() : updateCompilationRequest.getTitle());
        compilation.setId(compId);


        CompilationDto compilationDto = CompilationMapper.toCompilationDto(compilationRepository.save(compilation));

        return compilationDto;
    }
}