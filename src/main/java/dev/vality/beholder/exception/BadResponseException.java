package dev.vality.beholder.exception;

public class BadResponseException extends RuntimeException {

    public BadResponseException(String message) {
        super(message);
    }

    public BadResponseException(Throwable throwable) {
        super(throwable);
    }

    public BadResponseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
