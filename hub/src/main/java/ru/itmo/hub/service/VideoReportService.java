package ru.itmo.hub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.VideoDto;
import ru.itmo.hub.core.model.primary.videoreport.VideoReport;
import ru.itmo.hub.core.model.primary.videoreport.VideoReportRepository;
import ru.itmo.hub.exception.ServiceException;
import ru.itmo.hub.kafka.Conversator;
import ru.itmo.hub.kafka.SagaOrchestrator;
import ru.itmo.hub.kafka.TopicConversationData;

import java.util.List;

import static ru.itmo.shared.ActionResult.Ok;

@RequiredArgsConstructor
@Service
public class VideoReportService {
    private final VideoReportRepository videoReportRepository;
    private final VideoService videoService;
    private final Conversator conversator;
    private final SagaOrchestrator sagaOrchestrator;

    @Transactional
    public void report(Long videoId) {
        videoService.findByIdOrThrowNonDto(videoId); // проверка существования
        VideoReport videoReport = new VideoReport(null, videoId);
        videoReportRepository.save(videoReport);
    }

    @Transactional
    public List<VideoDto> getUncheckedVideos() {
        var reportedVideoIds = videoReportRepository.getAllReportedVideoIds();
        return videoService.findAll()
                .stream()
                .filter(video -> reportedVideoIds.contains(video.getId()))
                .toList();
    }

    @Transactional
    public void approveVideo(Long videoId) {
        videoReportRepository.deleteByVideoId(videoId);
    }

    public void deleteVideo(Long videoId) {
        conversator.dialogueSync(TopicConversationData.findVideoById(videoId))
                .orElseThrow(() -> ServiceException.videoNotFound(videoId)); // проверка на существование видео
        var actions = new SagaOrchestrator.Action[]{
                new SagaOrchestrator.Action((tid, body) -> {
                    var report = videoReportRepository.findByVideoId(videoId)
                            .orElseThrow(() -> new RuntimeException("Video with id \" + videoId + \" not reported."));
                    videoReportRepository.deleteByVideoId(videoId);
                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new Ok("")));
                    return t -> videoReportRepository.save(report);
                }),
                new SagaOrchestrator.Action((tid, body) -> {
                    conversator.dialogueAsync(tid, TopicConversationData.deleteVideoById(videoId));
                    return t -> {}; // по факту никогда не исполнится, поэтому это компенсирующее действие не нужно
                })
        };
        sagaOrchestrator.perform(actions, "");
    }
}
