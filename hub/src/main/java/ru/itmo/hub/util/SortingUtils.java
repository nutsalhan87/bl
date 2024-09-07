package ru.itmo.hub.util;

import org.springframework.lang.NonNull;
import ru.itmo.hub.api.dto.CommentDto;
import ru.itmo.hub.api.dto.CommentSortingStrategy;
import ru.itmo.hub.core.model.comments.comment.Comment;
import ru.itmo.hub.core.model.primary.reaction.Reaction;
import ru.itmo.hub.core.model.primary.reaction.ReactionRepository;
import ru.itmo.hub.core.model.primary.tag.TagRepository;
import ru.itmo.shared.Video;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class SortingUtils {
    public static List<CommentDto> sortAndMapComments(@NonNull List<Comment> comments,
                                                      @NonNull CommentSortingStrategy sorting) {
        return comments
                .stream()
                .sorted(provideCommentComparator(sorting))
                .map(CommentDto::new)
                .toList();
    }

    private static Comparator<Comment> provideCommentComparator(@NonNull CommentSortingStrategy sorting) {
        return switch (sorting) {
            case OLDER -> Comparator.comparing(Comment::getCreationTimestamp);
            case NEWER -> Comparator.comparing(Comment::getCreationTimestamp, Comparator.reverseOrder());
        };
    }

    public static List<Video> sortVideosByTagSets(@NonNull Set<Video> videos,
                                                  @NonNull TagRepository tagRepository,
                                                  @NonNull Set<String> preferredTagValues,
                                                  @NonNull Set<String> bannedTagValues) {
        return videos.stream()
                .sorted(Comparator.comparingInt(video ->
                        video.getTagIds().stream()
                                .mapToInt(tagId -> {
                                    var tag = tagRepository.findById(tagId)
                                            .orElseThrow(IllegalArgumentException::new); // такой ситуации возникнуть не должно
                                    if (preferredTagValues.contains(tag.getValue())) return 1;
                                    if (bannedTagValues.contains(tag.getValue())) return -3;
                                    return 0;
                                })
                                .sum()
                ))
                .toList();
    }

    public static List<Video> sortVideosByPositiveFeedback(@NonNull Set<Video> videos,
                                                           @NonNull ReactionRepository reactionRepository) {
        return videos.stream()
                .sorted(Comparator.comparing(
                        v -> v.getReactionIds()
                                .stream()
                                .map(reactionId -> reactionRepository.findById(reactionId)
                                        .orElseThrow(IllegalArgumentException::new) // такой ситуации возникнуть не должно
                                )
                                .filter(Reaction::isPositive)
                                .count(),
                        Comparator.reverseOrder())
                ).toList();
    }

    public static List<Video> sortVideosByViews(Set<Video> videos) {
        return videos.stream()
                .sorted(Comparator.comparing(Video::getViews, Comparator.reverseOrder()))
                .toList();
    }
}
