package net.querybuilder4j.service.database.data;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.dao.database.DatabaseQueryRunnerDao;
import net.querybuilder4j.dao.database.QueryResult;
import net.querybuilder4j.exceptions.QueryFailureException;
import net.querybuilder4j.model.database.Database;
import net.querybuilder4j.sql.builder.SqlBuilder;
import net.querybuilder4j.sql.builder.SqlBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseDataServiceImplTest {

    @Spy
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Mock
    private SqlBuilderFactory sqlBuilderFactory;

    @Spy
    private DatabaseQueryRunnerDao databaseQueryRunnerDao;

    @InjectMocks
    private DatabaseDataServiceImpl databaseDataService;

    @Before
    public void before() throws QueryFailureException {
        SqlBuilder sqlBuilder = mock(SqlBuilder.class);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        when(sqlBuilder.withoutRulesValidation())
                .thenReturn(sqlBuilder);
        when(sqlBuilder.withStatement(any()))
                .thenReturn(sqlBuilder);
        when(sqlBuilder.build())
                .thenReturn(sqlBuilder);
        when(sqlBuilder.getSql())
                .thenReturn("SELECT CURRENT_TIMESTAMP");

        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
                .thenReturn(
                        new Database("database", DatabaseType.MySql)
                );
        when(this.databaseMetadataCacheDao.findColumnByName(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(
                        TestUtils.buildColumn(Types.INTEGER)
                );
        when(this.databaseQueryRunnerDao.executeQuery(anyString(), anyString()))
                .thenReturn(mock(QueryResult.class));
    }

//    @Test
//    public void executeQuery_callsDaoMethodAndPassesResultBack() throws QueryFailureException {
//        QueryResult expectedQueryResult = mock(QueryResult.class);
//        when(this.databaseQueryRunnerDao.executeQuery(anyString(), anyString()))
//                .thenReturn(expectedQueryResult);
//
//        QueryResult actualQueryResult = this.databaseDataService.executeQuery("database", "SELECT * FROM table");
//
//        verify(this.databaseQueryRunnerDao, times(1))
//                .executeQuery(anyString(), anyString());
//        assertEquals(expectedQueryResult, actualQueryResult);
//    }

    @Test
    public void getColumnMembers_callsDaoMethodAndPassesResultBack() throws QueryFailureException {
        QueryResult expectedQueryResult = mock(QueryResult.class);
        when(this.databaseQueryRunnerDao.executeQuery(anyString(), anyString()))
                .thenReturn(expectedQueryResult);

        QueryResult actualQueryResult = this.databaseDataService.getColumnMembers(
                "database",
                "schema",
                "table",
                "column",
                10,
                0,
                true,
                "%search%"
        );

        verify(this.databaseQueryRunnerDao, times(1))
                .executeQuery(anyString(), anyString());
        assertEquals(expectedQueryResult, actualQueryResult);
    }

    @Test
    public void getColumnMembers_returnsQueryResult() throws QueryFailureException {
        QueryResult queryResult = this.databaseDataService.getColumnMembers(
                "database",
                "schema",
                "table",
                "column",
                10,
                0,
                true,
                "%mySearchText%"
        );

        assertNotNull(queryResult);
    }

}