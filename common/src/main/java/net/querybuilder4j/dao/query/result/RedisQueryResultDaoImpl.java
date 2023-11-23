package net.querybuilder4j.dao.query.result;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import net.querybuilder4j.dao.database.IdentifiedQueryResult;
import net.querybuilder4j.dao.query.GenericDao;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.utils.Utils;
import org.apache.commons.lang3.SerializationUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Objects;
import java.util.UUID;

@Slf4j
public class RedisQueryResultDaoImpl implements GenericDao<IdentifiedQueryResult, UUID> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(new JavaTimeModule());

    private static final int ONE_HOUR_IN_SECONDS = 3600;

    private static final String KEY_PREFIX = "query/result/%s";

    private final Jedis jedis;

    public RedisQueryResultDaoImpl(Jedis jedis) {
        this.jedis = jedis;
    }

    @Override
    public IdentifiedQueryResult getById(UUID id) {
        String key = buildKey(id);

        byte[] bytes = this.jedis.get(key.getBytes());

        if (bytes == null) {
            throw new CacheMissException("Could not find key, " + key);
        }

        return SerializationUtils.deserialize(bytes);
    }

    @Override
    public void save(IdentifiedQueryResult identifiedQueryResult) {
        try {
            byte[] redisRSValue = Utils.toByteArray(identifiedQueryResult);
            this.jedis.setex(
                    buildKey(identifiedQueryResult.getRunnableQueryId()).getBytes(),
                    ONE_HOUR_IN_SECONDS,
                    redisRSValue
            );
        }
        catch (IOException e) {
            log.error("", e);
            throw new JedisException(e);
        }

    }

    private String buildKey(UUID runnableQueryUuid) {
        Objects.requireNonNull(runnableQueryUuid, "runnableQueryId should not be null");
        return String.format(KEY_PREFIX, runnableQueryUuid.toString());
    }

}
