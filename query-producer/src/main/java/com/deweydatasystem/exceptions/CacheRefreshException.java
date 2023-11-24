package com.deweydatasystem.exceptions;

/**
 * A class representing an exception relating to the refreshing of the database metadata cache.
 */
public class CacheRefreshException extends RuntimeException {

    public CacheRefreshException(Throwable e) {
        super(e);
    }

}
