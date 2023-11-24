package com.deweydatasystem;

import com.deweydatasystem.dao.database.metadata.DatabaseMetadataCrawlerDao;
import com.rabbitmq.client.Connection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

    // This bean must be mocked because we do not have an external database running for the bean to read metadata from.
    @MockBean
    private DatabaseMetadataCrawlerDao databaseMetadataCrawlerDao;

    // This bean must be mocked because we do not have an external RabbitMQ instance running for the bean to publish messages to.
    @MockBean
    private Connection rabbitMqConnection;

    @Autowired
    private Application application;

    @BeforeClass
    public static void beforeClass() {
        TestUtils.loadSystemProperties();
    }

    @Test
    public void applicationContextLoadsSuccessfully() {
        assertNotNull(this.application);
    }

}