package com.deweydatasystem.model;

import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.utils.Utils;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class SqlParameter {

    @NonNull
    private final String parameterName;

    @NonNull
    private final int jdbcType;

    @NonNull
    private final List<String> arguments = new ArrayList<>();

    @NonNull
    private final Constraint constraint;

    @Data
    public static class ColumnConstraint implements Constraint {

        private final Column column;

        @Override
        public boolean isSatisfied(List<String> arguments) {
            for (String argument : arguments) {
                if (! Utils.isOfJdbcType(argument, column.getDataType())) {
                    return false;
                }
            }

            return true;
        }
    }

    @Data
    @Slf4j
    public static class NumberRangeConstraint implements Constraint {

        /**
         * The maximum number (inclusive).
         */
        private final int ceiling;

        /**
         * The minimum number (inclusive).
         */
        private final int floor;

        @Override
        public boolean isSatisfied(List<String> arguments) {
            if (arguments.isEmpty()) {
                return false;
            }

            for (String argument : arguments) {
                try {
                    int argumentInt = Integer.parseInt(argument);
                    if (argumentInt <= this.floor || this.ceiling <= argumentInt) {
                        return false;
                    }
                }
                catch (NumberFormatException e) {
                    log.error("", e);
                    return false;
                }
            }

            return true;
        }
    }

    @Data
    @Slf4j
    public static class EnumConstraint implements Constraint {
        private List<String> enumValues = new ArrayList<>();

        @Override
        public boolean isSatisfied(List<String> arguments) {
            // Make sure enumValues is a lower case list.
            enumValues = enumValues.stream().map(String::toLowerCase).collect(Collectors.toList());

            for (String argument : arguments) {
                try {
                    if (! enumValues.contains(argument.toLowerCase())) {
                        return false;
                    }
                }
                catch (ClassCastException | NullPointerException e) {
                    log.error("", e);
                    return false;
                }
            }

            return true;
        }

    }

}
