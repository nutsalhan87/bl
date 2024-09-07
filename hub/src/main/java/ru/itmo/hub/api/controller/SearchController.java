package ru.itmo.hub.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.itmo.hub.api.dto.TagDto;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.api.dto.TagSuggestionDto;
import ru.itmo.hub.core.model.primary.tag.Tag;
import ru.itmo.hub.service.TagService;
import ru.itmo.hub.service.TagSuggestionService;
import ru.itmo.hub.service.VideoService;
import ru.itmo.hub.util.ValidationUtils;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("search")
@RequiredArgsConstructor
public class SearchController {
    private final TagService tagService;
    private final VideoService videoService;
    private final TagSuggestionService tagSuggestionService;

    @GetMapping("/tag/list")
    public ResponseEntity<Set<Tag>> listTags() {
        return new ResponseEntity<>(tagService.findAll(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('user')")
    @PostMapping("/tag/suggest")
    public ResponseEntity<Void> suggestTag(@RequestBody TagSuggestionDto suggestionDto) {
        tagSuggestionService.createSuggestion(suggestionDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('moderator')")
    @PostMapping("/tag/create")
    public ResponseEntity<String> createTag(@RequestBody TagDto tagDto) {
        ValidationUtils.validateTagDto(tagDto);
        return new ResponseEntity<>(tagService.save(tagDto), HttpStatus.CREATED);
    }

    @GetMapping("/video/list")
    public ResponseEntity<Set<VideoDto>> listVideos() {
        return new ResponseEntity<>(videoService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/video/filter")
    public ResponseEntity<List<VideoDto>> filterVideos(
            @RequestBody Set<TagDto> preferredTagDtos,
            @RequestBody Set<TagDto> bannedTagDtos
    ) {
        return new ResponseEntity<>(videoService.filter(preferredTagDtos, bannedTagDtos), HttpStatus.OK);
    }

    @GetMapping("/most-liked")
    public ResponseEntity<List<VideoDto>> getMostPopularVideos(){
        return new ResponseEntity<>(videoService.getMostPopularVideos(), HttpStatus.OK);
    }

    @GetMapping("/most-viewed")
    public ResponseEntity<List<VideoDto>> getMostViewedVideos(){
        return new ResponseEntity<>(videoService.getMostViewedVideos(), HttpStatus.OK);
    }
}
