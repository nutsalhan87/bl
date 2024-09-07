package ru.itmo.hub.core.model.primary.tagsuggestion;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.Set;

public interface TagSuggestionRepository extends CrudRepository<TagSuggestion, Long>  {
    @NonNull
    Set<TagSuggestion> findAll();

    boolean existsByVideoIdAndTagValueAndIsAddSuggestion(long videoId, String tagValue, boolean isAddSuggestion);
}
