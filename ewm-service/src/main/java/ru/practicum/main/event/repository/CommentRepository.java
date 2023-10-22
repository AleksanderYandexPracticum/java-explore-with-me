package ru.practicum.main.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.event.model.Comment;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> getCommentByEventId(Long eventId, Pageable pageable);

    Comment getCommentById(Long commentId);

}