package com.deweydatasystem.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.deweydatasystem.config.QbConfig;
import com.deweydatasystem.dao.database.DatabaseQueryRunnerDao;
import com.deweydatasystem.dao.database.DatabaseQueryRunnerDaoImpl;
import com.deweydatasystem.dao.database.IdentifiedQueryResult;
import com.deweydatasystem.dao.query.GenericDao;
import com.deweydatasystem.dao.query.result.RedisQueryResultDaoImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Configuration
public class DaoConfig {

    private final QbConfig qbConfig;

    @Autowired
    public DaoConfig(QbConfig qbConfig) {
        this.qbConfig = qbConfig;
    }

    @Bean
    public DatabaseQueryRunnerDao buildDatabaseQueryRunnerDao() {
        return new DatabaseQueryRunnerDaoImpl(this.qbConfig);
    }

    @Bean
    public GenericDao<IdentifiedQueryResult, UUID> buildIdentifiedQueryResultDao(Jedis jedis) {
        return new RedisQueryResultDaoImpl(jedis);
    }

    @Bean
    public Jedis buildJedisClient() {
        final String host = this.qbConfig.getQueryStatusConfiguration().getHost();
        final int port = this.qbConfig.getQueryStatusConfiguration().getPort();
        final Jedis jedis = new Jedis(host, port);

        final String password = this.qbConfig.getQueryStatusConfiguration().getPassword();
        final String username = this.qbConfig.getQueryStatusConfiguration().getUsername();
        if (password != null) {
            if (username == null) {
                jedis.auth(password);
            } else {
                jedis.auth(username, password);
            }
        }

        return jedis;
    }

    @Bean
    public Connection buildRabbitMqConnection() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(this.qbConfig.getMessagingConfiguration().getHost());
        connectionFactory.setVirtualHost(this.qbConfig.getMessagingConfiguration().getVirtualHost());
        connectionFactory.setUsername(this.qbConfig.getMessagingConfiguration().getUsername());
        connectionFactory.setPassword(this.qbConfig.getMessagingConfiguration().getPassword());
        connectionFactory.setPort(this.qbConfig.getMessagingConfiguration().getPort());

        return connectionFactory.newConnection(qbConfig.getMessagingConfiguration().getConnectionName());
    }

}
