package ru.itmo.hub.core.model.primary.reaction;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.Set;

public interface ReactionRepository extends CrudRepository<Reaction, Long> {
    @NonNull
    Set<Reaction> findAll();
    @Query("select count(reaction) from Reaction reaction where reaction.id in ?1 and reaction.isPositive = true")
    Long countLikes(Set<Long> reactionIds);
    @Query("select count(reaction) from Reaction reaction where reaction.id in ?1 and reaction.isPositive = false")
    Long countDislikes(Set<Long> reactionIds);
    Optional<Reaction> findReactionByVideoIdAndAuthorName(Long videoId, String authorName);
    void deleteByVideoId(Long videoId);
}
