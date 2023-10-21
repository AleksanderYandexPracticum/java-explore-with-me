package ru.practicum.main.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.event.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}