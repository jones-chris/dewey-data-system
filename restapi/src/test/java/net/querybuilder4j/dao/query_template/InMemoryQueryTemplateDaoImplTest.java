package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.config.DatabaseType;
import net.querybuilder4j.exceptions.QueryTemplateNotFoundException;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.database.Database;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryQueryTemplateDaoImplTest {

    private InMemoryQueryTemplateDaoImpl inMemoryQueryTemplateDao;

    private final Database database1 = new Database("database1", DatabaseType.MySql);

    private final Database database2 = new Database("database2", DatabaseType.PostgreSQL);

    @Before
    public void beforeEach() {
        // Create a new instance for each test because the in-memory cache object is not exposed by the InMemoryQueryTemplateDaoImpl class.
        this.inMemoryQueryTemplateDao = new InMemoryQueryTemplateDaoImpl(this.buildQueryTemplateInMemoryCache());
    }

    @Test
    public void constructor_createsHashSetCacheByDefaultIfNoConstructorArgumentIsProvided() {
        this.inMemoryQueryTemplateDao = new InMemoryQueryTemplateDaoImpl();
        final String queryTemplateName = "name";
        final int queryTemplateVersion = 0;
        SelectStatement expectedSelectStatement = TestUtils.buildSelectStatement();
        expectedSelectStatement.getMetadata().setName(queryTemplateName);
        expectedSelectStatement.getMetadata().setVersion(queryTemplateVersion);

        this.inMemoryQueryTemplateDao.save(expectedSelectStatement);

        SelectStatement retrievedSelectStatement = this.inMemoryQueryTemplateDao.findByName(queryTemplateName, queryTemplateVersion);
        assertNotNull(retrievedSelectStatement);
        assertEquals(expectedSelectStatement, retrievedSelectStatement);
    }

    @Test
    public void findByName_findsQueryTemplateSuccessfully() {
        final String queryTemplateName = "queryTemplate0";
        final int queryTemplateVersion = 1;

        SelectStatement resultingSelectStatement = this.inMemoryQueryTemplateDao.findByName("queryTemplate0", 1);

        assertNotNull(resultingSelectStatement);
        assertEquals(queryTemplateName, resultingSelectStatement.getMetadata().getName());
        assertEquals(queryTemplateVersion, resultingSelectStatement.getMetadata().getVersion());
    }

    @Test(expected = QueryTemplateNotFoundException.class)
    public void findByName_throwsExceptionWhenQueryTemplateIsNotFound() {
        this.inMemoryQueryTemplateDao.findByName("fake queryTemplate", 100000000);
    }

    @Test
    public void save_addsQueryTemplateSuccessfully() {
        final String queryTemplateName = "brandNewQueryTemplate";
        final int queryTemplateVersion = 0;
        SelectStatement newSelectStatement = TestUtils.buildSelectStatement();
        newSelectStatement.getMetadata().setName(queryTemplateName);
        newSelectStatement.getMetadata().setVersion(queryTemplateVersion);

        boolean result = this.inMemoryQueryTemplateDao.save(newSelectStatement);

        SelectStatement retrievedSelectStatement = this.inMemoryQueryTemplateDao.findByName(queryTemplateName, queryTemplateVersion);
        assertTrue(result);
        assertNotNull(retrievedSelectStatement);
        assertEquals(queryTemplateName, retrievedSelectStatement.getMetadata().getName());
        assertEquals(queryTemplateVersion, retrievedSelectStatement.getMetadata().getVersion());
    }

    @Test
    public void listNames_returnsAllNamesSuccessfully() {
        Set<String> queryTemplateNames = this.inMemoryQueryTemplateDao.listNames(this.database1.getDatabaseName());

        assertNotNull(queryTemplateNames);
        assertEquals(1, queryTemplateNames.size());
        assertTrue(queryTemplateNames.contains("queryTemplate0"));
    }

    @Test
    public void getNewestVersion_returnsNewestVersionSuccessfully() {
        final String queryTemplateName = "queryTemplate0";
        final int expectedNewestVersionNumber = 1;

        Optional<Integer> resultingNewestVersion = this.inMemoryQueryTemplateDao.getNewestVersion(queryTemplateName);

        assertTrue(resultingNewestVersion.isPresent());
        assertEquals(expectedNewestVersionNumber, resultingNewestVersion.get().intValue());
    }

    @Test
    public void getVersions_returnsVersionsSuccessfully() {
        final String queryTemplateName = "queryTemplate1";

        List<Integer> versions = this.inMemoryQueryTemplateDao.getVersions(queryTemplateName);

        assertNotNull(versions);
        assertEquals(2, versions.size());
        assertTrue(versions.contains(0));
        assertTrue(versions.contains(1));
    }

    @Test
    public void getMetadata_returnsMetadataSuccessfully() {
        final String queryTemplateName = "queryTemplate0";
        final int queryTemplateVersion = 1;

        SelectStatement.Metadata metadata = this.inMemoryQueryTemplateDao.getMetadata(queryTemplateName, queryTemplateVersion);

        assertNotNull(metadata);
        assertEquals(queryTemplateName, metadata.getName());
        assertEquals(queryTemplateVersion, metadata.getVersion());
    }

    @Test(expected = QueryTemplateNotFoundException.class)
    public void getMetadata_throwsExceptionIfQueryTemplateNotFound() {
        this.inMemoryQueryTemplateDao.getMetadata("fake queryTemplateName", 10000000);
    }

    private Set<SelectStatement> buildQueryTemplateInMemoryCache() {
        Set<SelectStatement> queryTemplates = new HashSet<>();

        SelectStatement selectStatement0 = TestUtils.buildSelectStatement();
        selectStatement0.getMetadata().setName("queryTemplate0");
        selectStatement0.getMetadata().setVersion(0);
        selectStatement0.setDatabase(this.database1);
        queryTemplates.add(selectStatement0);

        SelectStatement selectStatement1 = TestUtils.buildSelectStatement();
        selectStatement1.getMetadata().setName("queryTemplate0");
        selectStatement1.getMetadata().setVersion(1);
        selectStatement1.setDatabase(this.database1);
        queryTemplates.add(selectStatement1);

        SelectStatement selectStatement2 = TestUtils.buildSelectStatement();
        selectStatement2.getMetadata().setName("queryTemplate1");
        selectStatement2.getMetadata().setVersion(0);
        selectStatement2.setDatabase(this.database2);
        queryTemplates.add(selectStatement2);

        SelectStatement selectStatement3 = TestUtils.buildSelectStatement();
        selectStatement3.getMetadata().setName("queryTemplate1");
        selectStatement3.getMetadata().setVersion(1);
        selectStatement3.setDatabase(this.database2);
        queryTemplates.add(selectStatement3);

        return queryTemplates;
    }

}