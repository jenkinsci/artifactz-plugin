package io.iktech.jenkins.plugins.artifactor;

public class ExchangeException extends Exception {
    public ExchangeException() {
        super();
    }

    public ExchangeException(String message) {
        super(message);
    }

    public ExchangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
