package net.querybuilder4j.controller.database.data;

import net.querybuilder4j.dao.database.IdentifiedQueryResult;
import net.querybuilder4j.dao.database.QueryResult;
import net.querybuilder4j.exceptions.QueryFailureException;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.ro.RunnableQueryMessage;
import net.querybuilder4j.model.validator.SqlValidator;
import net.querybuilder4j.ro.RunQueryTemplateRequest;
import net.querybuilder4j.service.QueryTemplateService;
import net.querybuilder4j.service.database.data.DatabaseDataService;
import net.querybuilder4j.service.messaging.RunnableQueryPublisherService;
import net.querybuilder4j.service.query.result.IdentifiedQueryResultService;
import net.querybuilder4j.sql.builder.SqlBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/data")
public class DatabaseDataController {

    private DatabaseDataService databaseDataService;

    private SqlBuilderFactory sqlBuilderFactory;

    private QueryTemplateService queryTemplateService;

    private RunnableQueryPublisherService runnableQueryPublisherService;

    private IdentifiedQueryResultService identifiedQueryResultService;

    @Autowired
    public DatabaseDataController(
            DatabaseDataService databaseDataService,
            SqlBuilderFactory sqlBuilderFactory,
            QueryTemplateService queryTemplateService,
            RunnableQueryPublisherService runnableQueryPublisherService,
            IdentifiedQueryResultService identifiedQueryResultService
    ) {
        this.databaseDataService = databaseDataService;
        this.sqlBuilderFactory = sqlBuilderFactory;
        this.queryTemplateService = queryTemplateService;
        this.runnableQueryPublisherService = runnableQueryPublisherService;
        this.identifiedQueryResultService = identifiedQueryResultService;
    }

    /**
     * Get a column's members.
     *
     * @param database The database of the column members to retrieve.
     * @param schema The schema of the column members to retrieve.
     * @param table The table of the column members to retrieve.
     * @param column The column of the column members to retrieve.
     * @param limit The maximum number of column members to retrieve (used for pagination).
     * @param offset The column member record number that the results should start at (used for pagination).
     * @param ascending Whether the query that retrieves the column members should be in ascending or descending order.
     * @param search The text that the column members should contain.
     * @return A ResponseEntity containing
     */
    @GetMapping(value = "/{database}/{schema}/{table}/{column}/column-member")
    public ResponseEntity<QueryResult> getColumnMembers(
            @PathVariable String database,
            @PathVariable String schema,
            @PathVariable String table,
            @PathVariable String column,
            @RequestParam int limit,
            @RequestParam int offset,
            @RequestParam boolean ascending,
            @RequestParam(required = false) String search
    ) throws QueryFailureException {
        QueryResult columnMembers = databaseDataService.getColumnMembers(database, schema, table, column, limit, offset, ascending, search);
        return ResponseEntity.ok(columnMembers);
    }

    /**
     * Execute a SelectStatement, audits the database for any unexpected changes, heals the database if necessary, publishes
     * a request to an SNS topic (if the database needed to be healed), and returns the query's results.
     *
     * @param selectStatement The SelectStatement to build a SQL string for.
     * @return A {@link ResponseEntity} containing a {@link QueryResult}.
     */
    @PostMapping(value = "/{database}/query")
    public ResponseEntity<UUID> getQueryResults(
            @PathVariable String database,
            @RequestBody SelectStatement selectStatement
    ) throws IOException, TimeoutException {
        String sql = this.sqlBuilderFactory
                .buildSqlBuilder(database)
                .withStatement(selectStatement)
                .build()
                .getSql();

        return this.runSql(database, sql, selectStatement);
    }

    /**
     * Returns a SQL {@link String} using the supplied {@link SelectStatement}.
     *
     * @param selectStatement The SelectStatement to build a SQL {@link String} for.
     * @return A {@link ResponseEntity} containing a SQL {@link String}.
     */
    @PostMapping(value = "/{database}/query/dry-run")
    public ResponseEntity<String> getSelectStatementSql(
            @PathVariable String database,
            @RequestBody SelectStatement selectStatement
    ) {
        final String sql = this.sqlBuilderFactory
                .buildSqlBuilder(database)
                .withStatement(selectStatement)
                .build()
                .getSql();

        return ResponseEntity.ok(sql);
    }

