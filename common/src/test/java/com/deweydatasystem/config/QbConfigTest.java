package com.deweydatasystem.config;

import com.deweydatasystem.exceptions.QbConfigException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class QbConfigTest {

    @Mock
    private QbConfig.QueryTemplateDataSource queryTemplateDataSource;

    @Mock
    private DataSource dataSource1;

    @Mock
    private DataSource dataSource2;

    @Test
    public void getTargetDataSourcesAsDataSource_getsDataSourcesCorrectly() {
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setDataSource(this.dataSource1);
        QbConfig.TargetDataSource targetDataSource2 = new QbConfig.TargetDataSource();
        targetDataSource2.setDataSource(this.dataSource2);
        List<QbConfig.TargetDataSource> targetDataSources = List.of(targetDataSource1, targetDataSource2);

        List<DataSource> dataSources = new QbConfig(targetDataSources, this.queryTemplateDataSource)
                .getTargetDataSourcesAsDataSource();

        assertEquals(2, dataSources.size());
        assertEquals(dataSource1, dataSources.get(0));
        assertEquals(dataSource2, dataSources.get(1));
    }

    @Test
    public void getTargetDataSource_findsTargetDataSourceByNameCorrectly() {
        final String dataSource1Name = "dataSource1";
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setName(dataSource1Name);
        QbConfig.TargetDataSource targetDataSource2 = new QbConfig.TargetDataSource();
        targetDataSource2.setName("dataSource2");
        List<QbConfig.TargetDataSource> targetDataSources = List.of(targetDataSource1, targetDataSource2);

        QbConfig.TargetDataSource filterdTargetDataSource = new QbConfig(targetDataSources, this.queryTemplateDataSource)
                .getTargetDataSource(dataSource1Name);

        assertNotNull(filterdTargetDataSource);
        assertEquals(dataSource1Name, filterdTargetDataSource.getName());
    }

    @Test(expected = QbConfigException.class)
    public void getTargetDataSource_throwsQbConfigExceptionWhenNoTargetDataSourceMatchesName() {
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setName("dataSource1");
        QbConfig.TargetDataSource targetDataSource2 = new QbConfig.TargetDataSource();
        targetDataSource2.setName("dataSource2");
        List<QbConfig.TargetDataSource> targetDataSources = List.of(targetDataSource1, targetDataSource2);

        new QbConfig(targetDataSources, this.queryTemplateDataSource)
                .getTargetDataSource("You won't be able to find this name");
    }

    @Test
    public void getTargetDataSourceAsDataSource_findsDataSourceByNameCorrectly() {
        final String dataSource1Name = "dataSource1";
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setName(dataSource1Name);
        targetDataSource1.setDataSource(this.dataSource1);
        QbConfig.TargetDataSource targetDataSource2 = new QbConfig.TargetDataSource();
        targetDataSource2.setName("dataSource2");
        targetDataSource2.setDataSource(this.dataSource2);
        List<QbConfig.TargetDataSource> targetDataSources = List.of(targetDataSource1, targetDataSource2);

        DataSource filteredDataSource = new QbConfig(targetDataSources, this.queryTemplateDataSource)
                .getTargetDataSourceAsDataSource(dataSource1Name);

        assertNotNull(filteredDataSource);
        assertEquals(this.dataSource1, filteredDataSource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTargetDataSourceAsDataSource_throwsExceptionWhenNoTargetDataSourceNameMatches() {
        QbConfig.TargetDataSource targetDataSource1 = new QbConfig.TargetDataSource();
        targetDataSource1.setName("dataSource1");
        targetDataSource1.setDataSource(this.dataSource1);
        QbConfig.TargetDataSource targetDataSource2 = new QbConfig.TargetDataSource();
        targetDataSource2.setName("dataSource2");
        targetDataSource2.setDataSource(this.dataSource2);
        List<QbConfig.TargetDataSource> targetDataSources = List.of(targetDataSource1, targetDataSource2);

        new QbConfig(targetDataSources, this.queryTemplateDataSource)
                .getTargetDataSourceAsDataSource("You won't be able to find this name");
    }

    @Test
    public void getConnectionName_isFormattedCorrectly() {
        final String connectionNameApp = "myApp";
        final String connectionNameComponent = "myComponent";
        QbConfig.MessagingConfiguration messagingConfiguration = new QbConfig.MessagingConfiguration();
        messagingConfiguration.setConnectionNameApp(connectionNameApp);
        messagingConfiguration.setConnectionNameComponent(connectionNameComponent);

        String actualConnectionName = messagingConfiguration.getConnectionName();

        assertEquals(
                String.format(QbConfig.MessagingConfiguration.CONNECTION_NAME_TEMPLATE, connectionNameApp, connectionNameComponent),
                actualConnectionName
        );
    }

    @Test
    public void getConnectionName_defaultsAreUsedWhenNoAppOrComponentAreSpecified() {
        QbConfig.MessagingConfiguration messagingConfiguration = new QbConfig.MessagingConfiguration();

        String actualConnectionName = messagingConfiguration.getConnectionName();

        assertEquals(
                String.format(
                        QbConfig.MessagingConfiguration.CONNECTION_NAME_TEMPLATE,
                        QbConfig.MessagingConfiguration.DEFAULT_COMPONENT_NAME_APP,
                        QbConfig.MessagingConfiguration.DEFAULT_CONNECTION_NAME_COMPONENT
                ),
                actualConnectionName
        );
    }

}