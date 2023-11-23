package net.querybuilder4j.dao.query_template;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.model.SelectStatement;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QueryTemplateDao {

    @LogExecutionTime
    SelectStatement findByName(String name, int version);

    // todo:  add this method back after producing an MVP.
//    Map<String, SelectStatement> findByNames(List<String> names);

    @LogExecutionTime
    boolean save(SelectStatement selectStatement);

    @LogExecutionTime
    Set<String> listNames(String databaseName);

    @LogExecutionTime
    Optional<Integer> getNewestVersion(String name);

    @LogExecutionTime
    List<Integer> getVersions(String name);

    @LogExecutionTime
    SelectStatement.Metadata getMetadata(String name, int version);

}
