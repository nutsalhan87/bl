package ru.itmo.hub.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.itmo.hub.api.dto.CommentDto;
import ru.itmo.hub.api.dto.CommentSortingStrategy;
import ru.itmo.hub.service.CommentReportService;
import ru.itmo.hub.service.CommentService;
import ru.itmo.hub.service.ReactionService;
import ru.itmo.hub.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final CommentService commentService;
    private final CommentReportService commentReportService;
    private final ReactionService reactionService;

    @GetMapping("/{videoId}/comment/list")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long videoId,
                                                        @RequestParam(defaultValue = "NEWER") CommentSortingStrategy sorting) {
        var comments = commentService.getComments(videoId, sorting);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @PostMapping(value = "/{videoId}/comment/create")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<Void> createComment(@PathVariable Long videoId, @RequestBody String value, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        commentService.createComment(videoId, value, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/report/comment/{commentId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> reportComment(@PathVariable Long commentId) {
        commentReportService.report(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{videoId}/like")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<Void> likeVideo(@PathVariable Long videoId, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        reactionService.reactVideo(videoId, true, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{videoId}/dislike")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<Void> dislikeVideo(@PathVariable Long videoId, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        reactionService.reactVideo(videoId, false, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{videoId}/unreact")
    @PreAuthorize("hasRole('user')")
    public ResponseEntity<Void> unreactVideo(@PathVariable Long videoId, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        reactionService.unreactVideo(videoId, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
