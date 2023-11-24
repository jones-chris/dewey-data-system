package com.deweydatasystem.dao.database;

import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.exceptions.QueryFailureException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseQueryRunnerDaoImplTest {

    @Mock
    private QbConfig qbConfig;

    @InjectMocks
    private DatabaseQueryRunnerDaoImpl databaseQueryRunnerDao;

    @Before
    public void beforeEach() {
        when(this.qbConfig.getTargetDataSourceAsDataSource(anyString()))
                .thenReturn(
                        new EmbeddedDatabaseBuilder()
                                .setType(EmbeddedDatabaseType.H2)
                                .addScript("h2_tables_seeder.sql")
                                .build()
                );

        QbConfig.TargetDataSource targetDataSource = new QbConfig.TargetDataSource();
        targetDataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
        targetDataSource.setUsername("sa");
        targetDataSource.setQueryTimeoutInSeconds(1);
        when(this.qbConfig.getTargetDataSource(anyString()))
                .thenReturn(targetDataSource);
        when(this.qbConfig.getTargetDataSourceAsDataSource(anyString()))
                .thenReturn(targetDataSource.getDataSource());
    }

    @Test
    public void executeQueryWithoutRunnableQueryUuid_runsSuccessfullyAgainstDatabase() throws QueryFailureException {
        QueryResult queryResult = this.databaseQueryRunnerDao.executeQuery("database", "SELECT CURRENT_TIMESTAMP");

        assertNotNull(queryResult);
        assertEquals(1, queryResult.getData().size());
    }

    @Test
    public void executeQueryWithRunnableQueryUuid_runsSuccessfullyAgainstDatabase() throws QueryFailureException {
        QueryResult queryResult = this.databaseQueryRunnerDao.executeQuery("database", "SELECT CURRENT_TIMESTAMP", UUID.randomUUID());

        assertNotNull(queryResult);
        assertEquals(1, queryResult.getData().size());
    }

    @Test(expected = QueryFailureException.class)
    public void executeQueryWithoutRunnableQueryUuid_throwsQueryFailureExceptionWhenSqlExceptionIsThrown() throws QueryFailureException {
        this.databaseQueryRunnerDao.executeQuery("database", "THIS QUERY WILL FAIL");
    }

    @Test(expected = QueryFailureException.class)
    public void executeQueryWithRunnableQueryUuid_throwsQueryFailureExceptionWhenSqlExceptionIsThrown() throws QueryFailureException {
        this.databaseQueryRunnerDao.executeQuery("database", "THIS QUERY WILL FAIL", UUID.randomUUID());
    }

    @Test(expected = QueryFailureException.class)
    public void executeQuery_throwsQueryFailureExceptionWhenTimeOutIsReached() throws QueryFailureException {
        // Test that the Statement's timeout cancel's the query by running an infinite loop CTE.  The github-actions.yml job
        // should have a timeout setting that will kill this test if the Statement does not successfully timeout.
        this.databaseQueryRunnerDao.executeQuery(
                "database",
                "with recursive rec(n) as\n" +
                        "        (\n" +
                        "        select  1 as n\n" +
                        "        union all\n" +
                        "        select  n + 1\n" +
                        "        from rec\n" +
                        "        )\n" +
                        "select  n\n" +
                        "from    rec"
        );
    }

}
