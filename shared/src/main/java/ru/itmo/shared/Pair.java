package ru.itmo.shared;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class Pair<T, S> {
    private T first;
    private S second;
}
