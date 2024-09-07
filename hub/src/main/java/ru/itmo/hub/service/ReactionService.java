package ru.itmo.hub.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.core.model.primary.reaction.Reaction;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.kafka.Conversator;
import ru.itmo.hub.kafka.SagaOrchestrator;
import ru.itmo.hub.kafka.TopicConversationData;
import ru.itmo.shared.Video;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.LongConsumer;

import static ru.itmo.shared.ActionResult.Ok;

@RequiredArgsConstructor
@Service
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final VideoService videoService;
    private final Conversator conversator;
    private final SagaOrchestrator sagaOrchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void reactVideo(Long videoId, boolean isPositive, String authorName) {
        AtomicReference<Optional<Long>> reactionIdRef = new AtomicReference<>(Optional.empty());
        var video = videoService.findByIdOrThrowNonDto(videoId);
        var actions = new SagaOrchestrator.Action[] {
                new SagaOrchestrator.Action((tid, body) -> {
                    var reactionOpt = reactionRepository.findReactionByVideoIdAndAuthorName(videoId, authorName);
                    LongConsumer compensator;
                    if (reactionOpt.isPresent()) {
                        if (reactionOpt.orElseThrow().isPositive() != isPositive) {
                            var reaction = reactionOpt.orElseThrow();
                            reaction.setPositive(isPositive);
                            reactionRepository.save(reaction);
                            compensator = t -> {
                                reaction.setPositive(!isPositive);
                                reactionRepository.save(reaction);
                            };
                        } else {
                            compensator = t -> {};
                        }
                    } else {
                        var reaction = new Reaction(null, videoId, authorName, isPositive);
                        reactionRepository.save(reaction);
                        reactionIdRef.set(Optional.of(reaction.getId()));
                        compensator = t -> reactionRepository.delete(reaction);
                    }
                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new Ok("")));
                    return compensator;
                }),
                new SagaOrchestrator.Action((tid, body) -> {
                    var reactionId = reactionIdRef.get();
                    if (reactionId.isPresent()) {
                        video.getReactionIds().add(reactionId.orElseThrow());
                        conversator.dialogueAsync(tid, TopicConversationData.updateVideo(video));
                    } else {
                        conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new Ok("")));
                    }
                    return t -> {};
                })
        };
        sagaOrchestrator.perform(actions, "");
    }

    public void unreactVideo(Long videoId, String authorName) {
        var video = videoService.findByIdOrThrowNonDto(videoId);
        var actions = new SagaOrchestrator.Action[] {
                new SagaOrchestrator.Action((tid, body) -> {
                    var reactionOpt = reactionRepository.findReactionByVideoIdAndAuthorName(videoId, authorName);
                    if (reactionOpt.isEmpty()) {
                        throw new RuntimeException("Video with id " + videoId + " wasn't reacted by " + authorName);
                    }
                    var reaction = reactionOpt.orElseThrow();
                    reactionRepository.delete(reaction);
                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new Ok(reaction.getId().toString())));
                    return t -> reactionRepository.save(reaction);
                }),
                new SagaOrchestrator.Action((tid, body) -> {
                    var reactionId = Long.parseLong(body);
                    var reactionIds = video.getReactionIds();
                    reactionIds.remove(reactionId);
                    video.setReactionIds(reactionIds);
                    conversator.dialogueAsync(tid, TopicConversationData.updateVideo(video));
                    return t -> {};
                })
        };
        sagaOrchestrator.perform(actions, "");
    }

    public void resetReactions(Long videoId) {
        var actions = new SagaOrchestrator.Action[]{
                new SagaOrchestrator.Action((tid, body) -> {
                    conversator.dialogueAsync(tid, TopicConversationData.findVideoById(videoId));
                    return t -> {};
                }),
                new SagaOrchestrator.Action((tid, body) -> {
                    Video video;
                    try {
                        video = objectMapper.readValue(body, Video.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    var newVideo = video.withReactionIds(Collections.emptySet());
                    conversator.dialogueAsync(tid, TopicConversationData.updateVideo(newVideo));
                    return t -> conversator.monologue(TopicConversationData.updateVideo(video));
                }),
                new SagaOrchestrator.Action((tid, body) -> {
                    reactionRepository.deleteByVideoId(videoId);
                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new Ok("")));
                    return t -> {};
                })
        };
        sagaOrchestrator.perform(actions, "");
    }
}
