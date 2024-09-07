package ru.itmo.hub.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.TagSuggestionDto;
import ru.itmo.hub.core.model.primary.tag.Tag;
import ru.itmo.hub.core.model.primary.tag.TagRepository;
import ru.itmo.hub.core.model.primary.tagsuggestion.TagSuggestion;
import ru.itmo.hub.core.model.primary.tagsuggestion.TagSuggestionRepository;
import ru.itmo.hub.exception.ServiceException;
import ru.itmo.hub.kafka.Conversator;
import ru.itmo.hub.kafka.SagaOrchestrator;
import ru.itmo.hub.kafka.TopicConversationData;
import ru.itmo.shared.ActionResult;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongConsumer;

@RequiredArgsConstructor
@Service
public class TagSuggestionService {
    private final TagSuggestionRepository tagSuggestionRepository;
    private final TagRepository tagRepository;
    private final VideoService videoService;
    private final Conversator conversator;
    private final SagaOrchestrator sagaOrchestrator;

    public Set<TagSuggestion> findAll() {
        return tagSuggestionRepository.findAll();
    }

    public void createSuggestion(TagSuggestionDto suggestionDto) {
        videoService.findByIdOrThrowNonDto(suggestionDto.getVideoId());

        if (tagSuggestionRepository.existsByVideoIdAndTagValueAndIsAddSuggestion(
                suggestionDto.getVideoId(),
                suggestionDto.getTagValue(),
                suggestionDto.isAddSuggestion())
        ) throw ServiceException.tagSuggestionAlreadyExists();

        tagSuggestionRepository.save(
                new TagSuggestion(
                        null,
                        suggestionDto.getTagValue(),
                        suggestionDto.getVideoId(),
                        suggestionDto.isAddSuggestion()
                )
        );
    }

    public void dismissSuggestion(long suggestionId) {
        tagSuggestionRepository.deleteById(suggestionId);
    }

    public void acceptSuggestion(long suggestionId) {
        var suggestion = tagSuggestionRepository.findById(suggestionId).orElseThrow(ServiceException::tagSuggestionDosNotExists);
        var tagValue = suggestion.getTagValue();
        var video = videoService.findByIdOrThrowNonDto(suggestion.getVideoId());
        var actions = new SagaOrchestrator.Action[]{
                new SagaOrchestrator.Action((tid, body) -> {
                    LongConsumer compensator = t -> {};
                    Optional<Tag> tagOpt = tagRepository.findByValue(tagValue);

                    Long tagId;
                    if (tagOpt.isPresent()) {
                        tagId = tagOpt.get().getId();
                    } else if (suggestion.isAddSuggestion()) {
                        Tag tag = new Tag(null, tagValue);
                        tagId = tagRepository.save(tag).getId();
                        compensator = t -> tagRepository.delete(tag);
                    } else throw ServiceException.tagDoesNotExists();
                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new ActionResult.Ok(tagId.toString())));
                    return compensator;
                }),

                new SagaOrchestrator.Action((tid, body) -> {
                    Long tagId = Long.parseLong(body);
                    var tagIds = video.getTagIds();

                    if (suggestion.isAddSuggestion()) {
                        tagIds.add(tagId);
                    } else tagIds.remove(tagId);

                    video.setTagIds(tagIds);
                    conversator.dialogueAsync(tid, TopicConversationData.updateVideo(video));

                    return t -> {
                        if (suggestion.isAddSuggestion()) {
                            tagIds.remove(tagId);
                        } else tagIds.add(tagId);
                        video.setTagIds(tagIds);
                        conversator.monologue(TopicConversationData.updateVideo(video));
                    };
                }),

                new SagaOrchestrator.Action((tid, body) -> {
                    tagSuggestionRepository.deleteById(suggestionId);
                    conversator.dialogueAsync(tid, TopicConversationData.sagaResponse(new ActionResult.Ok("")));
                    return t -> tagSuggestionRepository.save(suggestion);
                })
        };
        sagaOrchestrator.perform(actions, "");
    }

    @Transactional
    public void deleteOldTagSuggestions(Duration timeAgo) {
        var dayAgoTimestamp = Timestamp.from(Instant.now().minus(timeAgo));
        var suggestionIdsToDelete = tagSuggestionRepository.findAll()
                .stream()
                .filter(suggestion -> suggestion.getCreationTimestamp().before(dayAgoTimestamp))
                .map(TagSuggestion::getId)
                .toList();
        tagSuggestionRepository.deleteAllById(suggestionIdsToDelete);
    }
}
