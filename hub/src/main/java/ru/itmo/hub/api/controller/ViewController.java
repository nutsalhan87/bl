package ru.itmo.hub.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.hub.service.VideoService;

@RestController
@RequiredArgsConstructor
public class ViewController {
    private final VideoService videoService;

    @PostMapping("/watch/{videoId}")
    public ResponseEntity<Void> viewVideo(@PathVariable Long videoId) {
        videoService.viewVideo(videoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
