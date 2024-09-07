package ru.itmo.hub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.VideoCreationAddingToPlaylistDto;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.primary.playlist.Playlist;
import ru.itmo.hub.core.model.primary.playlist.PlaylistRepository;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.core.model.primary.tag.TagRepository;
import ru.itmo.hub.exception.ServiceException;
import ru.itmo.hub.kafka.SagaOrchestrator;
import ru.itmo.hub.kafka.TopicConversationData;
import ru.itmo.hub.util.mapper.VideoMapper;
import ru.itmo.hub.kafka.Conversator;
import ru.itmo.shared.ActionResult;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PlaylistService {
    private final PlaylistRepository playlistRepository;
    private final TagRepository tagRepository;
    private final ReactionRepository reactionRepository;
    private final VideoService videoService;
    private final SagaOrchestrator sagaOrchestrator;
    private final Conversator conversator;

    @Transactional
    public void createPlaylist(String authorName, String name) {
        Playlist playlist = new Playlist(authorName, name);
        playlistRepository.save(playlist);
    }

    public void addVideoToPlaylist(String authorName, Long playlistId, Long videoId) {
        videoService.findByIdOrThrow(videoId);
        Playlist playlist = playlistRepository.findPlaylistByIdOrThrow(playlistId);

        if (!Objects.equals(playlist.getAuthorName(), authorName)) throw ServiceException.wrongUser();

        Set<Long> videoIds = playlist.getVideoIds();

        if (videoIds.add(videoId)) {
            playlist.setVideoIds(videoIds); // TODO: это та же ссылка, поэтому заново устанавливать ее же не имеет смысла
            playlistRepository.save(playlist);
        }
    }

    public void createVideoAddToPlaylist(String authorName, VideoCreationAddingToPlaylistDto dto) {
        Playlist playlist = playlistRepository.findPlaylistByIdOrThrow(dto.getPlaylistId());

        if (!Objects.equals(playlist.getAuthorName(), authorName)) throw ServiceException.wrongUser();

        VideoDto videoDto = dto.getVideo();

        var actions = new SagaOrchestrator.Action[]{
                new SagaOrchestrator.Action((tid, body) -> {
                    Long videoId = videoService.createVideo(videoDto, authorName);

                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new ActionResult.Ok(videoId.toString())));
                    return t -> videoService.deleteVideo(videoId);
                }),
                new SagaOrchestrator.Action((tid, body) -> {
                    Long videoId = Long.parseLong(body);

                    addVideoToPlaylist(authorName, playlist.getId(), videoId);

                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new ActionResult.Ok("")));
                    return t -> {};
                })};

        sagaOrchestrator.perform(actions, "");
    }

    @Transactional
    public void deleteVideoFromPlaylist(String authorName, Long playlistId, Long videoId) {
        Playlist playlist = playlistRepository.findPlaylistByIdOrThrow(playlistId);

        if (!Objects.equals(playlist.getAuthorName(), authorName)) throw ServiceException.wrongUser();

        Set<Long> videoIds = playlist.getVideoIds();

        if (videoIds.remove(videoId)) {
            playlist.setVideoIds(videoIds);
            playlistRepository.save(playlist);
        }
    }

    @Transactional
    public void deletePlaylist(String authorName, Long playlistId) {
        Playlist playlist = playlistRepository.findPlaylistByIdOrThrow(playlistId);

        if (!Objects.equals(playlist.getAuthorName(), authorName)) throw ServiceException.wrongUser();

        playlistRepository.deleteById(playlistId);
    }

    @Transactional
    public Set<Playlist> findAll() {
        return playlistRepository.findAll();
    }

    @Transactional
    public Set<VideoDto> getPlaylistVideos(Long playlistId) {
        Playlist playlist = playlistRepository.findPlaylistByIdOrThrow(playlistId);
        return VideoMapper.videoStreamToVideoDtoStream(
                        videoService
                                .findAllNonDto()
                                .stream()
                                .filter(video -> playlist.getVideoIds().contains(video.getId())),
                        tagRepository,
                        reactionRepository)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void clearDailyPlaylist(){
        Playlist playlist = playlistRepository.findPlaylistByIdOrThrow(1L);
        playlist.setVideoIds(null);
        playlistRepository.save(playlist);
    }
}
