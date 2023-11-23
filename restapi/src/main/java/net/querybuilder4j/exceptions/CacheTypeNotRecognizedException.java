package net.querybuilder4j.exceptions;

import net.querybuilder4j.config.CacheType;
import net.querybuilder4j.dao.database.metadata.DatabaseMetadataCacheFactory;

/**
 * A class that should be raised if the {@link CacheType} is not recognized in the
 * {@link DatabaseMetadataCacheFactory} class.
 */
public class CacheTypeNotRecognizedException extends RuntimeException {

    public CacheTypeNotRecognizedException(String message) {
        super(message);
    }

}
