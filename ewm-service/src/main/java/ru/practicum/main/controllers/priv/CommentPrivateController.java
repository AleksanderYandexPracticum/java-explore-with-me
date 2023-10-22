package ru.practicum.main.controllers.priv;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.service.CommentService;
import ru.practicum.main.event.service.CommentServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/users")
@Validated
public class CommentPrivateController {

    private final CommentService commentService;

    @Autowired
    public CommentPrivateController(CommentServiceImpl commentServiceImpl) {
        this.commentService = commentServiceImpl;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users/{userId}/events/{eventId}")
    public CommentDto addComment(HttpServletRequest request,
                                 @Positive @PathVariable Long userId,
                                 @Positive @PathVariable Long eventId,
                                 @NonNull @RequestBody CommentDto commentDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        return commentService.addCommentPrivate(userId, eventId, commentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/comment/{commentId}")
    public void deleteUser(HttpServletRequest request,
                           @NonNull @Positive @PathVariable("Id") Long commentId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        commentService.deleteCommentPrivate(commentId);

    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public CommentDto updateCommentPrivate(HttpServletRequest request,
                                           @Positive @PathVariable Long userId,
                                           @Positive @PathVariable Long eventId,
                                           @Valid @RequestBody CommentDto commentDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Update request status with userId={} and eventId={}", userId, eventId);
        return commentService.updateCommentPrivate(eventId, userId, commentDto);
    }

    @GetMapping
    public List<CommentDto> getComments(HttpServletRequest request,
                                        @RequestParam(required = false) Long eventId,
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                        @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get user with userId={}, from={}, size={}", eventId, from, size);
        return commentService.getCommentsPrivate(eventId, from, size);
    }
}