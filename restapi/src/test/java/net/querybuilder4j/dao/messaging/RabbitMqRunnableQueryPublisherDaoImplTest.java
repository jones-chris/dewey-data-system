package net.querybuilder4j.dao.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import net.querybuilder4j.TestUtils;
import net.querybuilder4j.config.QbConfig;
import net.querybuilder4j.model.ro.RunnableQueryMessage;
import net.querybuilder4j.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqRunnableQueryPublisherDaoImplTest {

    private final QbConfig qbConfig = this.buildQbConfig();

    private final Connection connection = mock(Connection.class);

    private final RabbitMqRunnableQueryPublisherDaoImpl rabbitMqRunnableQueryPublisherDao = this.buildRabbitMqRunnableQueryPublisherDaoImpl(this.qbConfig, this.connection);

    public RabbitMqRunnableQueryPublisherDaoImplTest() throws IOException, TimeoutException {
    }

    @Test
    public void publish_publishesMessageToQueueSuccessfully() throws IOException, TimeoutException {
        Queue<byte[]> inMemoryQueue = new LinkedBlockingQueue<>();
        final var selectStatement = TestUtils.buildSelectStatement();
        final var runnableQueryMessage = new RunnableQueryMessage("database1", selectStatement, "SELECT * FROM table");
        final var runnableQueryMessageBytes = Utils.serializeToJson(runnableQueryMessage).getBytes();
        final var mockChannel = mock(Channel.class);
        doAnswer(invocationOnMock -> {
            inMemoryQueue.add(runnableQueryMessageBytes);
            return null;
        }).when(mockChannel).basicPublish(any(), any(), any(), any());
        doReturn(mockChannel)
                .when(this.connection).createChannel();

        this.rabbitMqRunnableQueryPublisherDao.publish(runnableQueryMessage);

        assertEquals(1, inMemoryQueue.size());
    }

    @Test
    public void publish_exceptionIsRethrownWhenPublishIOExceptionOccurs() {
        assertThrows(
                IOException.class,
                () -> {
                    final var selectStatement = TestUtils.buildSelectStatement();
                    final var runnableQueryMessage = new RunnableQueryMessage("database1", selectStatement, "SELECT * FROM table");
                    final var mockChannel = mock(Channel.class);
                    doReturn(mockChannel)
                            .when(this.connection).createChannel();
                    doThrow(IOException.class)
                            .when(mockChannel).basicPublish(any(), any(), any(), any());

                    this.rabbitMqRunnableQueryPublisherDao.publish(runnableQueryMessage);
                }
        );
    }

    private QbConfig buildQbConfig() {
        var messagingConfig = new QbConfig.MessagingConfiguration();
        messagingConfig.setHost("myHost");
        messagingConfig.setVirtualHost("myVirtualHost");
        messagingConfig.setUsername("username");
        messagingConfig.setPassword("password");
        messagingConfig.setPort(100);
        messagingConfig.setQueueName("my-queue");

        QbConfig qbConfig = new QbConfig();
        qbConfig.setMessagingConfiguration(messagingConfig);

        return qbConfig;
    }

    private RabbitMqRunnableQueryPublisherDaoImpl buildRabbitMqRunnableQueryPublisherDaoImpl(
            QbConfig qbConfig,
            Connection connection
    ) throws IOException, TimeoutException {
        return new RabbitMqRunnableQueryPublisherDaoImpl(qbConfig, connection);
    }

}