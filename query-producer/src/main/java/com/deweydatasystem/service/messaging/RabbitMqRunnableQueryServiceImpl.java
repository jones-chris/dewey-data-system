package com.deweydatasystem.service.messaging;

import com.deweydatasystem.dao.messaging.RunnableQueryPublisherDao;
import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.dao.database.IdentifiedQueryResult;
import com.deweydatasystem.dao.database.IdentifiedQueryResult.Status;
import com.deweydatasystem.dao.query.GenericDao;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.ro.RunnableQueryMessage;
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
