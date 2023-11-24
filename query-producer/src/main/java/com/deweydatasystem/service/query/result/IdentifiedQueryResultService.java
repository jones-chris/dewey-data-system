package com.deweydatasystem.service.query.result;

import com.deweydatasystem.dao.database.IdentifiedQueryResult;

import java.util.UUID;

public interface IdentifiedQueryResultService {

    IdentifiedQueryResult getById(UUID id);

    void save(IdentifiedQueryResult identifiedQueryResult);

}
