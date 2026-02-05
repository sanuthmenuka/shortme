package com.sanuth.shortme;

import com.sanuth.shortme.service.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private RedisService redisService;

    private void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        redisService = new RedisService(redisTemplate);
    }

    // ---------------------------------------------------------------
    // Key construction
    // ---------------------------------------------------------------

    @Test
    void incrementClick_callsIncrementOnTotalKey() {
        setup();
        redisService.incrementClick("abc");

        verify(valueOps).increment("clicks:code:abc:total");
    }

    @Test
    void incrementClick_callsIncrementOnDailyKey() {
        setup();
        redisService.incrementClick("abc");

        String expectedDailyKey = "clicks:code:abc:day:" + LocalDate.now();
        verify(valueOps).increment(expectedDailyKey);
    }

    // ---------------------------------------------------------------
    // TTL
    // ---------------------------------------------------------------

    @Test
    void incrementClick_setsTTLOnDailyKeyOnly() {
        setup();
        redisService.incrementClick("abc");

        String totalKey = "clicks:code:abc:total";
        String dailyKey = "clicks:code:abc:day:" + LocalDate.now();

        verify(redisTemplate).expire(dailyKey, Duration.ofDays(30));
        verify(redisTemplate, never()).expire(totalKey, any(Duration.class));
    }

    // ---------------------------------------------------------------
    // Call ordering: increment both keys before setting TTL
    // ---------------------------------------------------------------

    @Test
    void incrementClick_incrementsBeforeSettingExpiry() {
        setup();
        String dailyKey = "clicks:code:abc:day:" + LocalDate.now();

        redisService.incrementClick("abc");

        InOrder inOrder = inOrder(valueOps, redisTemplate);
        inOrder.verify(valueOps).increment("clicks:code:abc:total");
        inOrder.verify(valueOps).increment(dailyKey);
        inOrder.verify(redisTemplate).expire(dailyKey, Duration.ofDays(30));
    }

    // ---------------------------------------------------------------
    // Redis failure is swallowed
    // ---------------------------------------------------------------

    @Test
    void incrementClick_redisThrows_doesNotPropagate() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        doThrow(new RuntimeException("connection refused")).when(valueOps).increment(any());
        redisService = new RedisService(redisTemplate);

        assertDoesNotThrow(() -> redisService.incrementClick("abc"));
    }
}
