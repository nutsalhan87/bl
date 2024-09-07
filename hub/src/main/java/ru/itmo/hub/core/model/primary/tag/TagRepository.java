package ru.itmo.hub.core.model.primary.tag;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.Set;

public interface TagRepository extends CrudRepository<Tag, Long> {
    @NonNull
    Set<Tag> findAll();
    @Query("select tag.id from Tag tag where tag.value in ?1")
    Set<Long> findAllIdsByValuesIn(Set<String> values);
    @Query("select tag.value from Tag tag where tag.id in ?1")
    Set<String> findAllValuesByIdsIn(Set<Long> ids);
    boolean existsById(@NonNull Long id);
    boolean existsByValue(@NonNull String value);
    Optional<Tag> findByValue(@NonNull String value);
}
