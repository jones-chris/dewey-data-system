package net.querybuilder4j.service.query.result;

import net.querybuilder4j.dao.database.IdentifiedQueryResult;
import net.querybuilder4j.dao.query.GenericDao;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class IdentifiedQueryResultServiceImpl implements IdentifiedQueryResultService {

    private final GenericDao<IdentifiedQueryResult, UUID> identifiedQueryResultDao;

    public IdentifiedQueryResultServiceImpl(GenericDao<IdentifiedQueryResult, UUID> identifiedQueryResultDao) {
        this.identifiedQueryResultDao = identifiedQueryResultDao;
    }

    @Override
    public IdentifiedQueryResult getById(UUID id) {
        return this.identifiedQueryResultDao.getById(id);
    }

    @Override
    public void save(IdentifiedQueryResult identifiedQueryResult) {
        this.identifiedQueryResultDao.save(identifiedQueryResult);
    }

}
