package net.querybuilder4j.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zaxxer.hikari.HikariDataSource;
import lombok.*;
import net.querybuilder4j.exceptions.QbConfigException;
import net.querybuilder4j.utils.ExcludeFromJacocoGeneratedReport;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class QbConfig {

    private List<TargetDataSource> targetDataSources;

    private QueryTemplateDataSource queryTemplateDataSource;

    private DatabaseMetadataCacheSource databaseMetadataCacheSource;

    private Rules rules = new Rules();

    private SecurityConfiguration securityConfiguration = new SecurityConfiguration();

    private MessagingConfiguration messagingConfiguration = new MessagingConfiguration();

    private QueryStatusConfiguration queryStatusConfiguration = new QueryStatusConfiguration();

    public QbConfig(
            List<TargetDataSource> targetDataSources,
            QueryTemplateDataSource  queryTemplateDataSource
    ) {
        this.targetDataSources = targetDataSources;
        this.queryTemplateDataSource = queryTemplateDataSource;
    }

    public List<DataSource> getTargetDataSourcesAsDataSource() {
        return this.targetDataSources.stream()
                .map(TargetDataSource::getDataSource)
                .collect(Collectors.toList());
    }

    public TargetDataSource getTargetDataSource(String name) {
        return this.targetDataSources.stream()
                .filter(source -> source.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new QbConfigException("Could not find a target data source named, " + name + ", to create a DataSource for"));
    }

    public DataSource getTargetDataSourceAsDataSource(String targetDatabaseName) {
        return targetDataSources.stream()
                .filter(source -> source.getName().equals(targetDatabaseName))
                .findFirst()
                .map(TargetDataSource::getDataSource)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find target data source with name, %s", targetDatabaseName)));
    }

    @ExcludeFromJacocoGeneratedReport
    private static DataSource buildDataSource(
            String url,
            String username,
            String password,
            DatabaseType databaseType
    ) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setMaximumPoolSize(10); // todo:  Make this configurable.

        // todo:  Add more driver mappings here when more database types are supported.
        if(DatabaseType.PostgreSQL.equals(databaseType)) {
            dataSource.setDriverClassName("org.postgresql.Driver");
        }
        else if (DatabaseType.MySql.equals(databaseType)) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        else if (DatabaseType.Sqlite.equals(databaseType)) {
            dataSource.setDriverClassName("org.sqlite.JDBC");
        }

        return dataSource;
    }

    @NoArgsConstructor
    @Data
    @ExcludeFromJacocoGeneratedReport
    public static class TargetDataSource {

        private String name;

        private String url;

        private DatabaseType databaseType;

        private String username;

        private String password;

        private int queryTimeoutInSeconds = 900; // Default to 15 minutes.

        private ExcludeObjects excludeObjects = new ExcludeObjects();

        private DataSource dataSource;

        public DataSource getDataSource() {
            if (this.dataSource == null) {
                this.dataSource = QbConfig.buildDataSource(
                        this.url,
                        this.username,
                        this.password,
                        this.databaseType
                );
            }

            return this.dataSource;
        }

        @NoArgsConstructor
        @Data
        @ExcludeFromJacocoGeneratedReport
        public static class ExcludeObjects {

            /**
             * A list of the names of the schemas to exclude (case-sensitive).
             */
            private List<String> schemas = new ArrayList<>();

            /**
             * A list of the fully qualified names of the tables to exclude (case-sensitive).
             */
            private List<String> tables = new ArrayList<>();

            /**
             * A list of the fully qualified names of the columns to exclude (case-sensitive).
             */
            private List<String> columns = new ArrayList<>();

        }
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    @ExcludeFromJacocoGeneratedReport
    public static class QueryTemplateDataSource {

        @Getter
        @Setter
        private QueryTemplateRepositoryType repositoryType;

        @Getter
        @Setter
        private String url;

        @Getter
        @Setter
        private DatabaseType databaseType;

        @Getter
        @Setter
        private String username;

        @Getter
        @Setter
        private String password;

        private DataSource dataSource;

        public DataSource getDataSource() {
            if (this.dataSource == null) {
                this.dataSource = QbConfig.buildDataSource(
                        this.url,
                        this.username,
                        this.password,
                        this.databaseType
                );
            }

            return this.dataSource;
        }
    }

    @NoArgsConstructor
    @EqualsAndHashCode
    @ToString
    @Getter
    @Setter
    @ExcludeFromJacocoGeneratedReport
    public static class DatabaseMetadataCacheSource {

        private CacheType cacheType;

        private String host;

        private int port;

        private String username;

        private String password;

    }

    @Data
    @ExcludeFromJacocoGeneratedReport
    public static class Rules {

        @JsonProperty("numberOfCriteriaUsingIndexedColumns")
        private int numberOfCriteriaUsingIndexedColumns = 0;

        @JsonProperty("maximumAllowedSelectStatementNumberOfColumns")
        private int maximumAllowedSelectStatementNumberOfColumns = 25;

    }

    @Data
    @ExcludeFromJacocoGeneratedReport
    public static class SecurityConfiguration {

        /**
         * The domain of the core connector.  The Thymeleaf resolver will interpolate this into the non-core connector templates
         * so that the non-core connectors know which domain that messages will be posted from.  Otherwise, the non-core
         * connectors will have to accept all messages from all domains, which is not recommended from a security perspective.
         */
        @JsonProperty("servingDomain")
        private String servingDomain = "http://localhost:8080"; // Default to localhost.

    }

    @Data
    public static class MessagingConfiguration {

        public static final String CONNECTION_NAME_TEMPLATE = "app:%s component:%s";

        public static final String DEFAULT_COMPONENT_NAME_APP = "query-consumer";

        public static final String DEFAULT_CONNECTION_NAME_COMPONENT = "my-connection-name-component";

        private String queueName;

        private String host;

        private String virtualHost;

        private String username;

        private String password;

        private int port = 5672;

        private String connectionNameApp = DEFAULT_COMPONENT_NAME_APP;

        private String connectionNameComponent = DEFAULT_CONNECTION_NAME_COMPONENT; // todo:  dynamically set this.. It should not be hard coded.

        public String getConnectionName() {
            return String.format(CONNECTION_NAME_TEMPLATE, this.connectionNameApp, this.connectionNameComponent);
        }

    }

    @Data
    @ExcludeFromJacocoGeneratedReport
    public static class QueryStatusConfiguration {

        private String host;

        private int port;

        private String username;

        private String password;

    }

}