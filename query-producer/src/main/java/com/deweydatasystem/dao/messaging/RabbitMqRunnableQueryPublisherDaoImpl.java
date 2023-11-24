package com.deweydatasystem.dao.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.model.ro.RunnableQueryMessage;
import com.deweydatasystem.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Repository
@Slf4j
public class RabbitMqRunnableQueryPublisherDaoImpl implements RunnableQueryPublisherDao {

    private final QbConfig qbConfig;

    private final Connection connection;

    @Autowired
    public RabbitMqRunnableQueryPublisherDaoImpl(QbConfig qbConfig, Connection rabbitMqConnection) throws IOException, TimeoutException {
        this.qbConfig = qbConfig;
        this.connection = rabbitMqConnection;
    }

    @Override
    public RunnableQueryMessage publish(RunnableQueryMessage message) throws IOException, TimeoutException {
        final String messageJsonString = Utils.serializeToJson(message);

        log.info("Preparing to send message, {}, to queue, {}", message, this.qbConfig.getMessagingConfiguration().getQueueName());

        try (final Channel channel = this.connection.createChannel()) {
            channel.queueDeclare(this.qbConfig.getMessagingConfiguration().getQueueName(), false, false, false, null);

            channel.basicPublish(
                    "",
                    this.qbConfig.getMessagingConfiguration().getQueueName(),
                    null,
                    messageJsonString.getBytes()
            );
        }
        catch (IOException | TimeoutException e) {
            log.error("", e);
            log.error("Error attempting to publish message, {}, to queue, {}", message, this.qbConfig.getMessagingConfiguration().getQueueName());

            throw e;
        }

        return message;
    }

}
