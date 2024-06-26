package com.deweydatasystem.utils;

import com.deweydatasystem.exceptions.CriterionColumnDataTypeAndFilterMismatchException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.deweydatasystem.exceptions.JsonDeserializationException;
import com.deweydatasystem.exceptions.JsonSerializationException;
import com.deweydatasystem.exceptions.SqlTypeNotRecognizedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

public class Utils {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

    /**
     * Checks that the given {@link Collection} is not empty.  If it is empty, then it will throw a {@link IllegalArgumentException}
     * with the exception message being the message parameter.
     *
     * @param collection {@link Collection}
     * @param message The message to pass to the {@link IllegalArgumentException} if the collection is empty.
     * @param <T> The type of the elements in the {@link Collection}.
     * @return The collection.
     */
    public static <T extends Collection<?>> T requireNonEmpty(T collection, String message) {
        if (collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return collection;
    }

    /**
     * This method behaves the same way as {@link Objects#requireNonNull(Object, String)}, but it throws an {@link IllegalArgumentException}
     * instead of a {@link NullPointerException} if the object is null.
     *
     * @param obj The object to check whether it's null.
     * @param message The message to pass into the {@link IllegalArgumentException} if the object is null.
     * @param <T> The type of the object.
     * @return The object.
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }

        return obj;
    }

    /**
     * First, gets the SQL JDBC Type for the table and column parameters.  Then, gets a boolean from the typeMappings
     * class field associated with the SQL JDBC Types parameter, which is an int.  The typeMappings field will return
     * true if the SQL JDBC Types parameter should be quoted in a WHERE SQL clause and false if it should not be quoted.
     *
     * For example, the VARCHAR Type will return true, because it should be wrapped in single quotes in a WHERE SQL condition.
     * On the other hand, the INTEGER Type will return false, because it should NOT be wrapped in single quotes in a WHERE SQL condition.
     *
     * @param dataType The data type to inquire as to whether column members with this data type should be wrapped in single
     *                 quotes.
     * @return boolean
     */
    public static boolean shouldBeQuoted(int dataType) {
        /*
         * A Map with the keys being JDBC Types and the values being booleans.  The boolean values represent whether filters
         * with the given JDBC type should be quoted.  For example, if a column's JDBC type is INTEGER, then the value in the
         * typeMappings field is false, because integers do not need to be wrapped in quotes in a SQL WHERE clause.  On the
         * other hand, if a column's JDBC type is NVARCHAR, then the value in the typeMappings field is true, because varchar's
         * do not need to be wrapped in quotes in a SQL WHERE clause.
         */
        final Map<Integer, Boolean> TYPE_MAPPINGS = new HashMap<>() {{
            put(Types.ARRAY, true);
            put(Types.BIGINT, false);
            put(Types.BINARY, true);
            put(Types.BIT, false);
            put(Types.BLOB, true);
            put(Types.BOOLEAN, false);
            put(Types.CHAR, true);
            put(Types.CLOB, true);
//        put(Types.DATALINK, false);
            put(Types.DATE, true);
            put(Types.DECIMAL, true);
            put(Types.DISTINCT, true);
            put(Types.DOUBLE, false);
            put(Types.FLOAT, false);
            put(Types.INTEGER, false);
            put(Types.JAVA_OBJECT, true);
            put(Types.LONGNVARCHAR, true);
            put(Types.LONGVARBINARY, true);
            put(Types.LONGVARCHAR, true);
            put(Types.NCHAR, true);
            put(Types.NCLOB, true);
            put(Types.NULL, true);
            put(Types.NUMERIC, false);
            put(Types.NVARCHAR, true);
            put(Types.OTHER, true);
            put(Types.REAL, false);
            put(Types.REF, true);
            put(Types.REF_CURSOR, true);
            put(Types.ROWID, false);
            put(Types.SMALLINT, false);
            put(Types.SQLXML, true);
            put(Types.STRUCT, true);
            put(Types.TIME, true);
            put(Types.TIME_WITH_TIMEZONE, true);
            put(Types.TIMESTAMP, true);
            put(Types.TIMESTAMP_WITH_TIMEZONE, true);
            put(Types.TINYINT, false);
            put(Types.VARBINARY, true);
            put(Types.VARCHAR, true);
        }};

        Boolean isQuoted = TYPE_MAPPINGS.get(dataType);

        if (isQuoted == null) {
            throw new SqlTypeNotRecognizedException(dataType);
        }

        return isQuoted;
    }

    public static String getJdbcSqlType(int jdbcType) {
        if (jdbcType == 2003) { return "ARRAY"; }
        else if (jdbcType == -5) { return "BIG_INT"; }
        else if (jdbcType == -2) { return "BINARY"; }
        else if (jdbcType == -7) { return "BIT"; }
        else if (jdbcType == 2004) { return "BLOB"; }
        else if (jdbcType == 16) { return "BOOLEAN"; }
        else if (jdbcType == 1) { return "CHAR"; }
        else if (jdbcType == 2005) { return "CLOB"; }
        else if (jdbcType == 70) { return "DATA_LINK"; }
        else if (jdbcType == 91) { return "DATE"; }
        else if (jdbcType == 3) { return "DECIMAL"; }
        else if (jdbcType == 2001) { return "DISTINCT"; }
        else if (jdbcType == 8) { return "DOUBLE"; }
        else if (jdbcType == 6) { return "FLOAT"; }
        else if (jdbcType == 4) { return "INTEGER"; }
        else if (jdbcType == -16) { return "LONGNVARCHAR"; }
        else if (jdbcType == -1) { return "LONGVARCHAR"; }
        else if (jdbcType == -15) { return "NCHAR"; }
        else if (jdbcType == 2) { return "NUMERIC"; }
        else if (jdbcType == -9) { return "NVARCHAR"; }
        else if (jdbcType == 5) { return "SMALL_INT"; }
        else if (jdbcType == 92) { return "TIME"; }
        else if (jdbcType == 2013) { return "TIME_WITH_TIMEZONE"; }
        else if (jdbcType == 93) { return "TIMESTAMP"; }
        else if (jdbcType == 2014) { return "TIMESTAMP_WITH_TIMEZONE"; }
        else if (jdbcType == -6) { return "TINY_INT"; }
        else if (jdbcType == 12) { return "VARCHAR"; }
        else if (jdbcType == 1111) { return "OTHER"; } // UUID will have this type.
        else { throw new SqlTypeNotRecognizedException(jdbcType); }
    }

    /**
     * A convenience method for instantiating a {@link T} from a JSON {@link String}.
     *
     * @param json The JSON {@link String}.
     * @param clazz The class to instantiate from the JSON {@link String}.
     * @param <T> The class to instantiate from the JSON {@link String}.
     * @return An instance of {@link T}.
     */
    public static <T> T deserializeJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            /*
             * Instantiate and throw a child of the RuntimeException class so we don't have to worry about exception checking
             * in the event that a JSON string cannot be deserialized because the application is not expected to recover
             * from that.
             */
            throw new JsonDeserializationException(e);
        }
    }

    /**
     * A convenience method for instantiating a {@link List <T>} from a {@link Iterable<String>}.
     *
     * @param jsons The {@link Iterable<String>} of JSON {@link String}s.
     * @param clazz The class to instantiate from each of the JSON {@link String}s.
     * @param <T> The class to instantiate from each of the JSON {@link String}s.
     * @return A {@link List<T>}.
     */
    public static <T> List<T> deserializeJsons(Iterable<String> jsons, Class<T> clazz) {
        List<T> deserializedObjects = new ArrayList<>();
        jsons.forEach(json -> deserializedObjects.add(
                deserializeJson(json, clazz)
        ));

        return deserializedObjects;
    }

    public static String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonSerializationException(e);
        }
    }

    /**
     * Converts an {@link Object} to a {@link byte[]}
     *
     * @param obj The {@link Object} to perform the conversion on.
     * @return {@link byte[]}
     * @throws IOException If an I/O error occurs while writing stream header.
     */
    public static byte[] toByteArray(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(obj);

        return bos.toByteArray();
    }

    public static boolean isOfJdbcType(String value, int jdbcDataType) {
        if (jdbcDataType == Types.BIGINT || jdbcDataType == Types.DECIMAL || jdbcDataType == Types.DOUBLE ||
                jdbcDataType == Types.FLOAT || jdbcDataType == Types.INTEGER || jdbcDataType == Types.NUMERIC ||
                jdbcDataType == Types.SMALLINT || jdbcDataType == Types.TINYINT) {
            try {
                BigDecimal.valueOf(Double.parseDouble(value));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else if (jdbcDataType == Types.BOOLEAN) {
            return Boolean.TRUE.toString().toLowerCase().equals(value) || Boolean.FALSE.toString().toLowerCase().equals(value);
        }

        // If JDBC type is another type, such as a String, then return true.
        return true;
    }

}
