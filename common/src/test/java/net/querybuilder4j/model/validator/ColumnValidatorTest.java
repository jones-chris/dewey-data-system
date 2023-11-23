package net.querybuilder4j.model.validator;

import net.querybuilder4j.dao.database.DatabaseMetadataCacheDao;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.column.Column;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class ColumnValidatorTest {

    @Mock
    private DatabaseMetadataCacheDao databaseMetadataCacheDao;

    @InjectMocks
    private ColumnValidator columnValidator;

    @Before
    public void beforeEach() {
        when(this.databaseMetadataCacheDao.columnsExist(anyList()))
                .thenReturn(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isValid_emptyColumnsThrowsException() {
        SelectStatement selectStatement = new SelectStatement();

        this.columnValidator.isValid(selectStatement.getColumns());
    }

    @Test
    public void isValid_nonEmptyColumnsPasses() {
        SelectStatement selectStatement = new SelectStatement();
        selectStatement.getColumns().add(
                new Column()
        );

        this.columnValidator.isValid(selectStatement.getColumns());
    }

    @Test
    public void isValid_throwsExceptionWhenColumnsDoNotExist() {
        assertThrows(
                IllegalStateException.class,
                () -> {
                    when(this.databaseMetadataCacheDao.columnsExist(anyList()))
                            .thenReturn(false);
                    var selectStatement = new SelectStatement();
                    selectStatement.getColumns().add(
                            new Column()
                    );

                    this.columnValidator.isValid(selectStatement.getColumns());
                }
        );
    }

}