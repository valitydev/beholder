package dev.vality.beholder.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Browser {
    CHROME("chrome");

    @Getter
    private final String label;
}
