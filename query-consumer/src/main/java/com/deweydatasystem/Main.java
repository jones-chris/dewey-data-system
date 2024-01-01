package com.deweydatasystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.*;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import com.deweydatasystem.config.ConfigFileReader;
import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.dao.database.DatabaseQueryRunnerDao;
import com.deweydatasystem.dao.database.DatabaseQueryRunnerDaoImpl;
import com.deweydatasystem.dao.database.IdentifiedQueryResult;
import com.deweydatasystem.dao.query.GenericDao;
import com.deweydatasystem.dao.query.result.RedisQueryResultDaoImpl;
import com.deweydatasystem.exceptions.QueryFailureException;
import com.deweydatasystem.model.ro.RunnableQueryMessage;
import redis.clients.jedis.Jedis;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

    private static final QbConfig qbConfig;

    private static final ConnectionFactory connectionFactory;

    private static final DatabaseQueryRunnerDao databaseQueryRunnerDao;

    private static final Jedis jedis;

    private static final GenericDao<IdentifiedQueryResult, UUID> identifiedQueryResultDao;

    private static final Map<String, DataSourceConnectionsMetadata> dataSourceConnectionsMetadataMap;

    static {
        qbConfig = getQbConfig();
        connectionFactory = getConnectionFactory(qbConfig.getMessagingConfiguration());
        databaseQueryRunnerDao = new DatabaseQueryRunnerDaoImpl(qbConfig);
        jedis = buildJedisClient(qbConfig);
        dataSourceConnectionsMetadataMap = buildDatabaseConnectionMetadataMap();
        identifiedQueryResultDao = getIdentifiedQueryResultDao(jedis);
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        final Connection connection = connectionFactory.newConnection(qbConfig.getMessagingConfiguration().getConnectionName());
        final Channel channel = connection.createChannel();

        final Consumer consumer = buildConsumer(channel);

        // Bind the consumer to all the target data source queues.
        qbConfig.getTargetDataSources()
                .forEach(targetDataSource -> {
                    try {
                        channel.queueDeclare(qbConfig.getMessagingConfiguration().getQueueName(), false, false, false, null); // todo: Is this needed b/c the queue name is passed into the basicConsume call?

                        channel.basicConsume(
                                qbConfig.getMessagingConfiguration().getQueueName(),
                                false, // Turning auto-acknowledge off.
                                consumer
                        );
                    } catch (IOException e) {
                        log.error("", e);
                    }
                });

    }

    private static Consumer buildConsumer(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                RunnableQueryMessage runnableQueryMessage;
                IdentifiedQueryResult identifiedQueryResult;
                try {
                    final String message = new String(body, StandardCharsets.UTF_8);
                    log.info("Received message - consumerTag={}, envelope={}, body={}", consumerTag, envelope.toString(), message);

                    runnableQueryMessage = objectMapper.readValue(message, RunnableQueryMessage.class);

                    identifiedQueryResult = identifiedQueryResultDao.getById(runnableQueryMessage.getUuid());
                }
                catch (Exception e) {
                    log.error("", e);
                    channel.basicReject(envelope.getDeliveryTag(), false); // todo: set up a dead letter queue so the message is not lost.
                    return;
                }

                try {
                    // If there are available connections, then run query.
                    DataSourceConnectionsMetadata dataSourceConnectionsMetadata = getDataSourceConnectionMetadata(runnableQueryMessage.getDataSourceName());
                    if (dataSourceConnectionsMetadata.hasAvailableConnection()) {
                        dataSourceConnectionsMetadata.getCurrentNumberOfDatabaseConnectionsInUse().getAndIncrement();
                    }
                    else {
                        channel.basicReject(envelope.getDeliveryTag(), true); // Requeue the message for later.
                    }

                    // Update query status to RUNNING.
                    identifiedQueryResult.updateStatusStartTime(IdentifiedQueryResult.Status.RUNNING);
                    identifiedQueryResultDao.save(identifiedQueryResult);

                    identifiedQueryResult = databaseQueryRunnerDao.executeQuery(
                            runnableQueryMessage.getDataSourceName(),
                            runnableQueryMessage.getSql(),
                            runnableQueryMessage.getUuid()
                    );

                    // After the query has finished and the connection has been released, decrement the number of connections in use.
                    dataSourceConnectionsMetadata.getCurrentNumberOfDatabaseConnectionsInUse().getAndDecrement();

//                    identifiedQueryResult.setSelectStatement(runnableQueryMessage.getSelectStatement());

                    log.info("Successfully ran identified query result with id, {}", identifiedQueryResult.getRunnableQueryId());

                    // Update the query status to COMPLETE and put query result in cache.
                    identifiedQueryResult.updateStatusStartTime(IdentifiedQueryResult.Status.COMPLETE);
                    identifiedQueryResultDao.save(identifiedQueryResult);

                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
                catch (Exception e) {
                    log.error("", e);
                    log.error("Identified query result failed with id, {}", identifiedQueryResult.getRunnableQueryId());

                    // Update the query status to FAILED.
                    identifiedQueryResult.updateStatusStartTime(IdentifiedQueryResult.Status.FAILED);
                    identifiedQueryResultDao.save(identifiedQueryResult);

                    // Get the error message and write the query result to the cache.
                    if (e instanceof QueryFailureException) {
                        identifiedQueryResult.setMessage(e.getMessage());
                        identifiedQueryResultDao.save(identifiedQueryResult);
                    }

                    channel.basicReject(envelope.getDeliveryTag(), false);
                }
            }
        };
    }

    private static DataSourceConnectionsMetadata getDataSourceConnectionMetadata(String dataSourceName) throws IllegalArgumentException {
        DataSourceConnectionsMetadata dataSourceConnectionsMetadata = dataSourceConnectionsMetadataMap.get(dataSourceName);
        if (dataSourceConnectionsMetadata == null) {
            throw new IllegalArgumentException("Could not find TargetDataSource with name, " + dataSourceName);
        }

        return dataSourceConnectionsMetadata;
    }

    private static ConnectionFactory getConnectionFactory(QbConfig.MessagingConfiguration messagingConfiguration) {
        final ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(messagingConfiguration.getHost());
        connectionFactory.setVirtualHost(messagingConfiguration.getVirtualHost());
        connectionFactory.setUsername(messagingConfiguration.getUsername());
        connectionFactory.setPassword(messagingConfiguration.getPassword());
        connectionFactory.setPort(messagingConfiguration.getPort());

        return connectionFactory;
    }

    private static QbConfig getQbConfig() {
        return new ConfigFileReader().read();
    }

    private static Jedis buildJedisClient(QbConfig qbConfig) {
        final String host = qbConfig.getQueryStatusConfiguration().getHost();
        final int port = qbConfig.getQueryStatusConfiguration().getPort();
        final Jedis jedis = new Jedis(host, port);

        final String password = qbConfig.getQueryStatusConfiguration().getPassword();
        final String username = qbConfig.getQueryStatusConfiguration().getUsername();
        if (password != null) {
            if (username == null) {
                jedis.auth(password);
            } else {
                jedis.auth(username, password);
            }
        }

        return jedis;
    }

    private static GenericDao<IdentifiedQueryResult, UUID> getIdentifiedQueryResultDao(Jedis jedis) {
        return new RedisQueryResultDaoImpl(jedis);
    }

    /**
     * Returns a {@link Map} with the keys being the name of each {@link com.deweydatasystem.config.QbConfig.TargetDataSource}
     * and the values being a {@link DataSourceConnectionsMetadata} which encapsulates the maximum number of available connections
     * and the number of in-use/active connections.
     *
     * @return {@link Map>}
     */
    private static Map<String, DataSourceConnectionsMetadata> buildDatabaseConnectionMetadataMap() {
        return qbConfig.getTargetDataSources().stream()
                .collect(
                        Collectors.toMap(
                                QbConfig.TargetDataSource::getName,
                                targetDataSource -> {
                                    DataSource dataSource = qbConfig.getTargetDataSourceAsDataSource(targetDataSource.getName());

                                    if (dataSource instanceof HikariDataSource) {
                                        int maxNumberOfAvailableConnections = ((HikariDataSource) dataSource).getHikariConfigMXBean().getMaximumPoolSize();
                                        return new DataSourceConnectionsMetadata(maxNumberOfAvailableConnections);
                                    }

                                    throw new IllegalStateException("DataSource must be a HikariDataSource");
                                }
                        )
                );
    }

}
