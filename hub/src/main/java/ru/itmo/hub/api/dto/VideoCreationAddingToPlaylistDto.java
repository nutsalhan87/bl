package ru.itmo.hub.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VideoCreationAddingToPlaylistDto {
    Long playlistId;

    VideoDto video;
}
