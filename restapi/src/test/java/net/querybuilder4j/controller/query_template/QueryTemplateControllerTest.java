package net.querybuilder4j.controller.query_template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.querybuilder4j.TestUtils;
import net.querybuilder4j.exceptions.JsonDeserializationException;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.service.QueryTemplateService;
import net.querybuilder4j.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(QueryTemplateController.class)
public class QueryTemplateControllerTest {

    @MockBean
    private QueryTemplateService queryTemplateService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void getQueryTemplates_returns200() throws Exception {
        Set<String> queryTemplateNames = Set.of("queryTemplate1", "queryTemplate2");
        when(this.queryTemplateService.getNames(anyString()))
                .thenReturn(queryTemplateNames);

        this.mockMvc.perform(
                get("/api/v1/query-template?databaseName=myDatabase")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(queryTemplateNames)
                )
        );
    }

    @Test
    public void getQueryTemplateById_returns200() throws Exception {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        when(this.queryTemplateService.findByName(anyString(), anyInt()))
                .thenReturn(selectStatement);

        this.mockMvc.perform(
                get("/api/v1/query-template/name?version=0")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(selectStatement)
                )
        );
    }

    @Test
    public void saveQueryTemplate_returns200() throws Exception {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(new SelectStatement.Metadata());
        selectStatement.getMetadata().setName("bob");
        when(this.queryTemplateService.save(selectStatement))
                .thenReturn(true);

        this.mockMvc.perform(
                post("/api/v1/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                Utils.serializeToJson(selectStatement)
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        );
    }

    @Test
    public void saveQueryTemplate_returns400WhenMetadataIsNull() throws Exception {
        SelectStatement selectStatement = TestUtils.buildSelectStatement();
        selectStatement.setMetadata(null);
        when(this.queryTemplateService.save(selectStatement))
                .thenReturn(true);

        this.mockMvc.perform(
                post("/api/v1/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                this.objectMapper.writeValueAsString(selectStatement)
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(400)
        );
    }

    @Test
    public void getQueryTemplateVersions_returns200() throws Exception {
        List<Integer> versions = List.of(1, 2, 3);
        when(this.queryTemplateService.getVersions(anyString()))
                .thenReturn(versions);

        this.mockMvc.perform(
                get("/api/v1/query-template/name/versions")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(versions)
                )
        );
    }

    @Test
    public void getQueryTemplateMetadata_returns200() throws Exception {
        SelectStatement.Metadata metadata = new SelectStatement.Metadata();
        metadata.setName("bob");
        when(this.queryTemplateService.getMetadata(anyString(), anyInt()))
                .thenReturn(metadata);

        this.mockMvc.perform(
                get("/api/v1/query-template/name/metadata?version=0")
        ).andDo(
                print()
        ).andExpect(
                status().is(200)
        ).andExpect(
                content().json(
                        this.objectMapper.writeValueAsString(metadata)
                )
        );
    }

    @Test
    public void getQueryTemplateMetadata_returns500WhenServiceThrowsJsonDeserializationException() throws Exception {
        when(this.queryTemplateService.getMetadata(anyString(), anyInt()))
                .thenThrow(new JsonDeserializationException("something went horribly wrong!"));

        this.mockMvc.perform(
                get("/api/v1/query-template/name/metadata?version=0")
        ).andDo(
                print()
        ).andExpect(
                status().is(500)
        );
    }

    /**
     * This tests that the {@link net.querybuilder4j.exceptions.ExceptionMapper}'s default behavior of returning a 400 response
     * with an empty response body is overriden so that the response body is not null and is therefore more helpful to clients
     * to understand what the cause of the 400 response is.
     * <p>
     * Technically this test is not related to the {@link QueryTemplateController} class which is under test, but a {@link MockMvc}
     * is needed to test this {@link net.querybuilder4j.exceptions.ExceptionMapper} behavior, so this test has been added here.
     *
     * @throws Exception {@link Exception}
     */
    @Test
    public void nonSerializableRequestBodyReturnsNonNullResponseBody() throws Exception {
        this.mockMvc.perform(
                post("/api/v1/query-template")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                this.objectMapper.writeValueAsString(
                                        Map.of("firstName", "bob")
                                )
                        )
        ).andDo(
                print()
        ).andExpect(
                status().is(400)
        ).andExpect(
                jsonPath("$.message", notNullValue())
        );
    }

}