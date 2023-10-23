package ru.practicum.main.controllers.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.event.dto.CommentDto;
import ru.practicum.main.event.service.CommentService;
import ru.practicum.main.event.service.CommentServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@Validated
public class CommentAdminController {

    private final CommentService commentService;

    @Autowired
    public CommentAdminController(CommentServiceImpl commentServiceImpl) {
        this.commentService = commentServiceImpl;
    }


    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}/comment/{commentId}")
    public void deleteUserAdmin(HttpServletRequest request,
                                @Positive @PathVariable Long userId,
                                @Positive @PathVariable Long commentId) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());

        commentService.deleteCommentByIdAdmin(userId, commentId);

    }

    @PatchMapping("/{userId}/events/{eventId}")
    public CommentDto updateCommentAdmin(HttpServletRequest request,
                                         @Positive @PathVariable Long userId,
                                         @Positive @PathVariable Long eventId,
                                         @Valid @RequestBody CommentDto commentDto) {
        log.info("Request to the endpoint was received: '{} {}', string of request parameters: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        log.info("Update comment status with userId={} and eventId={}", userId, eventId);
        return commentService.updateCommentAdmin(userId, eventId, commentDto);
    }
}