package ru.practicum.main.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.compilation.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    List<Compilation> getCompilationByPinnedIs(boolean pinned, Pageable pageable);

    Compilation getCompilationById(Long compId);

    Compilation removeCompilationById(Long compId);
}