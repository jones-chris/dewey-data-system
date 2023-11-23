package net.querybuilder4j.config;

import net.querybuilder4j.exceptions.QbConfigException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class QbConfigTest {

    private static QbConfig qbConfig;

    @BeforeClass
    public static void beforeClass() {
        List<QbConfig.TargetDataSource> targetDataSources = List.of(
                buildTargetDataSource(
                        "name1",
                        "url",
                        "username",
                        "password",
                        DatabaseType.PostgreSQL
                ),
                buildTargetDataSource(
                        "name2",
                        "url",
                        "username",
                        "password",
                        DatabaseType.MySql
                ),
                buildTargetDataSource(
                        "name3",
                        "url",
                        "username",
                        "password",
                        DatabaseType.Sqlite
                )

        );

        qbConfig = new QbConfig(targetDataSources, null);
    }

    @Test
    public void getTargetDataSourcesAsDataSource_returnsCorrectListOfDataSources() {
        List<DataSource> resultingDataSources = qbConfig.getTargetDataSourcesAsDataSource();

        assertEquals(3, resultingDataSources.size());
    }

    @Test
    public void getTargetDataSource_returnsTargetDataSource() {
        final String name = "name1";

        QbConfig.TargetDataSource resultingDataSource = qbConfig.getTargetDataSource(name);

        assertNotNull(resultingDataSource);
        assertEquals(name, resultingDataSource.getName());
    }

    @Test
    public void getTargetDataSource_throwsExceptionIfTargetDataSourceNameCouldNotBeFound() {
        assertThrows(QbConfigException.class, () -> qbConfig.getTargetDataSource("You won't find this name"));
    }

    @Test
    public void getTargetDataSourceAsDataSource_returnsDataSource() {
        DataSource dataSource = qbConfig.getTargetDataSourceAsDataSource("name1");

        assertNotNull(dataSource);
    }

    @Test
    public void getTargetDataSourceAsDataSource_throwsExceptionIfTargetDataSourceNameCouldNotBeFound() {
        assertThrows(IllegalArgumentException.class, () -> qbConfig.getTargetDataSourceAsDataSource("You won't find this name"));
    }

    private static QbConfig.TargetDataSource buildTargetDataSource(
            String name,
            String url,
            String username,
            String password,
            DatabaseType databaseType
    ) {
        QbConfig.TargetDataSource targetDataSource = new QbConfig.TargetDataSource();
        targetDataSource.setName(name);
        targetDataSource.setDatabaseType(databaseType);
        targetDataSource.setUrl(url);
        targetDataSource.setUsername(username);
        targetDataSource.setPassword(password);

        return targetDataSource;

    }

}