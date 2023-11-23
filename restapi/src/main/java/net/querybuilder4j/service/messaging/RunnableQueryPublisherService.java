package net.querybuilder4j.service.messaging;

import net.querybuilder4j.aspect.LogExecutionTime;
import net.querybuilder4j.model.SelectStatement;
import net.querybuilder4j.model.ro.RunnableQueryMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface RunnableQueryPublisherService {

    @LogExecutionTime
    RunnableQueryMessage publish(String databaseName, SelectStatement selectStatement, String sql) throws IOException, TimeoutException;

}
