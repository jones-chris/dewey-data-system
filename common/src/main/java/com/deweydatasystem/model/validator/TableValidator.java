package com.deweydatasystem.model.validator;

import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.utils.Utils;
import lombok.NonNull;

public class TableValidator extends SqlValidator<Table> {

    @Override
    public void isValid(@NonNull Table table) {
        Utils.requireNonNull(table, "table cannot be null");
    }

}
