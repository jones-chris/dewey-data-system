//package com.deweydatasystem.dao.database.data;
//
//import com.deweydatasystem.TestUtils;
//import com.deweydatasystem.config.DatabaseType;
//import com.deweydatasystem.dao.database.DatabaseMetadataCacheDao;
//import com.deweydatasystem.dao.database.DatabaseQueryRunnerDao;
//import com.deweydatasystem.dao.database.QueryResult;
//import com.deweydatasystem.exceptions.QueryFailureException;
//import com.deweydatasystem.model.database.Database;
//import com.deweydatasystem.sql.builder.SqlBuilder;
//import com.deweydatasystem.sql.builder.SqlBuilderFactory;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.MockitoJUnitRunner;
//
//import java.sql.Types;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@RunWith(MockitoJUnitRunner.class)
//public class DatabaseDataDaoImplTest {
//
//    @Mock
//    private DatabaseMetadataCacheDao databaseMetadataCacheDao;
//
//    @Mock
//    private SqlBuilderFactory sqlBuilderFactory;
//
//    @Mock
//    private DatabaseQueryRunnerDao databaseQueryRunnerDao;
//
//    @InjectMocks
//    private DatabaseDataDaoImpl databaseDataDao;
//
////    @Before
////    public void beforeEach() {
////        when(this.qbConfig.getTargetDataSourceAsDataSource(anyString()))
////            .thenReturn(
////                    new EmbeddedDatabaseBuilder()
////                            .setType(EmbeddedDatabaseType.H2)
////                            .build()
////            );
////
////        when(this.qbConfig.getTargetDataSource(anyString()))
////                .thenReturn(new QbConfig.TargetDataSource());
////    }
//
//    @Test
//    public void getColumnMembers_returnsQueryResult() throws QueryFailureException {
//        when(this.databaseMetadataCacheDao.findDatabases(anyString()))
//                .thenReturn(
//                        new Database("database", DatabaseType.MySql)
//                );
//        when(this.databaseMetadataCacheDao.findColumnByName(anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(
//                        TestUtils.buildColumn(Types.INTEGER)
//                );
//        SqlBuilder sqlBuilder = mock(SqlBuilder.class);
//        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
//                .thenReturn(sqlBuilder);
//        when(sqlBuilder.withoutRulesValidation())
//                .thenReturn(sqlBuilder);
//        when(sqlBuilder.withStatement(any()))
//                .thenReturn(sqlBuilder);
//        when(sqlBuilder.build())
//                .thenReturn(sqlBuilder);
//        when(sqlBuilder.getSql())
//                .thenReturn("SELECT CURRENT_TIMESTAMP");
//
//        QueryResult queryResult = this.databaseDataDao.getColumnMembers(
//                "database",
//                "schema",
//                "table",
//                "column",
//                10,
//                0,
//                true,
//                "%mySearchText%"
//        );
//
//        assertNotNull(queryResult);
//        assertEquals(1, queryResult.getData().size());
//    }
//
//}