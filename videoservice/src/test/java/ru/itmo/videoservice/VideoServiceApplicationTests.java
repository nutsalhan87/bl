package ru.itmo.videoservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.itmo.shared.Video;
import ru.itmo.videoservice.repository.VideoRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class VideoServiceApplicationTests {
    @Autowired
    private VideoRepository videoRepository;

    @Test
    void repository() {
        var id = videoRepository.generateId();
        var video = Video.builder().
                id(id).
                name("Aboba " + id).
                description("((" + id).
                authorName("Vadim " + id).
                views(1000L + id).
                posted(Timestamp.from(Instant.now())).
                tagIds(LongStream.range(0, id).boxed().collect(Collectors.toSet())).
                reactionIds(LongStream.range(1, id + 1).boxed().collect(Collectors.toSet())).
                build();
        videoRepository.save(video);
        var actual = videoRepository.findById(id).orElseThrow();
        assertEquals(video.getId(), actual.getId());
        assertEquals(video.getName(), actual.getName());
        assertEquals(video.getDescription(), actual.getDescription());
        assertEquals(video.getAuthorName(), actual.getAuthorName());
        assertEquals(video.getViews(), actual.getViews());
        assertEquals(video.getPosted(), actual.getPosted());
        assertArrayEquals(video.getTagIds().toArray(new Long[0]), actual.getTagIds().toArray(new Long[0]));
        assertArrayEquals(video.getReactionIds().toArray(new Long[0]), actual.getReactionIds().toArray(new Long[0]));
    }
}
