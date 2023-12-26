package com.deweydatasystem.model.criterion;

import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.validator.SqlValidator;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    /**
     * Wrap a single argument {@link String} in quotes based on the associated {@link Column}.
     *
     * @param arg {@link String}
     * @param columnDataType {@link int}
     * @return {@link String}
     */
    public static String wrapArgInQuotes(String arg, int columnDataType) {
        final String escapedArg = SqlValidator.escape(arg);

        boolean shouldHaveQuotes = com.deweydatasystem.utils.Utils.shouldBeQuoted(columnDataType);
        // If the argument's values should be wrapped in quotes, then do so...
        if (shouldHaveQuotes) {
            return String.format("'%s'", escapedArg);
        }

        // ...else, do not wrap the filter item in quotes.
        return escapedArg;
    }

    /**
     * Wrap each argument {@link String} in quotes based on the associated {@link Column}.
     *
     * @param args {@link List <String>}
     * @param columnDataType {@link int}
     * @return {@link List<String>} containing the wrapped argument values.
     */
    public static List<String> wrapArgsInQuotes(List<String> args, int columnDataType) {
        final List<String> newArgs = new ArrayList<>();

        for (String arg : args) {
            final String newArg = wrapArgInQuotes(arg, columnDataType);
            newArgs.add(newArg);
        }

        return newArgs;
    }

}