package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.model.SelectStatement;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

//import net.querybuilder4j.dao.query_template.QueryTemplateDaoFactory.SqlStringHolder;

@RunWith(MockitoJUnitRunner.class)
public class SqlDatabaseQueryTemplateDaoImplTest {

    private final static EmbeddedDatabase embeddedDatabase = buildEmbeddedDatabase();

//    private static SqlStringHolder sqlStringHolder = buildSqlStringHolderMock();

    private SqlDatabaseQueryTemplateDaoImpl sqlDatabaseQueryTemplateDao = new SqlDatabaseQueryTemplateDaoImpl(
            embeddedDatabase
//            sqlStringHolder
    );

    @AfterClass
    public static void afterClass() {
        embeddedDatabase.shutdown();
    }

    @Test
    public void save_savesSuccessfully() {
        SelectStatement expectedSelectStatement = TestUtils.buildSelectStatement();
        final String name = "myQueryTemplate";
        final int version = 0;
        SelectStatement.Metadata metadata = new SelectStatement.Metadata();
        metadata.setName(name);
        metadata.setVersion(version);
        expectedSelectStatement.setMetadata(metadata);

        boolean result = this.sqlDatabaseQueryTemplateDao.save(expectedSelectStatement);

        SelectStatement retrievedSelectStatement = this.sqlDatabaseQueryTemplateDao.findByName(name, version);
        assertTrue(result);
        assertEquals(expectedSelectStatement.getMetadata().getName(), retrievedSelectStatement.getMetadata().getName());
        assertEquals(expectedSelectStatement.getMetadata().getVersion(), retrievedSelectStatement.getMetadata().getVersion());
    }

    @Test
    public void listNames_returnsNamesSuccessfully() {
        Set<String> queryTemplateNames = this.sqlDatabaseQueryTemplateDao.listNames("database1");

        assertFalse(queryTemplateNames.isEmpty());
    }

    @Test
    public void getNewestVersion_returnsNewestVersionSuccessfullyIfQueryTemplateNameExists() {
        Optional<Integer> newestVersion = this.sqlDatabaseQueryTemplateDao.getNewestVersion("queryTemplate0");

        assertTrue(newestVersion.isPresent());
        assertEquals(1, newestVersion.get().intValue());
    }

    @Test
    public void getNewestVersion_returnsEmptyOptionalIfQueryTemplateNameDoesNotExist() {
        Optional<Integer> newestVersion = this.sqlDatabaseQueryTemplateDao.getNewestVersion("you won't find this name");

        assertTrue(newestVersion.isEmpty());
    }

    @Test
    public void getVersions_returnsVersionsSuccessfully() {
        List<Integer> versions = this.sqlDatabaseQueryTemplateDao.getVersions("queryTemplate0");

        assertNotNull(versions);
        assertFalse(versions.isEmpty());
    }

    @Test
    public void getMetadata_returnsMetadataSuccessfully() {
        final String queryTemplateName = "getMetadata_returnsMetadataSuccessfully";
        final int queryTemplateVersion = 0;
        SelectStatement expectedSelectStatement = TestUtils.buildSelectStatement();
        SelectStatement.Metadata metadata = new SelectStatement.Metadata();
        metadata.setName(queryTemplateName);
        metadata.setVersion(queryTemplateVersion);
        expectedSelectStatement.setMetadata(metadata);
        this.sqlDatabaseQueryTemplateDao.save(expectedSelectStatement);

        SelectStatement.Metadata resultingMetadata = this.sqlDatabaseQueryTemplateDao.getMetadata(queryTemplateName, queryTemplateVersion);

        assertNotNull(metadata);
        assertEquals(expectedSelectStatement.getMetadata(), resultingMetadata);
        assertEquals(expectedSelectStatement.getMetadata(), resultingMetadata);
    }

    @Test
    public void findByName_returnsSelectStatementSuccessfully() {
        final String name = "queryTemplate0";
        final int version = 0;
        SelectStatement selectStatement = this.sqlDatabaseQueryTemplateDao.findByName(name, version);

        assertNotNull(selectStatement);
    }

    private static EmbeddedDatabase buildEmbeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("h2_query_templates_seeder.sql")
                .build();
    }

}