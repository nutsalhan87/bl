package ru.itmo.hub.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.itmo.hub.api.dto.CommentDto;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.primary.tagsuggestion.TagSuggestion;
import ru.itmo.hub.service.CommentReportService;
import ru.itmo.hub.service.ReactionService;
import ru.itmo.hub.service.TagSuggestionService;
import ru.itmo.hub.service.VideoReportService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("moderation")
@PreAuthorize("hasRole('moderator')")
@RequiredArgsConstructor
public class ModeratorController {
    private final CommentReportService commentReportService;
    private final VideoReportService videoReportService;
    private final ReactionService reactionService;
    private final TagSuggestionService tagSuggestionService;

    @GetMapping("/comment/list")
    public ResponseEntity<List<CommentDto>> getUncheckedComments() {
        var uncheckedCommentIds = commentReportService.getUncheckedComments();
        return new ResponseEntity<>(uncheckedCommentIds, HttpStatus.OK);
    }

    @PatchMapping("/comment/approve/{commentId}")
    public ResponseEntity<Void> approveComment(@PathVariable Long commentId) {
        commentReportService.approveComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/comment/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentReportService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/video/list")
    public ResponseEntity<List<VideoDto>> getUncheckedVideos() {
        var uncheckedVideoIds = videoReportService.getUncheckedVideos();
        return new ResponseEntity<>(uncheckedVideoIds, HttpStatus.OK);
    }

    @PatchMapping("/video/approve/{videoId}")
    public ResponseEntity<Void> approveVideo(@PathVariable Long videoId) {
        videoReportService.approveVideo(videoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/video/delete/{videoId}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long videoId) {
        videoReportService.deleteVideo(videoId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/video/{videoId}/reset-reactions")
    public ResponseEntity<Void> resetReactions(@PathVariable Long videoId) {
        reactionService.resetReactions(videoId);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/tag-suggest/list")
    public ResponseEntity<Set<TagSuggestion>> listTagSuggestions() {
        return new ResponseEntity<>(tagSuggestionService.findAll(), HttpStatus.OK);
    }

    @PatchMapping("/tag-suggest/accept/{suggestionId}")
    public ResponseEntity<Void> acceptTagSuggestion(@PathVariable long suggestionId) {
        tagSuggestionService.acceptSuggestion(suggestionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/tag-suggest/dismiss/{suggestionId}")
    public ResponseEntity<Void> dismissTagSuggestion(@PathVariable long suggestionId) {
        tagSuggestionService.dismissSuggestion(suggestionId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
