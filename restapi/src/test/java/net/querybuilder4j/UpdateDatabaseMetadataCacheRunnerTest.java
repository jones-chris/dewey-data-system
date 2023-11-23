package net.querybuilder4j;

import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDatabaseMetadataCacheRunnerTest {

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    /**
     * Clears the "updateCache" system property after all tests in this class are complete so that other
     * classes, specifically {@link ApplicationTest} are not affected by the "updateCache" property being set
     * in these test cases.
     */
    @AfterClass
    public static void afterClass() {
        System.clearProperty("updateCache");
    }

    @Test
    public void onApplicationEvent_updateCacheOptionIsMissingDoesNotUpdateCacheOrCloseApplicationContext() throws Exception {
        new UpdateDatabaseMetadataCacheRunner().onApplicationEvent(this.applicationReadyEvent);

        verify(this.databaseMetadataCacheDao, times(0)).refreshCache();
        verify(this.applicationContext, times(0)).close();
    }

    @Test
    public void onApplicationEvent_updateCacheOptionIsFalseDoesNotUpdateCacheOrCloseApplicationContext() throws Exception {
        System.setProperty("updateCache", "false");

        new UpdateDatabaseMetadataCacheRunner().onApplicationEvent(this.applicationReadyEvent);

        verify(this.databaseMetadataCacheDao, times(0)).refreshCache();
        verify(this.applicationContext, times(0)).close();
    }

    @Test
    public void onApplicationEvent_updateCacheOptionIsTrueRefreshesCacheAndExitsApplicationContext() throws Exception {
        System.setProperty("updateCache", "true");
        when(this.applicationReadyEvent.getApplicationContext())
                .thenReturn(this.applicationContext);
        doNothing()
                .when(this.applicationContext).close();
        doReturn(this.databaseMetadataCacheDao)
                .when(this.applicationContext).getBean(DatabaseMetadataCacheDao.class);
        doNothing()
                .when(this.databaseMetadataCacheDao).refreshCache();

        new UpdateDatabaseMetadataCacheRunner().onApplicationEvent(this.applicationReadyEvent);

        verify(this.databaseMetadataCacheDao, times(1)).refreshCache();
        verify(this.applicationContext, times(1)).close();
    }

    @Test
    public void onApplicationEvent_updateCacheOptionIsTrueAndExceptionIsThrownWhichCausesApplicationContextToClose() throws Exception {
        System.setProperty("updateCache", "true");
        when(this.applicationReadyEvent.getApplicationContext())
                .thenReturn(this.applicationContext);
        doNothing()
                .when(this.applicationContext).close();
        doThrow(new RuntimeException())
                .when(this.applicationContext).getBean(DatabaseMetadataCacheDao.class);

        new UpdateDatabaseMetadataCacheRunner().onApplicationEvent(this.applicationReadyEvent);

        verify(this.databaseMetadataCacheDao, times(0)).refreshCache();
        verify(this.applicationContext, times(1)).close();
    }

}