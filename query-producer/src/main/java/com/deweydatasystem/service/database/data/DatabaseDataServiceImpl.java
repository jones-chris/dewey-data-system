package com.deweydatasystem.service.database.data;

import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.config.DatabaseType;
import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
import com.deweydatasystem.dao.database.DatabaseQueryRunnerDao;
import com.deweydatasystem.dao.database.QueryResult;
import com.deweydatasystem.exceptions.QueryFailureException;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.criterion.Criterion;
import com.deweydatasystem.model.criterion.Filter;
import com.deweydatasystem.model.criterion.Operator;
import com.deweydatasystem.model.database.Database;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.SqlBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseDataServiceImpl implements DatabaseDataService {

    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    private DatabaseQueryRunnerDao databaseQueryRunnerDao;

    private SqlBuilderFactory sqlBuilderFactory;

    @Autowired
    public DatabaseDataServiceImpl(
            DatabaseMetadataCacheDao databaseMetadataCacheDao,
            DatabaseQueryRunnerDao databaseQueryRunnerDao,
            SqlBuilderFactory sqlBuilderFactory
    ) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
        this.databaseQueryRunnerDao = databaseQueryRunnerDao;
        this.sqlBuilderFactory = sqlBuilderFactory;
    }

//    @Override
//    @LogExecutionTime
//    public QueryResult executeQuery(String databaseName, String sql) throws QueryFailureException {
////        return this.databaseQueryRunnerDao.executeQuery(databaseName, sql);
//        final RunnableQueryMessage runnableQueryMessage = new RunnableQueryMessage(databaseName, null, sql);
//        this.runnableQueryPublisher.publish(runnableQueryMessage);
//
//        // todo:  replace this with 1) writing query status to cache or database and 2) returning UUID of query run.
//        try {
//            return new QueryResult(null, sql);
//        }
//        catch (SQLException e) {
//            log.error("", e);
//            throw new QueryFailureException(e, sql);
//        }
//
//    }

    @Override
    @LogExecutionTime
    public QueryResult getColumnMembers(
            String databaseName,
            String schemaName,
            String tableName,
            String columnName,
            int limit,
            int offset,
            boolean ascending,
            String search
    ) throws QueryFailureException {
        // todo:  Consider caching this select statement object so that it can just be modified and not instantiated anew upon each request.
        SelectStatement selectStatement = new SelectStatement();

        // Set database.
        DatabaseType databaseType = this.databaseMetadataCacheDao.findDatabases(databaseName).getDatabaseType();
        selectStatement.setDatabase(new Database(databaseName, databaseType));

        // Set distinct.
        selectStatement.setDistinct(true);

        // Create column
        int columnDataType = this.databaseMetadataCacheDao
                .findColumnByName(databaseName, schemaName, tableName, columnName)
                .getDataType();
        Column column = new Column(databaseName, schemaName, tableName, columnName, columnDataType, null);
        selectStatement.getColumns().add(column);

        // Create table.
        selectStatement.setTable(new Table(databaseName, schemaName, tableName));

        // Create criterion.
        if (search != null) {
            Filter filter = new Filter();
            filter.getValues().add(search);

            Criterion criterion = new Criterion(0, null, null, column, Operator.like, filter, null);
            selectStatement.getCriteria().add(criterion);
        }

        // Set limit, offset, order by, and ascending.
        selectStatement.setLimit(Integer.toUnsignedLong(limit));
        selectStatement.setOffset(Integer.toUnsignedLong(offset));
        selectStatement.setOrderBy(true);
        selectStatement.setAscending(ascending);

        // Build the SQL string.
        String sql = this.sqlBuilderFactory.buildSqlBuilder(databaseName)
                .withoutRulesValidation()
                .withStatement(selectStatement)
                .build()
                .getParameterizedSql();

        // todo:  Add SQL interpolation here.

        // Query the database.
        try {
            return this.databaseQueryRunnerDao.executeQuery(databaseName, sql);
        }
        catch (QueryFailureException ex) {
            ex.setSelectStatement(selectStatement);
            throw ex;
        }
    }

}
