package com.deweydatasystem.service.messaging;

import com.deweydatasystem.aspect.LogExecutionTime;
import com.deweydatasystem.model.SelectStatement;
import com.deweydatasystem.model.ro.RunnableQueryMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface RunnableQueryPublisherService {

    @LogExecutionTime
    RunnableQueryMessage publish(String databaseName, SelectStatement selectStatement, String sql) throws IOException, TimeoutException;

}
