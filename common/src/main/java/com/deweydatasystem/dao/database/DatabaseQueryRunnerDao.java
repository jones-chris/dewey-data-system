package com.deweydatasystem.dao.database;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.exceptions.QueryFailureException;

import java.util.UUID;

public interface DatabaseQueryRunnerDao {

    @LogExecutionTime
    IdentifiedQueryResult executeQuery(String databaseName, String sql, UUID runnableQueryId) throws QueryFailureException;

    @LogExecutionTime
    QueryResult executeQuery(String databaseName, String sql) throws QueryFailureException;

}
