package ru.itmo.hub.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.itmo.hub.util.FormatUtils;

@Data
public class TagSuggestionDto {
    private String tagValue;
    private long videoId;
    @JsonProperty
    private boolean isAddSuggestion;

    public void setTagValue(String tagValue) {
        this.tagValue = FormatUtils.normalizeTag(tagValue);
    }
}
