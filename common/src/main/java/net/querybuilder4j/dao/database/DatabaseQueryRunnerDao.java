package net.querybuilder4j.dao.database;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.exceptions.QueryFailureException;
import net.querybuilder4j.model.ro.RunnableQueryMessage;

import java.util.UUID;

public interface DatabaseQueryRunnerDao {

    @LogExecutionTime
    IdentifiedQueryResult executeQuery(String databaseName, String sql, UUID runnableQueryId) throws QueryFailureException;

    @LogExecutionTime
    QueryResult executeQuery(String databaseName, String sql) throws QueryFailureException;

}
