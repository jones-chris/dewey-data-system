package com.deweydatasystem.controller.database.data;

import com.deweydatasystem.TestUtils;
import com.deweydatasystem.exceptions.QueryTemplateNotFoundException;
import com.deweydatasystem.ro.RunQueryTemplateRequest;
import com.deweydatasystem.service.database.data.DatabaseDataService;
import com.deweydatasystem.service.messaging.RunnableQueryPublisherService;
import com.deweydatasystem.service.query.result.IdentifiedQueryResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deweydatasystem.dao.database.QueryResult;
import com.deweydatasystem.exceptions.QueryFailureException;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.ro.RunnableQueryMessage;
import com.deweydatasystem.service.QueryTemplateService;
import com.deweydatasystem.SqlBuilder;
import com.deweydatasystem.SqlBuilderFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(DatabaseDataController.class)
public class DatabaseDataControllerTest {

    @MockBean
    private DatabaseDataService databaseDataService;

    @MockBean
    private SqlBuilderFactory sqlBuilderFactory;

    @MockBean
    private QueryTemplateService queryTemplateService;

    @MockBean
    private RunnableQueryPublisherService runnableQueryPublisherService;

    @MockBean
    private IdentifiedQueryResultService identifiedQueryResultService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getColumnMembers_returns200IfNoSearchQueryParameter() throws Exception {
        ResultSet resultSet = this.buildMockResultSet();
        QueryResult queryResult = new QueryResult(resultSet, "SELECT * FROM table");
        doReturn(queryResult)
                .when(this.databaseDataService)
                .getColumnMembers(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyInt(),
                        anyInt(),
                        anyBoolean(),
                        any()
                );

        this.mockMvc.perform(
                get("/api/v1/data/database/schema/table/column/column-member?limit=5&offset=0&ascending=true")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                jsonPath("$.sql", notNullValue())
        ).andExpect(
                jsonPath("$.columns", notNullValue())
        ).andExpect(
                jsonPath("$.data", notNullValue())
        ).andExpect(
                jsonPath("$.selectStatement", nullValue())
        );
    }

    @Test
    public void getColumnMembers_returns200IfSearchQueryParameter() throws Exception {
        ResultSet resultSet = this.buildMockResultSet();
        QueryResult queryResult = new QueryResult(resultSet, "SELECT * FROM table");
        doReturn(queryResult)
                .when(this.databaseDataService)
                .getColumnMembers(
                        anyString(),
                        anyString(),
                        anyString(),
                        anyString(),
                        anyInt(),
                        anyInt(),
                        anyBoolean(),
                        any()
                );

        this.mockMvc.perform(
                get("/api/v1/data/database/schema/table/column/column-member?limit=5&offset=0&ascending=true&search=mySearchText")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                jsonPath("$.sql", notNullValue())
        ).andExpect(
                jsonPath("$.columns", notNullValue())
        ).andExpect(
                jsonPath("$.data", notNullValue())
        ).andExpect(
                jsonPath("$.selectStatement", nullValue())
        );
    }

