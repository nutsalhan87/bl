package ru.itmo.hub.api.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.itmo.hub.api.dto.VideoCreationAddingToPlaylistDto;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.service.PlaylistService;
import ru.itmo.hub.service.VideoReportService;
import ru.itmo.hub.service.VideoService;
import ru.itmo.hub.util.SecurityUtils;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("video")
@PreAuthorize("hasRole('user')")
public class VideoController {
    private final VideoService videoService;
    private final PlaylistService playlistService;
    private final VideoReportService videoReportService;

    @PostMapping
    public ResponseEntity<Void> createVideo(@RequestBody VideoDto videoDto, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        log.debug("POST request received to create a new video {}", videoDto);
        videoService.createVideo(videoDto, username);

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/playlist")
    public ResponseEntity<Void> createVideoAddToPlaylist(@RequestBody VideoCreationAddingToPlaylistDto dto, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        playlistService.createVideoAddToPlaylist(username, dto);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PatchMapping("/{videoId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> updateVideo(@PathVariable(name = "videoId") Long videoId,
                                @RequestBody VideoDto videoDto, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        log.debug("PATCH request received to update video {} with id = {}", videoDto, videoId);
        videoService.updateVideo(videoId, videoDto, username);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/{videoId}")
    @PreAuthorize("permitAll()")
    public VideoDto getVideoById(@PathVariable(name = "videoId") @NonNull Long videoId) {
        log.debug("GET request received to get video by id {}", videoId);
        return videoService.findByIdOrThrow(videoId);
    }

    @PutMapping("/report/{videoId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> reportVideo(@PathVariable(name = "videoId") @NonNull Long videoId) {
        log.debug("GET request received to report video by id {}", videoId);
        videoReportService.report(videoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
