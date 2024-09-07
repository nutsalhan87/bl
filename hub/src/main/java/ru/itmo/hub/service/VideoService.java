package ru.itmo.hub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.TagDto;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.core.model.primary.tag.TagRepository;
import ru.itmo.hub.exception.ServiceException;
import ru.itmo.hub.kafka.Conversator;
import ru.itmo.hub.kafka.TopicConversationData;
import ru.itmo.hub.util.FormatUtils;
import ru.itmo.hub.util.SortingUtils;
import ru.itmo.hub.util.mapper.VideoMapper;
import ru.itmo.shared.Video;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class VideoService {
    private final TagRepository tagRepository;
    private final ReactionRepository reactionRepository;
    private final Conversator conversator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Set<VideoDto> findAll() {
        return VideoMapper
                .videoStreamToVideoDtoStream(findAllNonDto().stream(), tagRepository, reactionRepository)
                .collect(Collectors.toSet());
    }

    public Set<Video> findAllNonDto() {
        var result = conversator.dialogueSync(TopicConversationData.findAllVideos())
                .orElseThrow(ServiceException::unexpectedError);
        try {
            return objectMapper.readValue(result, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw ServiceException.unexpectedError();
        }
    }

    public List<VideoDto> filter(Set<TagDto> rawPreferredTags, Set<TagDto> rawBannedTags) {
        var preferredTagValues = mapAndNormalizeTagDtoSet(rawPreferredTags);
        var bannedTagValues = mapAndNormalizeTagDtoSet(rawBannedTags);
        preferredTagValues.removeAll(bannedTagValues);
        var videos = findAllNonDto();
        var sorted = SortingUtils.sortVideosByTagSets(videos, tagRepository, preferredTagValues, bannedTagValues);
        return VideoMapper
                .videoStreamToVideoDtoStream(sorted.stream(), tagRepository, reactionRepository)
                .toList();
    }

    private Set<String> mapAndNormalizeTagDtoSet(Set<TagDto> tagDtoSet) {
        return tagDtoSet.stream()
                .filter(tag -> tag != null && tag.getValue() != null)
                .map(TagDto::getValue)
                .map(FormatUtils::normalizeTag)
                .collect(Collectors.toSet());
    }

    public VideoDto findByIdOrThrow(Long id) {
        return VideoMapper.videoToVideoDto(findByIdOrThrowNonDto(id), tagRepository, reactionRepository);
    }

    public Video findByIdOrThrowNonDto(Long id) {
        var result = conversator.dialogueSync(TopicConversationData.findVideoById(id))
                .orElseThrow(() -> ServiceException.videoNotFound(id));
        try {
            return objectMapper.readValue(result, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw ServiceException.unexpectedError();
        }
    }

    public void viewVideo(Long id) {
        conversator.dialogueSync(TopicConversationData.viewVideo(id))
                .orElseThrow(() -> ServiceException.videoNotFound(id));
    }

    public List<VideoDto> getMostPopularVideos() {
        var videos = findAllNonDto();
        var sorted = SortingUtils.sortVideosByPositiveFeedback(videos, reactionRepository);
        return VideoMapper
                .videoStreamToVideoDtoStream(sorted.stream(), tagRepository, reactionRepository)
                .toList();
    }

    public List<VideoDto> getMostViewedVideos() {
        var sorted = SortingUtils.sortVideosByViews(findAllNonDto());
        return VideoMapper
                .videoStreamToVideoDtoStream(sorted.stream(), tagRepository, reactionRepository)
                .toList();
    }

    public Long createVideo(VideoDto videoDto, String authorName) {
        Video video = VideoMapper.videoDtoToVideo(videoDto, tagRepository);
        video.setAuthorName(authorName);

        var result = conversator.dialogueSync(TopicConversationData.createVideo(video))
                .orElseThrow(ServiceException::unexpectedError);
        try {
            return objectMapper.readValue(result, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw ServiceException.unexpectedError();
        }
    }

    public void deleteVideo(Long videoId) {
        conversator.monologue(TopicConversationData.deleteVideoById(videoId));
    }

    public void updateVideo(Long videoId, VideoDto videoDto, String authorName) {
        Video existingVideo = findByIdOrThrowNonDto(videoId);
        Video updatedVideo = VideoMapper.updateVideo(existingVideo, videoDto, tagRepository);
        updatedVideo.setAuthorName(authorName);
        conversator.monologue(TopicConversationData.updateVideo(updatedVideo));
    }
}
