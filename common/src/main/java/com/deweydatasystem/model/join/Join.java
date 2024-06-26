package com.deweydatasystem.model.join;

import com.deweydatasystem.model.SqlRepresentation;
import lombok.Data;
import com.deweydatasystem.model.column.Column;
import com.deweydatasystem.model.table.Table;
import com.deweydatasystem.model.validator.SqlValidator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class Join implements SqlRepresentation, Serializable {

    private static final long serialVersionUID = 1L;

    private JoinType joinType;

    private Table parentTable;

    private Table targetTable;

    private List<Column> parentJoinColumns = new ArrayList<>();

    private List<Column> targetJoinColumns = new ArrayList<>();

    public enum JoinType {
        LEFT_EXCLUDING {
            @Override
            public String toString() {
                return " LEFT JOIN ";
            }
        },
        LEFT {
            @Override
            public String toString() {
                return " LEFT JOIN ";
            }
        },
        INNER {
            @Override
            public String toString() {
                return " INNER JOIN ";
            }
        },
        FULL_OUTER {
            @Override
            public String toString() {
                return " FULL OUTER JOIN ";
            }
        },
        FULL_OUTER_EXCLUDING {
            @Override
            public String toString() {
                return " FULL OUTER JOIN ";
            }
        },
        RIGHT_EXCLUDING {
            @Override
            public String toString() {
                return " RIGHT JOIN ";
            }
        },
        RIGHT {
            @Override
            public String toString() {
                return " RIGHT JOIN ";
            }
        }
    }

    @Override
    public String toSql(char beginningDelimiter, char endingDelimiter) {
        // Throw an exception if the size of parentJoinColumns and targetJoinColumns is not the same.
        if (this.getParentJoinColumns().size() != this.getTargetJoinColumns().size()) {
            final String joinColumnsSizeDiffMessage = "The parent and target join columns have differing number of elements";
            throw new RuntimeException(joinColumnsSizeDiffMessage);
        }

        // Build the SQL string representation starting with something like this:  " LEFT JOIN `schema`.`table`.`column` ",
        // assuming the target table has a schema - otherwise the string will be like this:  " LEFT JOIN `table`.`column` ".
        StringBuilder sb = new StringBuilder();
        if (this.targetTable.getSchemaName().equals("null")) {
            sb.append(this.getJoinType().toString())
                    .append(String.format(" %s%s%s ",
                            beginningDelimiter, SqlValidator.escape(this.getTargetTable().getTableName()), endingDelimiter)
                    );
        } else {
            sb.append(this.getJoinType().toString())
                    .append(String.format(" %s%s%s.%s%s%s ",
                            beginningDelimiter, SqlValidator.escape(this.getTargetTable().getSchemaName()), endingDelimiter,
                            beginningDelimiter, SqlValidator.escape(this.getTargetTable().getTableName()), endingDelimiter)
                    );
        }

        // For each of the parentJoinColumns, find the same index in targetJoinColumns and create a SQL string representation
        // like this:  " AND/ON `schema`.`parentTable`.`column` = `schema`.`targetTable`.`column` ", assuming the target
        // table has a schema - otherwise the string will be like this:  " AND/ON `parentTable`.`column` = `targetTable`.`column` ".
        for (int i=0; i<this.getParentJoinColumns().size(); i++) {
            String conjunction = (i == 0) ? "ON" : "AND";
            String parentJoinColumnSql = this.getParentJoinColumns().get(i).toSqlWithoutAlias(beginningDelimiter, endingDelimiter);
            String targetJoinColumnSql = this.getTargetJoinColumns().get(i).toSqlWithoutAlias(beginningDelimiter, endingDelimiter);

            sb.append(String.format(" %s %s = %s ", conjunction, parentJoinColumnSql, targetJoinColumnSql));
        }

        return sb.toString();
    }

}
