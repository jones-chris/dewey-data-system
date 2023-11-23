package net.querybuilder4j.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CacheMissException extends RuntimeException {

    public CacheMissException(String message) {
        super(message);
    }

}
