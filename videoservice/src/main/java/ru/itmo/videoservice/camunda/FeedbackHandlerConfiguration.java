package ru.itmo.videoservice.camunda;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.itmo.shared.HubExternalTaskHandler;
import ru.itmo.videoservice.service.VideoService;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeedbackHandlerConfiguration {
    private final VideoService videoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    @ExternalTaskSubscription(value = "delete-reaction-from-video", variableNames = {"videoId", "reactionId"})
    public ExternalTaskHandler deleteReactionFromVideoHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start delete-reaction-from-video topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            Long reactionId = helper.getVarOrThrow("reactionId");
            var video = videoService.findById(videoId); // or throw
            video.getReactionIds().remove(reactionId);
            videoService.updateVideo(video);
            externalTaskService.complete(externalTask);
        }, Optional.empty());
    }

    @Bean
    @ExternalTaskSubscription(value = "add-reaction-to-video", variableNames = {"videoId", "reactionId"})
    public ExternalTaskHandler addReactionToVideoHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start add-reaction-to-video topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            Long reactionId = helper.getVarOrThrow("reactionId");
            var video = videoService.findById(videoId); // or throw
            video.getReactionIds().add(reactionId);
            videoService.updateVideo(video);
            externalTaskService.complete(externalTask);
        }, Optional.empty());
    }

    @Bean
    @ExternalTaskSubscription(value = "reset-reactions-on-video", variableNames = {"videoId"})
    public ExternalTaskHandler resetReactionsOnVideoHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start reset-reactions-on-video topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            var video = videoService.findById(videoId); // or throw
            var reactionIds = video.getReactionIds();
            try {
                var serializedReactionIds = objectMapper.writeValueAsString(reactionIds);
                reactionIds.clear();
                videoService.updateVideo(video);
                helper.completeWithVar("reactionIds", serializedReactionIds);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize reaction IDs.");
            }
        }, Optional.empty());
    }

    @Bean
    @ExternalTaskSubscription(value = "restore-reactions-on-video", variableNames = {"videoId", "reactionIds"})
    public ExternalTaskHandler restoreReactionsOnVideoHandler() {
        return new HubExternalTaskHandler((externalTask, externalTaskService, helper) -> {
            log.info("Start restore-reactions-on-video topic handler");
            Long videoId = helper.getVarOrThrow("videoId");
            String serializedReactionIds = helper.getVarOrThrow("reactionIds");
            var video = videoService.findById(videoId); // or throw
            try {
                Set<Long> reactionIds = objectMapper.readValue(serializedReactionIds, new TypeReference<>() {});
                video.setReactionIds(reactionIds);
                videoService.updateVideo(video);
                externalTaskService.complete(externalTask);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, Optional.empty());
    }
}
