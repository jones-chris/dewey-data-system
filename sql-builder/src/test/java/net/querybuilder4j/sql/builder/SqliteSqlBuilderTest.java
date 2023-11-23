package net.querybuilder4j.sql.builder;

import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.validator.SelectStatementValidator;
import net.querybuilder4j.service.QueryTemplateService;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SqliteSqlBuilderTest extends SqlBuilderCommonTests{

    public SqliteSqlBuilderTest() {
        super();

        this.databaseMetadataCacheDao = Mockito.mock(DatabaseMetadataCacheDao.class);
        this.queryTemplateService = Mockito.mock(QueryTemplateService.class);
        this.selectStatementSqlValidator = Mockito.mock(SelectStatementValidator.class);
        this.sqlBuilder = Mockito.spy(
                new SqliteSqlBuilder(
                        this.databaseMetadataCacheDao,
                        this.queryTemplateService,
                        this.selectStatementSqlValidator
                )
        );
        this.beginningDelimiter = '"';
        this.endingDelimiter = '"';
    }

}