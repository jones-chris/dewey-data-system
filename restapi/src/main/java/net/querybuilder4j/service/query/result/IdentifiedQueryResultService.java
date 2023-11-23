package net.querybuilder4j.service.query.result;

import net.querybuilder4j.dao.database.IdentifiedQueryResult;

import java.util.UUID;

public interface IdentifiedQueryResultService {

    IdentifiedQueryResult getById(UUID id);

    void save(IdentifiedQueryResult identifiedQueryResult);

}
