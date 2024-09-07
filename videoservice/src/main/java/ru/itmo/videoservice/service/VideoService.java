package ru.itmo.videoservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.itmo.shared.ActionResult;
import ru.itmo.shared.Video;
import ru.itmo.videoservice.repository.VideoRepository;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final KafkaTemplate<Long, ActionResult> producer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"findAllVideos", "findVideoById", "viewVideo", "createVideo", "updateVideo", "deleteVideoById"},
            containerFactory = "stringKafkaListenerContainerFactory")
    public void listener(@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) Long transactionId,
                         @Header(value = "returnTopic", required = false) String returnTopic,
                         @Payload String body) {
        log.info("Received message to topic " + topic + " with key=" + transactionId + " and body=" + body + ". Return topic is " + returnTopic);
        ActionResult actionResult;
        try {
            var result = switch (topic) {
                case "findAllVideos" -> findAll();
                case "findVideoById" -> {
                    var videoId = Long.parseLong(body);
                    yield findById(videoId);
                }
                case "viewVideo" -> {
                    var videoId = Long.parseLong(body);
                    viewVideo(videoId);
                    yield null;
                }
                case "createVideo" -> {
                    var video = objectMapper.readValue(body, Video.class);
                    yield createVideo(video);
                }
                case "updateVideo" -> {
                    var video = objectMapper.readValue(body, Video.class);
                    yield updateVideo(video);
                }
                case "deleteVideoById" -> {
                    var videoId = Long.parseLong(body);
                    deleteVideoById(videoId);
                    yield null;
                }
                default -> throw new IllegalStateException("Unexpected topic: " + topic);
            };
            var json = objectMapper.writeValueAsString(result);
            actionResult = new ActionResult.Ok(json);
        } catch (Throwable t) {
            actionResult = new ActionResult.Fail(t.getMessage());
        }

        if (returnTopic != null) {
            producer.send(returnTopic, transactionId, actionResult);
        }
    }

    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    public Video findById(Long id) {
        return videoRepository.findById(id).orElseThrow(() -> new RuntimeException("Video with id " + id + " is not found"));
    }

    public void viewVideo(Long id) {
        Video video = videoRepository.findById(id).orElseThrow();
        video.setViews(video.getViews() + 1);

        videoRepository.save(video);
    }

    public Long createVideo(Video video) {
        return videoRepository.save(video);
    }

    public Video updateVideo(Video video) {
        var existingVideo = findById(video.getId());
        if (video.getName() != null) existingVideo.setName(video.getName());
        if (video.getDescription() != null) existingVideo.setDescription(video.getDescription());
        if (video.getAuthorName() != null)
            existingVideo.setAuthorName(video.getAuthorName()); // передача прав другому пользователю?
        if (video.getTagIds() != null) existingVideo.setTagIds(video.getTagIds());
        if (video.getReactionIds() != null) existingVideo.setReactionIds(video.getReactionIds());
        videoRepository.save(existingVideo);
        return existingVideo;
    }

    public void deleteVideoById(Long videoId) {
        videoRepository.delete(videoId);
    }
}
