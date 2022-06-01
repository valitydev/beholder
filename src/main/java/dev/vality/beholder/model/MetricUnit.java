package dev.vality.beholder.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MetricUnit {
    TOTAL("total"),
    MILLIS("millis");

    @Getter
    private final String unit;
}