    @Test
    public void getColumnMembers_exceptionCauses500ToBeReturned() throws Exception {
        when(this.databaseDataService.getColumnMembers(anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenThrow(
                        new QueryFailureException(
                                new Exception(),
                                "SELECT * FROM table"
                        )
                );

        this.mockMvc.perform(
                get("/api/v1/data/database/schema/table/column/column-member?limit=5&offset=0&ascending=true&search=mySearchText")
        ).andDo(
                print()
        ).andExpect(
                status().is5xxServerError()
        );
    }

    @Test
    public void getQueryResults_returns200IfQueryIsRunSuccessfully() throws Exception {
        final String expectedSql = "SELECT * FROM table";
        SqlBuilder sqlBuilder = TestUtils.buildSqlBuilderMock(expectedSql);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        RunnableQueryMessage runnableQueryMessage = new RunnableQueryMessage("databaseName", new SelectStatement(), "sql");
        when(this.runnableQueryPublisherService.publish(anyString(), any(SelectStatement.class), anyString()))
                .thenReturn(runnableQueryMessage);

        this.mockMvc.perform(
                post("/api/v1/data/database/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(TestUtils.buildSelectStatement())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().string(TestUtils.UuidMatcher.isUuid())
        );
    }

    @Test
    public void getQueryResult_exceptionCauses500ToBeReturned() throws Exception {
        final String expectedSql = "SELECT * FROM table";
        SqlBuilder sqlBuilder = TestUtils.buildSqlBuilderMock(expectedSql);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        when(this.runnableQueryPublisherService.publish(anyString(), any(SelectStatement.class),anyString()))
                .thenThrow(TimeoutException.class);

        this.mockMvc.perform(
                post("/api/v1/data/database/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(TestUtils.buildSelectStatement())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is5xxServerError()
        );
    }

    @Test
    public void runRawSql_returns200IfRawSqlIsRunSuccessfully() throws Exception {
        final String expectedSql = "SELECT * FROM table";
        final QueryResult queryResult = new QueryResult(
                this.buildMockResultSet(),
                expectedSql
        );
        RunnableQueryMessage runnableQueryMessage = new RunnableQueryMessage("databaseName", new SelectStatement(), "sql");
        when(this.runnableQueryPublisherService.publish(any(), any(), any()))
                .thenReturn(runnableQueryMessage);

        this.mockMvc.perform(
                post("/api/v1/data/database/query/raw")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content(expectedSql)
        ).andDo(
                print()
        ).andExpect(
                status().is2xxSuccessful()
        ).andExpect(
                content().string(TestUtils.UuidMatcher.isUuid())
        );
    }

    @Test
    public void runRawSql_exceptionCauses500ToBeReturned() throws Exception {
        doAnswer(invocationOnMock -> {
            throw new TimeoutException();
        }).when(this.runnableQueryPublisherService).publish(any(), any(), any());

        this.mockMvc.perform(
                post("/api/v1/data/database/query/raw")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("SELECT * FROM table")
        ).andDo(
                print()
        ).andExpect(
                status().is5xxServerError()
        ).andExpect(
                jsonPath("$.message", notNullValue())
        );
    }

    @Test
    public void runRawSql_destructiveSqlCauses400ToBeReturned() throws Exception {
        this.mockMvc.perform(
                post("/api/v1/data/database/query/raw")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("SELECT * FROM table; DROP TABLE table; --")
        ).andDo(
                print()
        ).andExpect(
                status().is4xxClientError()
        );
    }

    @Test
    public void runQueryTemplate_returns200IfQueryTemplateIsRunSuccessfully() throws Exception {
        final String expectedSql = "SELECT * FROM table";
        SqlBuilder sqlBuilder = TestUtils.buildSqlBuilderMock(expectedSql);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        when(this.queryTemplateService.findByName(any(), anyInt()))
                .thenReturn(TestUtils.buildSelectStatement());
        QueryResult queryResult = new QueryResult(
                this.buildMockResultSet(),
                expectedSql
        );
        RunnableQueryMessage runnableQueryMessage = new RunnableQueryMessage("databaseName", new SelectStatement(), "sql");
        when(this.runnableQueryPublisherService.publish(anyString(), any(SelectStatement.class),anyString()))
                .thenReturn(runnableQueryMessage);

        this.mockMvc.perform(
                post("/api/v1/data/database/query/query-template")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                            new ObjectMapper().writeValueAsString(this.buildRunQueryTemplateRequest())
                    )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().string(TestUtils.UuidMatcher.isUuid())
        );
    }

    @Test
    public void runQueryTemplate_returns500IfQueryTemplateNotRunSuccessfully() throws Exception {
        final String expectedSql = "SELECT * FROM table";
        SqlBuilder sqlBuilder = TestUtils.buildSqlBuilderMock(expectedSql);
        when(this.sqlBuilderFactory.buildSqlBuilder(any()))
                .thenReturn(sqlBuilder);
        when(this.queryTemplateService.findByName(any(), anyInt()))
                .thenReturn(TestUtils.buildSelectStatement());
        when(this.runnableQueryPublisherService.publish(anyString(), any(SelectStatement.class),anyString()))
                .thenThrow(TimeoutException.class);

        this.mockMvc.perform(
                post("/api/v1/data/database/query/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(this.buildRunQueryTemplateRequest())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(500)
        );
    }

    @Test
    public void runQueryTemplate_returns404IfQueryTemplateIsNotFound() throws Exception {
        when(this.queryTemplateService.findByName(any(), anyInt()))
                .thenThrow(QueryTemplateNotFoundException.class);

        this.mockMvc.perform(
                post("/api/v1/data/database/query/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(this.buildRunQueryTemplateRequest())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(404)
        );
    }

    @Test
    public void getSelectStatementSql_returns200AndSqlInResponseBody() throws Exception {
        final String expectedSql = "select * from table";
        final SqlBuilder sqlBuilder = mock(SqlBuilder.class);
        doReturn(sqlBuilder)
                .when(this.sqlBuilderFactory).buildSqlBuilder(anyString());
        doReturn(sqlBuilder)
                .when(sqlBuilder).withStatement(any());
        doReturn(sqlBuilder)
                .when(sqlBuilder).build();
        when(sqlBuilder.getParameterizedSql())
                .thenReturn(expectedSql);

        this.mockMvc.perform(
                post("/api/v1/data/database/query/dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(TestUtils.buildSelectStatement())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().string(expectedSql)
        ).andExpect(
                content().contentType("text/plain;charset=UTF-8")
        );
    }

    @Test
    public void getQueryTemplateSql_returns200AndSqlInResponseBody() throws Exception {
        final String expectedSql = "select * from table";
        final SqlBuilder sqlBuilder = mock(SqlBuilder.class);
        when(this.queryTemplateService.findByName(any(), anyInt()))
                .thenReturn(TestUtils.buildSelectStatement());
        doReturn(sqlBuilder)
                .when(this.sqlBuilderFactory).buildSqlBuilder(anyString());
        doReturn(sqlBuilder)
                .when(sqlBuilder).withStatement(any());
        doReturn(sqlBuilder)
                .when(sqlBuilder).build();
        when(sqlBuilder.getParameterizedSql())
                .thenReturn(expectedSql);

        this.mockMvc.perform(
                post("/api/v1/data/database/query/query-template/dry-run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                new ObjectMapper().writeValueAsString(TestUtils.buildSelectStatement())
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().string(expectedSql)
        ).andExpect(
                content().contentType("text/plain;charset=UTF-8")
        );
    }

    private ResultSet buildMockResultSet() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next())
                .thenReturn(true, false);
        when(resultSet.getObject(anyInt()))
                .thenReturn("bob", "joe");

        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData())
                .thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount())
                .thenReturn(2);
        when(resultSetMetaData.getColumnLabel(anyInt()))
                .thenReturn("column1", "column2");

        return resultSet;
    }

    private RunQueryTemplateRequest buildRunQueryTemplateRequest() {
        RunQueryTemplateRequest runQueryTemplateRequest = new RunQueryTemplateRequest();
        runQueryTemplateRequest.setQueryTemplateName("name");
        runQueryTemplateRequest.setVersion(0);

        return runQueryTemplateRequest;
    }

}