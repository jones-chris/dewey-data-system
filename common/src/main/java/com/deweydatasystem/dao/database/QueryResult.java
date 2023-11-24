package com.deweydatasystem.dao.database;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.exceptions.ResultSetDataExtractionException;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.utils.Utils;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is intended to take a {@link ResultSet} and SQL {@link String} and encapsulates the {@link ResultSet}'s
 * column names, data, and the SQL {@link String}.
 */
@Data
@NoArgsConstructor
@Slf4j
public class QueryResult implements Serializable {

    private static final long serialVersionUID = 2L;

    protected SelectStatement selectStatement;

    protected final List<String> columns = new ArrayList<>();

    protected final List<Object[]> data = new ArrayList<>();

    protected String sql;

    /**
     * Intended to hold the database message in the case that the SQL fails.
     */
    protected String message;

    public QueryResult(String message) {
        this.message = message;
    }

    /**
     * This constructor acts as a bridge between a {@link QueryResultDto} and the @{@link QueryResult#QueryResult(ResultSet, String)}.
     *
     * @param queryResultDto {@link QueryResultDto}
     * @throws ResultSetDataExtractionException Thrown if the {@link ResultSet} throws a {@link SQLException} while trying to extract
     * metadata or data.
     */
    public QueryResult(QueryResultDto queryResultDto) throws ResultSetDataExtractionException {
        this(queryResultDto.getResultSet(), queryResultDto.getSql());
    }

    public QueryResult(ResultSet resultSet, String sql) throws ResultSetDataExtractionException {
        Utils.requireNonNull(sql, "sql must not be null");
        this.sql = sql;

        // Set `columns` and `data` fields.
        boolean columnNamesRetrieved = false;

        try {
            int totalColumns = resultSet.getMetaData().getColumnCount();
            while (resultSet.next()) {
                // If column names have not been retrieved yet, build the json array before getting data.
                if (! columnNamesRetrieved) {
                    for (int i = 0; i < totalColumns; i++) {
                        this.columns.add(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase());
                    }

                    columnNamesRetrieved = true;
                }

                // Now get the row's data, put it in a json array, and add it to the json object.
                Object[] newRow = new Object[totalColumns];
                for (int i = 0; i < totalColumns; i++) {
                    newRow[i] = (resultSet.getObject(i + 1));
                }

                data.add(newRow);
            }
        }
        catch (SQLException e) {
            log.error("", e);
            throw new ResultSetDataExtractionException(e);
        }
    }

}
