package net.querybuilder4j.model.validator;

import lombok.NonNull;
import net.querybuilder4j.model.table.Table;
import net.querybuilder4j.utils.Utils;

public class TableValidator extends SqlValidator<Table> {

    @Override
    public void isValid(@NonNull Table table) {
        Utils.requireNonNull(table, "table cannot be null");
    }

}
