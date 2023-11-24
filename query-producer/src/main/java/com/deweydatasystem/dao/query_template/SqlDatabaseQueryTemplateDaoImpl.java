package com.deweydatasystem.dao.query_template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.exceptions.JsonDeserializationException;
import com.deweydatasystem.exceptions.QueryTemplateNotFoundException;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.*;

@Repository
public class SqlDatabaseQueryTemplateDaoImpl implements QueryTemplateDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SqlDatabaseQueryTemplateDaoImpl(QbConfig qbConfig) {
        final DataSource dataSource = qbConfig.getQueryTemplateDataSource().getDataSource();
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean save(SelectStatement selectStatement) {
        final String saveQuery = "INSERT INTO qb4j.query_templates (name, version, query_json, discoverable, created_by, last_updated_by, database_name) " +
                "VALUES (:name, :version, :json, :discoverable, :createdBy, :lastUpdatedBy, :databaseName)";

        String json = Utils.serializeToJson(selectStatement);

        int numRowsInserted = this.jdbcTemplate.update(
                saveQuery,
                Map.of(
                        "name", selectStatement.getMetadata().getName(),
                        "version", selectStatement.getMetadata().getVersion(),
                        "json", json,
                        "discoverable", selectStatement.getMetadata().isDiscoverable(),
                        "createdBy", selectStatement.getMetadata().getAuthor(),
                        "lastUpdatedBy", selectStatement.getMetadata().getAuthor(),
                        "databaseName", selectStatement.getDatabase().getDatabaseName()
                )
        );

        return numRowsInserted > 0;
    }

    @Override
    public Set<String> listNames(String databaseName) {
        List<String> names = this.jdbcTemplate.queryForList(
                "SELECT DISTINCT name FROM qb4j.query_templates WHERE database_name = :databaseName AND discoverable = true ORDER BY name ASC",
                Map.of(
                        "databaseName", databaseName
                ),
                String.class
        );

        return Set.copyOf(names);
    }

    @Override
    public Optional<Integer> getNewestVersion(String name) {
        try {
            return Optional.ofNullable(
                    this.jdbcTemplate.queryForObject(
                            "SELECT version FROM qb4j.query_templates WHERE name = :name ORDER BY last_updated_ts DESC LIMIT 1",
                            Map.of("name", name),
                            Integer.class
                    )
            );
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }

    @Override
    public List<Integer> getVersions(String name) {
        Objects.requireNonNull(name, "name is null");

        return this.jdbcTemplate.queryForList(
                "SELECT version FROM qb4j.query_templates WHERE name = :name",
                Map.of("name", name),
                Integer.class
        );
    }

    @Override
    public SelectStatement.Metadata getMetadata(String name, int version) {
        Objects.requireNonNull(name, "name is null");

        return this.jdbcTemplate.queryForObject(
                "SELECT query_json FROM qb4j.query_templates WHERE name = :name AND version = :version",
                Map.of(
                        "name", name,
                        "version", version
                ),
                (resultSet, i) -> {
                    String metadataJson = resultSet.getString(1);
                    try {
                        SelectStatement selectStatement = this.objectMapper.readValue(metadataJson, SelectStatement.class);
                        return selectStatement.getMetadata();
                    } catch (JsonProcessingException e) {
                        throw new JsonDeserializationException("Could not deserialize " + name + " and version " + version);
                    }
                }
        );
    }

    @Override
    public SelectStatement findByName(String name, int version) {
        Objects.requireNonNull(name, "name is null");

        String json = this.jdbcTemplate.queryForObject(
                "SELECT query_json FROM qb4j.query_templates WHERE name = :name AND version = :version",
                Map.of(
                        "name", name,
                        "version", version
                ),
                String.class
        );

        if (json == null) {
            throw new QueryTemplateNotFoundException(name);
        }

        try {
            return this.objectMapper.readValue(json, SelectStatement.class);
        } catch (JsonProcessingException e) {
            throw new JsonDeserializationException("Could not deserialize JSON string for query template, " + name);
        }
    }

    // todo:  add this method back after producing an MVP.
//    @Override
//    public Map<String, SelectStatement> findByNames(List<String> names) {
//        // Check that the names parameter is not null or empty.
//        Objects.requireNonNull(names, "names is null");
//        if (names.isEmpty()) {
//            throw new IllegalArgumentException("names is empty");
//        }
//
//        // Create a parameter map and SQL WHERE clause.
//        Map<String, String> params = new HashMap<>();
//        for (int i=0; i<names.size(); i++) {
//            params.put("name" + i, names.get(i));
//        }
//        String sqlWhereClause = "WHERE name IN (" + params.keySet().stream().map(key -> ":" + key).collect(Collectors.joining(", ")) + ") ";
//
//        // Query the database.
//        String beginningQueryFragment = this.getFindByNamesSqlQueryStartingFragment();
//        Map<String, Object> retrievedObjects = this.jdbcTemplate.query(
//                beginningQueryFragment + sqlWhereClause,
//                params,
//                (resultSet, i) -> {
//                    System.out.println(i);
//                }
//        );
//
//        // Convert each object to a SelectStatement and return a map with the keys being the query template names and the
//        // values being the the query template's SelectStatement.
//        Map<String, SelectStatement> selectStatements = new HashMap<>();
//        for (Map.Entry<String, Object> entry : retrievedObjects.entrySet()) {
//            try {
//                SelectStatement selectStatement = this.objectMapper.readValue(entry.getValue().toString(), SelectStatement.class);
//                selectStatements.put(entry.getKey(), selectStatement);
//            } catch (JsonProcessingException e) {
//                throw new JsonDeserializationException("Could not deserialize JSON for query template, " + entry.getKey());
//            }
//        }
//
//        return selectStatements;
//    }

}
