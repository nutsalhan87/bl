package ru.itmo.hub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.hub.api.dto.TagDto;
import ru.itmo.hub.core.model.primary.tag.Tag;
import ru.itmo.hub.core.model.primary.tag.TagRepository;
import ru.itmo.hub.util.FormatUtils;
import ru.itmo.hub.util.ValidationUtils;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class TagService {
    private final TagRepository tagRepository;

    public Set<Tag> findAll() {
        return tagRepository.findAll();
    }

    public String save(TagDto tagDto) {
        ValidationUtils.validateTagDto(tagDto);
        String tagValue = FormatUtils.normalizeTag(tagDto.getValue());
        if (!tagRepository.existsByValue(tagValue)) {
            tagRepository.save(new Tag(null, tagValue));
        }
        return tagValue;
    }

}
