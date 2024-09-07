package ru.itmo.shared;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ActionResult.Ok.class, name = "ok"),
        @JsonSubTypes.Type(value = ActionResult.Fail.class, name = "fail")
})
public sealed interface ActionResult {
    record Ok(String value) implements ActionResult {
    }

    record Fail(String message) implements ActionResult {
    }

    default Optional<String> toOptional() {
        if (this instanceof Ok ok) {
            return Optional.of(ok.value);
        } else {
            return Optional.empty();
        }
    }
}