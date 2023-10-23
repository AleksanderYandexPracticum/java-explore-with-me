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
    @PostMapping("/{userId}/events/{eventId}/comments")
    public CommentDto addComment(HttpServletRequest request,
                                 @Positive @PathVariable("userId") Long userId,
                                 @Positive @PathVariable("eventId") Long eventId,
                                 @NonNull @RequestBody CommentDto commentDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        return commentService.addCommentPrivate(userId, eventId, commentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}/comments/{commentId}")
    public void deleteUser(HttpServletRequest request,
                           @Positive @PathVariable Long userId,
                           @Positive @PathVariable Long commentId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());

        commentService.deleteCommentByIdPrivate(userId, commentId);

    }

    @PatchMapping("/{userId}/events/{eventId}/comments/{commentId}")
    public CommentDto updateCommentPrivate(HttpServletRequest request,
                                           @Positive @PathVariable Long userId,
                                           @Positive @PathVariable Long eventId,
                                           @Positive @PathVariable Long commentId,
                                           @Valid @RequestBody CommentDto commentDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Update comment status with userId={} and eventId={}", userId, eventId);
        return commentService.updateCommentPrivate(userId, eventId, commentId, commentDto);
    }

    @GetMapping("/{eventId}")
    public List<CommentDto> getComments(HttpServletRequest request,
                                        @PathVariable Long eventId,
                                        @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                        @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get comments by event eventId={}, from={}, size={}", eventId, from, size);
        return commentService.getCommentsPrivate(eventId, from, size);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto getCommentById(HttpServletRequest request,
                                     @Positive @PathVariable Long commentId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Get comment by Id ={}", commentId);
        return commentService.getCommentByIdPrivate(commentId);
    }
}