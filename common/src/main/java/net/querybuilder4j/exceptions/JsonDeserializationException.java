package net.querybuilder4j.exceptions;

public class JsonDeserializationException extends RuntimeException {

    public JsonDeserializationException(String message) {
        super(message);
    }

    public JsonDeserializationException(Throwable throwable) {
        super(throwable);
    }

}
