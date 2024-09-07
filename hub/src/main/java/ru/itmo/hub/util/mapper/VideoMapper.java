package ru.itmo.hub.util.mapper;

import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.core.model.primary.tag.TagRepository;
import ru.itmo.shared.Video;

import java.util.stream.Stream;

@UtilityClass
public class VideoMapper {
    public Video videoDtoToVideo(@NonNull VideoDto videoDto,
                                 @NonNull TagRepository tagRepository) {
        return Video.builder()
                .id(videoDto.getId())
                .name(videoDto.getName())
                .description(videoDto.getDescription())
                .tagIds(tagRepository.findAllIdsByValuesIn(videoDto.getTags()))
                .build();
    }

    public VideoDto videoToVideoDto(@NonNull Video video,
                                    @NonNull TagRepository tagRepository,
                                    @NonNull ReactionRepository reactionRepository) {
        return VideoDto.builder()
                .id(video.getId())
                .name(video.getName())
                .description(video.getDescription())
                .authorName(video.getAuthorName())
                .views(video.getViews())
                .posted(video.getPosted())
                .tags(tagRepository.findAllValuesByIdsIn(video.getTagIds()))
                .likes(reactionRepository.countLikes(video.getReactionIds()))
                .dislikes(reactionRepository.countDislikes(video.getReactionIds()))
                .build();
    }

    public Stream<VideoDto> videoStreamToVideoDtoStream(@NonNull Stream<Video> videos,
                                                        @NonNull TagRepository tagRepository,
                                                        @NonNull ReactionRepository reactionRepository) {
        return videos.map(video -> VideoMapper.videoToVideoDto(video, tagRepository, reactionRepository));
    }

    public Video updateVideo(@NonNull Video existingVideo,
                             @NonNull VideoDto video,
                             @NonNull TagRepository tagRepository) {
        return Video.builder()
                .id(existingVideo.getId())
                .name(video.getName() == null ? existingVideo.getName() : video.getName())
                .description(video.getDescription() == null ? existingVideo.getDescription() : video.getDescription())
                .authorName(existingVideo.getAuthorName())
                .tagIds(video.getTags() == null ?  existingVideo.getTagIds() : tagRepository.findAllIdsByValuesIn(video.getTags()))
                .build();
    }
}
