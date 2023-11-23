package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.config.QbConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static net.querybuilder4j.config.DatabaseType.PostgreSQL;
import static net.querybuilder4j.config.QueryTemplateRepositoryType.SQL_DATABASE;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(QueryTemplateDaoFactorySqlDatabaseTest.DataConfig.class)
@ContextConfiguration(
        classes = {
                QueryTemplateDaoFactory.class
        }
)
public class QueryTemplateDaoFactorySqlDatabaseTest {

    @Autowired
    private QueryTemplateDaoFactory queryTemplateDaoFactory;

    @TestConfiguration
    public static class DataConfig {

        @Bean
        public QbConfig buildQbConfig() {
            QbConfig.QueryTemplateDataSource queryTemplateDataSource = new QbConfig.QueryTemplateDataSource();
            queryTemplateDataSource.setRepositoryType(SQL_DATABASE);
            queryTemplateDataSource.setDatabaseType(PostgreSQL);

            QbConfig qbConfig = new QbConfig();
            qbConfig.setQueryTemplateDataSource(queryTemplateDataSource);

            return qbConfig;
        }

    }

    @Test
    public void createInstance_inMemoryCacheInstantiatesAnInMemoryQueryTemplateDaoImpl() {
        QueryTemplateDao queryTemplateDao = this.queryTemplateDaoFactory.createInstance();

        assertTrue(queryTemplateDao instanceof SqlDatabaseQueryTemplateDaoImpl);
    }

}