    @PostMapping(
            value = "/{database}/query/raw",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UUID> runRawSql(
            @PathVariable String database,
            @RequestBody String sql
    ) throws IOException, TimeoutException {
        SqlValidator.assertSqlIsNotDestructive(sql);
        return this.runSql(database, sql);
    }

    /**
     * Gets a {@link SelectStatement} given a query template name and version, passes overrides and arguments to it,
     * builds a SQL {@link String}, and executes the SQL.
     *
     * @param database {@link String} The database to run the SQL against.
     * @param runQueryTemplateRequest A {@link RunQueryTemplateRequest} encapsulating the query template name and version
     *                                as well as the {@link SelectStatement} overrides and arguments.
     * @return {@link QueryResult}
     */
    @PostMapping(
            value = "/{database}/query/query-template",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UUID> runQueryTemplate(
            @PathVariable String database,
            @RequestBody RunQueryTemplateRequest runQueryTemplateRequest
    ) throws IOException, TimeoutException {
        final SelectStatement selectStatement = this.queryTemplateService.findByName(
                runQueryTemplateRequest.getQueryTemplateName(),
                runQueryTemplateRequest.getVersion()
        );

        selectStatement.setOverrides(runQueryTemplateRequest.getOverrides());
        selectStatement.setCriteriaArguments(runQueryTemplateRequest.getCriteriaArguments());

        final String sql = this.sqlBuilderFactory
                .buildSqlBuilder(database)
                .withStatement(selectStatement)
                .build()
                .getSql();

        return this.runSql(database, sql, selectStatement);
    }

    /**
     * Gets a {@link SelectStatement} given a query template name and version, passes overrides and arguments to it,
     * builds a SQL {@link String}, but does not execute the SQL against a database - rather it simply returns the generated
     * SQL.
     *
     * @param database {@link String} The database to run the SQL against.
     * @param runQueryTemplateRequest A {@link RunQueryTemplateRequest} encapsulating the query template name and version
     *                                as well as the {@link SelectStatement} overrides and arguments.
     * @return {@link String}
     */
    @PostMapping(
            value = "/{database}/query/query-template/dry-run",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> getQueryTemplateSql(
            @PathVariable String database,
            @RequestBody RunQueryTemplateRequest runQueryTemplateRequest
    ) {
        final SelectStatement selectStatement = this.queryTemplateService.findByName(
                runQueryTemplateRequest.getQueryTemplateName(),
                runQueryTemplateRequest.getVersion()
        );

        selectStatement.setOverrides(runQueryTemplateRequest.getOverrides());
        selectStatement.setCriteriaArguments(runQueryTemplateRequest.getCriteriaArguments());

        final String sql = this.sqlBuilderFactory
                .buildSqlBuilder(database)
                .withStatement(selectStatement)
                .build()
                .getSql();

        return ResponseEntity.ok(sql);
    }

//    @GetMapping(
//            value = "/query/{runnableQueryUuid}/status",
//            produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    public ResponseEntity<Map<QueryStatus.Status, OffsetDateTime>> getQueryStatus(@PathVariable UUID runnableQueryUuid) {
//        final IdentifiedQueryResult identifiedQueryResult = this.identifiedQueryResultService.getById(runnableQueryUuid);
//        return ResponseEntity.ok(identifiedQueryResult.getStatusStartTimes());
//    }

    @GetMapping(
            value = "/query/{runnableQueryUuid}/result",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<IdentifiedQueryResult> getIdentifiedQueryResult(@PathVariable UUID runnableQueryUuid) {
        IdentifiedQueryResult identifiedQueryResult = this.identifiedQueryResultService.getById(runnableQueryUuid);
        return ResponseEntity.ok(identifiedQueryResult);
    }

    /**
     * Query the database with the generated SQL sting.  Then add the select statement to the object so that the
     * encapsulating UI window has the full context, like column JDBC types.
     *
     * @param databaseName The database name to run the SQL against.
     * @param sql The SQL {@link String}.
     * @param selectStatement The {@link SelectStatement} that was used to generate the SQL.  Can be null.
     * @return {@link ResponseEntity<UUID>}
     */
    private ResponseEntity<UUID> runSql(String databaseName, String sql, SelectStatement selectStatement) throws IOException, TimeoutException {
        final RunnableQueryMessage result = this.runnableQueryPublisherService.publish(databaseName, selectStatement, sql);
        return ResponseEntity.ok(result.getUuid());
    }

    /**
     * Query the database with the generated SQL sting.  Then add the select statement to the object so that the
     * encapsulating UI window has the full context, like column JDBC types.
     *
     * @param databaseName The database name to run the SQL against.
     * @param sql The SQL {@link String}.
     * @return {@link ResponseEntity<QueryResult>}
     */
    private ResponseEntity<UUID> runSql(String databaseName, String sql) throws IOException, TimeoutException {
        return this.runSql(databaseName, sql, null);
    }

}
