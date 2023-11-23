package net.querybuilder4j.config;

import net.querybuilder4j.TestUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { DataConfig.class })
public class DataConfigTest {

    @Autowired
    private QbConfig qbConfig;

    @BeforeClass
    public static void beforeClass() throws IOException, URISyntaxException {
        TestUtils.loadSystemProperties();
    }

    @Test
    public void getTargetDatabases_buildsQbConfigObjectSuccessfully() {
        assertNotNull(this.qbConfig);
        assertNotNull(this.qbConfig.getTargetDataSources());
        assertNotNull(this.qbConfig.getDatabaseMetadataCacheSource());
        assertNotNull(this.qbConfig.getQueryTemplateDataSource());
    }

}