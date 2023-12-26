package com.deweydatasystem;

import com.deweydatasystem.model.RunnableSql;
import com.deweydatasystem.model.SqlParameter;
import com.deweydatasystem.model.criterion.Utils;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public class ParameterizedSqlInterpolator {

    /**
     * Interpolates the given runtime arguments into a parameterized SQL {@link String} and returns the runnable SQL
     * {@link String}.
     *
     * @return The interpolated SQL {@link String}.
     */
    public static String interpolate(@NonNull RunnableSql runnableSql) {
        String interpolatedSql = runnableSql.getSql();

        // Check that there are no parameters with empty string values.
        runnableSql.getSqlParameters().stream()
                .filter(sqlParameter -> sqlParameter.getArguments().contains(""))
                .findFirst()
                .ifPresent(sqlParameter -> {
                    throw new IllegalStateException("One of the runtime arguments is an empty string: " + sqlParameter.getParameterName());
                });

        // Replace the parameter placeholders with the runtime arguments.
        for (SqlParameter sqlParameter : runnableSql.getSqlParameters()) {
            final String criterionParameter = sqlParameter.getParameterName();
            final List<String> arguments = sqlParameter.getArguments();

            final String placeholder = "?" + criterionParameter;

            // Get the data type associated with the argument placeholder/criterion parameter, so we can know
            // whether to wrap each argument's values in quotes.
            final int argDataType = sqlParameter.getJdbcType();

            // Wrap the argument's values.
            final List<String> wrappedArguments = Utils.wrapArgsInQuotes(arguments, argDataType);

            // Join the argument's values and wrap all the values in parentheses.
            final String joinedRuntimeArguments = "(" + String.join(", ", wrappedArguments) + ")";

            // Interpolate the values into the parameterized SQL.
            interpolatedSql = interpolatedSql.replaceAll(placeholder, joinedRuntimeArguments);
        }

        return interpolatedSql;
    }

}
