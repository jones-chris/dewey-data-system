package com.deweydatasystem.dao.database;

import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.exceptions.QueryFailureException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class DatabaseQueryRunnerDaoImpl implements DatabaseQueryRunnerDao {

    private final QbConfig qbConfig;

    public DatabaseQueryRunnerDaoImpl(QbConfig qbConfig) {
        this.qbConfig = qbConfig;
    }

    @Override
    public IdentifiedQueryResult executeQuery(String databaseName, String sql, UUID runnableQueryId) throws QueryFailureException  {
        try {
            var identifiedQueryResult = this.executeQuery(
                    databaseName,
                    sql,
                    IdentifiedQueryResult::new
            );
            identifiedQueryResult.setRunnableQueryId(runnableQueryId);

            return identifiedQueryResult;
        }
        catch (SQLException ex) {
            log.error("", ex);
            throw new QueryFailureException(ex, sql);
        }
        catch (Exception ex) {
            log.error("", ex);
            throw ex;
        }
    }

    @Override
    public QueryResult executeQuery(String databaseName, String sql) throws QueryFailureException {
        try {
            return this.executeQuery(
                    databaseName,
                    sql,
                    QueryResult::new
            );
        }
        catch (SQLException ex) {
            log.error("", ex);
            throw new QueryFailureException(ex, sql);
        }
        catch (Exception ex) {
            log.error("", ex);
            throw ex;
        }
    }

    private <T extends QueryResult> T executeQuery(
            String databaseName,
            String sql,
            Function<QueryResultDto, T> resultSetMapper
    ) throws SQLException {
        DataSource dataSource = this.qbConfig.getTargetDataSourceAsDataSource(databaseName);

        final int queryTimeout = this.qbConfig
                .getTargetDataSource(databaseName)
                .getQueryTimeoutInSeconds();

        try (
                Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
        ) {
            stmt.setQueryTimeout(queryTimeout);

            ResultSet rs = stmt.executeQuery(sql);
            QueryResultDto queryResultDto = new QueryResultDto(rs, sql);

            return resultSetMapper.apply(queryResultDto);
        }
    }

}
