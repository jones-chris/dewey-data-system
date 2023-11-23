package net.querybuilder4j.model.validator;

import lombok.extern.slf4j.Slf4j;
import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.column.Column;
import net.querybuilder4j.utils.Utils;

import java.util.List;

@Slf4j
public class ColumnValidator extends SqlValidator<List<Column>> {

    private final DatabaseMetadataCacheDao databaseMetadataCacheDao;

    public ColumnValidator(DatabaseMetadataCacheDao databaseMetadataCacheDao) {
        this.databaseMetadataCacheDao = databaseMetadataCacheDao;
    }

    @Override
    public void isValid(List<Column> columns) {
        Utils.requireNonEmpty(columns, "columns cannot be empty");

        // Validate all columns exist.
        boolean columnsExist = this.databaseMetadataCacheDao.columnsExist(columns);
        if (! columnsExist) {
            final String message = "A column in the Select Statement's criteria or select clause does not exist";

            log.error(message);
            throw new IllegalStateException(message);
        }
    }

}
