package com.deweydatasystem.service.database.data;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.dao.database.QueryResult;
import com.deweydatasystem.exceptions.QueryFailureException;

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
