package net.querybuilder4j.service.messaging;

import lombok.extern.slf4j.Slf4j;
import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.dao.database.IdentifiedQueryResult;
import net.querybuilder4j.dao.database.IdentifiedQueryResult.Status;
import net.querybuilder4j.dao.messaging.RunnableQueryPublisherDao;
import net.querybuilder4j.dao.query.GenericDao;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.ro.RunnableQueryMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
public class RabbitMqRunnableQueryServiceImpl implements RunnableQueryPublisherService {

    private RunnableQueryPublisherDao runnableQueryPublisherDao;

//    private QueryStatusService queryStatusService;

    private GenericDao<IdentifiedQueryResult, UUID> identifiedQueryResultDao;

    @Autowired
    public RabbitMqRunnableQueryServiceImpl(
            RunnableQueryPublisherDao runnableQueryPublisherDao,
            GenericDao<IdentifiedQueryResult, UUID> identifiedQueryResultDao
//            QueryStatusService queryStatusService
    ) {
        this.runnableQueryPublisherDao = runnableQueryPublisherDao;
        this.identifiedQueryResultDao = identifiedQueryResultDao;
//        this.queryStatusService = queryStatusService;
    }

    @Override
    @LogExecutionTime
    public RunnableQueryMessage publish(String databaseName, SelectStatement selectStatement, String sql) throws IOException, TimeoutException {
        RunnableQueryMessage runnableQueryMessage = new RunnableQueryMessage(
                databaseName,
                selectStatement,
                sql
        );

        IdentifiedQueryResult identifiedQueryResult = new IdentifiedQueryResult(runnableQueryMessage.getUuid());
        identifiedQueryResult.updateStatusStartTime(Status.BUILT);
//        QueryStatus queryStatus = new QueryStatus(
//                runnableQueryMessage.getUuid(),
//                QueryStatus.Status.BUILT
//        );
//        this.queryStatusService.save(queryStatus);
        this.identifiedQueryResultDao.save(identifiedQueryResult);

        runnableQueryMessage = this.runnableQueryPublisherDao.publish(runnableQueryMessage);

//        queryStatus.setStatus(QueryStatus.Status.QUEUED);
//        this.queryStatusService.save(queryStatus);

        identifiedQueryResult.updateStatusStartTime(Status.QUEUED);
        this.identifiedQueryResultDao.save(identifiedQueryResult);

        return runnableQueryMessage;
    }

}
