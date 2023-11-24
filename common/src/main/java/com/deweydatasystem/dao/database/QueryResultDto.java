package com.deweydatasystem.dao.database;

import lombok.Data;

import java.sql.ResultSet;
import java.util.UUID;

/**
 * This class acts simply as a way to encapsulate all the necessary data to produce a {@link QueryResult} or
 * {@link IdentifiedQueryResult}.
 */
@Data
public class QueryResultDto {

    private final ResultSet resultSet;

    private final String sql;

    private UUID runnableQueryId;

}