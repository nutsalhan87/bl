package ru.itmo.hub.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.primary.playlist.Playlist;
import ru.itmo.hub.service.PlaylistService;
import ru.itmo.hub.util.SecurityUtils;

import java.util.Set;

@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
public class PlaylistController {
    private final PlaylistService playlistService;

    @GetMapping("/list")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Set<Playlist>> getPlaylists() {
        return new ResponseEntity<>(playlistService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{playlistId}/videos")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Set<VideoDto>> getPlaylistVideos(@PathVariable Long playlistId) {
        return new ResponseEntity<>(playlistService.getPlaylistVideos(playlistId), HttpStatus.OK);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('editor')")
    public ResponseEntity<Void> createPlaylist(@RequestBody String name, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        playlistService.createPlaylist(username, name);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{playlistId}/add")
    @PreAuthorize("hasRole('editor')")
    public ResponseEntity<Void> addVideoToPlaylist(@PathVariable Long playlistId, @RequestBody Long videoId, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        playlistService.addVideoToPlaylist(username, playlistId, videoId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{playlistId}/remove")
    @PreAuthorize("hasRole('editor')")
    public ResponseEntity<Void> removeVideoFromPlaylist(@PathVariable Long playlistId, @RequestBody Long videoId, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        playlistService.deleteVideoFromPlaylist(username, playlistId, videoId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('editor')")
    public ResponseEntity<Void> deletePlaylist(@RequestBody Long playlistId, Authentication authentication) {
        var username = SecurityUtils.getUsername(authentication);
        playlistService.deletePlaylist(username, playlistId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}