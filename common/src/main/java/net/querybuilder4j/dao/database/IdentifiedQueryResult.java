package net.querybuilder4j.dao.database;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;

import static java.time.OffsetDateTime.now;

@EqualsAndHashCode(callSuper = true)
@ToString
@Setter
@Getter
@Slf4j
public class IdentifiedQueryResult extends QueryResult implements Serializable {

    private static final long serialVersionUID = 3L;

    /**
     * The id of the query.
     */
    private UUID runnableQueryId;

    /**
     * A {@link List} of the other query {@link UUID}s that are run in parallel with this query and whose results are
     * eventually combined/unions with the results of this query.
     */
    private final List<UUID> dependentRunnableQueryIds = new ArrayList<>();

    /**
     * A {@link EnumMap} with the keys being the {@link Status} and the value being a {@link MutablePair}
     * containing the times the query started and ended in that status/state as {@link OffsetDateTime}s.
     */
    private final Map<Status, OffsetDateTime> statusStartTimes = new EnumMap<>(Status.class);

    public IdentifiedQueryResult(UUID runnableQueryId) {
        this.runnableQueryId = runnableQueryId;
    }

    public IdentifiedQueryResult(String message) {
        super(message);
    }

    /**
     * This constructor acts as a bridge between a {@link QueryResultDto} and the @{@link QueryResult#QueryResult(ResultSet, String)}.
     *
     * @param queryResultDto {@link QueryResultDto}
     */
    public IdentifiedQueryResult(QueryResultDto queryResultDto) {
        this(queryResultDto.getResultSet(), queryResultDto.getSql(), queryResultDto.getRunnableQueryId());
    }

    public IdentifiedQueryResult(ResultSet resultSet, String sql, UUID runnableQueryId) {
        super(resultSet, sql);
        this.runnableQueryId = runnableQueryId;
    }

    public void updateStatusStartTime(Status status) {
        this.statusStartTimes.put(status, now());
    }

    @Getter
    public enum Status {

        //        NOT_BUILT("Not Built"),
        BUILT("Built"),
        QUEUED("Queued"),
        RUNNING("Running"),
        COMPLETE("Complete"),
        FAILED("Failed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

    }

}
