export class QueryParameter {

    name = null;

    dataType = 'oneString';

    constraintType = 'noConstraint';

    constraint = null;
    
    value = [];

}

export class NumberRangeConstraint {

    minimum = 0;

    maximum = 100;

}

export class ColumnConstraint {
    
    column = null

}

export class EnumConstraint {

    enumValues = [];

}