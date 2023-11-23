package net.querybuilder4j.model.validator;

import lombok.NonNull;
import net.querybuilder4j.model.cte.CommonTableExpression;

public class CommonTableExpressionValidator extends SqlValidator<CommonTableExpression> {

    @Override
    public void isValid(@NonNull CommonTableExpression obj) {
        assertSqlIsClean(obj.getName());
    }

}
