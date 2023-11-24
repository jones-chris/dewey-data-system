package com.deweydatasystem.dao.messaging;

import com.deweydatasystem.model.ro.RunnableQueryMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface RunnableQueryPublisherDao {

    RunnableQueryMessage publish(RunnableQueryMessage message) throws IOException, TimeoutException;

}
