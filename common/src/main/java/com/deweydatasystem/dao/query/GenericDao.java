package com.deweydatasystem.dao.query;

/**
 * A generic DAO interface that can be implemented by all DAO implementation classes.
 *
 * @param <T> The object/entity type.
 * @param <U> The type of unique id of the object/entity.
 */
public interface GenericDao<T, U> {

    T getById(U id);

    void save(T t);

}
