package net.querybuilder4j.service.database.data;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.dao.database.QueryResult;
import net.querybuilder4j.exceptions.QueryFailureException;

public interface DatabaseDataService {

//    @LogExecutionTime
//    QueryResult executeQuery(String databaseName, String sql) throws QueryFailureException;

    @LogExecutionTime
    QueryResult getColumnMembers(
            String databaseName,
            String schemaName,
            String tableName,
            String columnName,
            int limit,
            int offset,
            boolean ascending,
            String search
    ) throws QueryFailureException;

}
