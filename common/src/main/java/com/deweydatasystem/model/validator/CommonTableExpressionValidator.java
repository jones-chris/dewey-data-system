package com.deweydatasystem.model.validator;

import com.deweydatasystem.model.cte.CommonTableExpression;
import lombok.NonNull;

public class CommonTableExpressionValidator extends SqlValidator<CommonTableExpression> {

    @Override
    public void isValid(@NonNull CommonTableExpression obj) {
        assertSqlIsClean(obj.getName());
    }

}
