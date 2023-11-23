package net.querybuilder4j.dao.query.result;

import net.querybuilder4j.dao.database.IdentifiedQueryResult;
import net.querybuilder4j.exceptions.CacheMissException;
import net.querybuilder4j.utils.Utils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisQueryResultDaoImplTest {

    @Mock
    private Jedis jedis;

    @InjectMocks
    private RedisQueryResultDaoImpl redisQueryResultDao;

    private final Map<String, byte[]> inMemoryDatabase = new HashMap<>();

    /**
     * Empty/clear the in-memory database/map between each unit test.
     */
    @Before
    public void beforeEach() {
        this.inMemoryDatabase.clear();
    }

    @Test
    public void getById_deserializesBytesToIdentifiedQueryResult() {
        final var expectedIdentifiedQueryResult = new IdentifiedQueryResult(UUID.randomUUID());
        when(this.jedis.get(any(byte[].class)))
                .thenReturn(SerializationUtils.serialize(expectedIdentifiedQueryResult));

        var actualIdentifiedQueryResult = this.redisQueryResultDao.getById(UUID.randomUUID());

        assertEquals(expectedIdentifiedQueryResult, actualIdentifiedQueryResult);
    }

    @Test(expected = CacheMissException.class)
    public void getById_nullResultThrowsCacheMissException() {
        final var expectedIdentifiedQueryResult = new IdentifiedQueryResult(UUID.randomUUID());
        when(this.jedis.get(any(byte[].class)))
                .thenReturn(null);

        this.redisQueryResultDao.getById(UUID.randomUUID());
    }

    @Test
    public void save_setsValueCorrectly() throws IOException {
        final String expectedKey = "key";
        final IdentifiedQueryResult expectedQueryResult = new IdentifiedQueryResult(UUID.randomUUID());
        final byte[] expectedQueryResultByteArray = Utils.toByteArray(expectedQueryResult);
        when(this.jedis.setex(any(byte[].class), anyInt(), any(byte[].class)))
                .thenAnswer(
                        invocationOnMock -> this.inMemoryDatabase.put(expectedKey, expectedQueryResultByteArray)
                );

        this.redisQueryResultDao.save(expectedQueryResult);

        assertEquals(1, this.inMemoryDatabase.size());
        assertEquals(expectedQueryResultByteArray, this.inMemoryDatabase.get(expectedKey));
    }

    @Test(expected = JedisException.class)
    public void save_IOExceptionIsWrappedInJedisExceptionAndRethrown() throws IOException {
        when(this.jedis.setex(any(byte[].class), anyInt(), any(byte[].class)))
                .thenAnswer(
                        invocationOnMock -> {
                            throw new IOException();
                        }
                );

        this.redisQueryResultDao.save(
                new IdentifiedQueryResult(UUID.randomUUID())
        );
    }

}