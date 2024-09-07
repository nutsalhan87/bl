package ru.itmo.hub.core.model.primary.playlist;

import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import ru.itmo.hub.exception.ServiceException;

import java.util.Set;

public interface PlaylistRepository extends CrudRepository<Playlist, Long> {
    // TODO: теперь не используется. мб убрать, если и впредь не понадобится?
    @NonNull
    Set<Playlist> findAllByAuthorName(String authorName);
    @NonNull
    Set<Playlist> findAll();
    default Playlist findPlaylistByIdOrThrow(@NonNull Long playlistId) {
        return this.findById(playlistId).orElseThrow(() -> ServiceException.playlistNotFound(playlistId));
    }
}
