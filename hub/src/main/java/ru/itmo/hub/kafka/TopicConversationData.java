package ru.itmo.hub.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import ru.itmo.shared.ActionResult;
import ru.itmo.shared.Video;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TopicConversationData {
    @Getter(AccessLevel.NONE)
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String topic;
    private final String serialized;

    public static TopicConversationData findAllVideos() {
        return new TopicConversationData("findAllVideos", "");
    }

    @SneakyThrows
    public static TopicConversationData findVideoById(Long videoId) {
        return new TopicConversationData("findVideoById", objectMapper.writeValueAsString(videoId));
    }

    @SneakyThrows
    public static TopicConversationData viewVideo(Long videoId) {
        return new TopicConversationData("viewVideo", objectMapper.writeValueAsString(videoId));
    }

    @SneakyThrows
    public static TopicConversationData createVideo(Video video) {
        return new TopicConversationData("createVideo", objectMapper.writeValueAsString(video));
    }

    @SneakyThrows
    public static TopicConversationData updateVideo(Video video) {
        return new TopicConversationData("updateVideo", objectMapper.writeValueAsString(video));
    }

    @SneakyThrows
    public static TopicConversationData deleteVideoById(Long videoId) {
        return new TopicConversationData("deleteVideoById", objectMapper.writeValueAsString(videoId));
    }

    @SneakyThrows
    public static TopicConversationData sagaResponse(ActionResult actionResult) {
        return new TopicConversationData("response", objectMapper.writeValueAsString(actionResult));
    }
}
