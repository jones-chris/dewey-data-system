package net.querybuilder4j.dao.messaging;

import net.querybuilder4j.model.ro.RunnableQueryMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface RunnableQueryPublisherDao {

    RunnableQueryMessage publish(RunnableQueryMessage message) throws IOException, TimeoutException;

}
