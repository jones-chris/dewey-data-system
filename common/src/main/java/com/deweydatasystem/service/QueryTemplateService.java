package com.deweydatasystem.service;


import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.cte.CommonTableExpression;

import java.util.List;
import java.util.Set;

public interface QueryTemplateService {

    @LogExecutionTime
    boolean save(SelectStatement selectStatement);

    // todo:  add this method back after producing an MVP.
//    Map<String, SelectStatement> findByNames(List<String> names);

    @LogExecutionTime
    SelectStatement findByName(String name, int version);

    @LogExecutionTime
    Set<String> getNames(String databaseName);

    @LogExecutionTime
    void getCommonTableExpressionSelectStatement(List<CommonTableExpression> commonTableExpressions);

    @LogExecutionTime
    List<Integer> getVersions(String name);

    @LogExecutionTime
    SelectStatement.Metadata getMetadata(String name, int version);

}
