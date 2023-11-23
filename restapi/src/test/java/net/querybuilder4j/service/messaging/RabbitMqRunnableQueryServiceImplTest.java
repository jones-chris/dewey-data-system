package net.querybuilder4j.service.messaging;

import net.querybuilder4j.TestUtils;
import net.querybuilder4j.dao.database.IdentifiedQueryResult;
import net.querybuilder4j.dao.messaging.RunnableQueryPublisherDao;
import net.querybuilder4j.dao.query.GenericDao;
import net.querybuilder4j.model.ro.RunnableQueryMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqRunnableQueryServiceImplTest {

    @Mock
    private RunnableQueryPublisherDao runnableQueryPublisherDao;

    @Mock
    private GenericDao<IdentifiedQueryResult, UUID> identifiedQueryResultDao;

    @InjectMocks
    private RabbitMqRunnableQueryServiceImpl rabbitMqRunnableQueryService;

    private final Map<UUID, IdentifiedQueryResult> inMemoryDatabase = new HashMap<>();

    private final Queue<RunnableQueryMessage> inMemoryQueue = new LinkedList<>();

    @Test
    public void publish_publishesRunnableQueryMessageToQueueAndPersistsIdentifiedQueryResultToDatabase() throws IOException, TimeoutException {
        final var expectedDatabaseName = "database1";
        final var expectedSelectStatement = TestUtils.buildSelectStatement();
        final var expectedSql = "select * from table";
        var expectedIdentifiedQueryResult = new IdentifiedQueryResult(UUID.randomUUID());
        doAnswer(invocationOnMock -> {
            inMemoryDatabase.put(expectedIdentifiedQueryResult.getRunnableQueryId(), expectedIdentifiedQueryResult);
            return null;
        }).when(this.identifiedQueryResultDao).save(any());
        var expectedRunnableQueryMessage = new RunnableQueryMessage(
                expectedSql,
                expectedSelectStatement,
                expectedSql
        );
        doAnswer(invocationOnMock -> {
            inMemoryQueue.add(expectedRunnableQueryMessage);
            return expectedRunnableQueryMessage;
        }).when(this.runnableQueryPublisherDao).publish(any());

        var actualRunnableQueryMessage = this.rabbitMqRunnableQueryService.publish(expectedDatabaseName, expectedSelectStatement, expectedSql);

        assertEquals(1, this.inMemoryDatabase.size());
        assertEquals(expectedIdentifiedQueryResult, this.inMemoryDatabase.get(expectedIdentifiedQueryResult.getRunnableQueryId()));
        assertEquals(1, this.inMemoryQueue.size());
        assertTrue(this.inMemoryQueue.contains(expectedRunnableQueryMessage));
        assertEquals(expectedRunnableQueryMessage, actualRunnableQueryMessage);
    }

}