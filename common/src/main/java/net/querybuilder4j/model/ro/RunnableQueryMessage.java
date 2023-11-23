package net.querybuilder4j.model.ro;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.utils.ExcludeFromJacocoGeneratedReport;

import java.util.UUID;

@ExcludeFromJacocoGeneratedReport
@Data
@NoArgsConstructor
public class RunnableQueryMessage {

    private final UUID uuid = UUID.randomUUID();

    private String dataSourceName;

    private SelectStatement selectStatement;

    private String sql;

    public RunnableQueryMessage(
            String dataSourceName,
            SelectStatement selectStatement,
            String sql
    ) {
        this.dataSourceName = dataSourceName;
        this.selectStatement = selectStatement;
        this.sql = sql;
    }

}
