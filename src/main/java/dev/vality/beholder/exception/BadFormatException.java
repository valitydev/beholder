package dev.vality.beholder.exception;

public class BadFormatException extends RuntimeException {

    public BadFormatException(String message) {
        super(message);
    }

    public BadFormatException(Throwable throwable) {
        super(throwable);
    }

    public BadFormatException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
