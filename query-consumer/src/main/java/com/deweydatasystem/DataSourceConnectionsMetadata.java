package com.deweydatasystem;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode
@ToString
public class DataSourceConnectionsMetadata {

    @Getter
    private final int maxNumberOfDatabaseConnections;

    @Getter
    private AtomicInteger currentNumberOfDatabaseConnectionsInUse = new AtomicInteger(0);

    public DataSourceConnectionsMetadata(int maxNumberOfDatabaseConnections) {
        if (maxNumberOfDatabaseConnections == 0) {
            throw new IllegalArgumentException("maxNumberOfDatabaseConnections should not be 0");
        }

        this.maxNumberOfDatabaseConnections = maxNumberOfDatabaseConnections;
    }

    /**
     * Returns true if the number of connections currently in use is less than the maximum number of database connections,
     * else false.  This is intended to indicate whether a {@link javax.sql.DataSource} has an available connection.
     *
     * @return {@link boolean}
     */
    public boolean hasAvailableConnection() {
        return this.maxNumberOfDatabaseConnections > this.currentNumberOfDatabaseConnectionsInUse.get();
    }

    public void incrementCurrentNumberOfDatabaseConnectionsInUse() {
        this.currentNumberOfDatabaseConnectionsInUse.getAndIncrement();
    }

    public void decrementCurrentNumberOfDatabaseConnectionsInUse() {
        this.currentNumberOfDatabaseConnectionsInUse.getAndDecrement();
    }

}
