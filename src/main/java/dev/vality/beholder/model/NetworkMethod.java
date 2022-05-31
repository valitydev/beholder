package dev.vality.beholder.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NetworkMethod {

    REQUEST_WILL_BE_SENT("Network.requestWillBeSent"),

    RESPONSE_RECEIVED("Network.responseReceived"),
    LOADING_FINISHED("Network.loadingFinished");

    @Getter
    private final String value;

}
