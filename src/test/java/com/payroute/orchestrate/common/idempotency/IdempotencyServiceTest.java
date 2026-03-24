package com.payroute.orchestrate.common.idempotency;

import com.payroute.orchestrate.config.IdempotencyProperties;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import com.payroute.orchestrate.domain.enums.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for IdempotencyService with mocked Redis.
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTest {

    @Mock
    private RedisTemplate<String, PaymentResponse> paymentResponseRedisTemplate;

    @Mock
    private RedisTemplate<String, String> stringRedisTemplate;

    @Mock
    private ValueOperations<String, PaymentResponse> valueOps;

    @Mock
    private ValueOperations<String, String> stringValueOps;

    @Mock
    private IdempotencyProperties idempotencyProperties;

    private IdempotencyService idempotencyService;

    private PaymentResponse sampleResponse() {
        return new PaymentResponse(
                UUID.randomUUID(),
                TransactionStatus.SUCCESS,
                "STRIPE",
                UUID.randomUUID().toString(),
                new BigDecimal("100.00"),
                "USD",
                LocalDateTime.now(),
                null
        );
    }

    @BeforeEach
    void setUp() {
        idempotencyService = new IdempotencyService(
                paymentResponseRedisTemplate,
                stringRedisTemplate,
                idempotencyProperties
        );
    }

    @Test
    void getIfPresent_returnsEmpty_whenKeyNotInRedis() {
        when(paymentResponseRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null);

        Optional<PaymentResponse> result = idempotencyService.getIfPresent("key-123");

        assertTrue(result.isEmpty());
    }

    @Test
    void getIfPresent_returnsResponse_whenKeyExistsInRedis() {
        PaymentResponse cached = sampleResponse();
        when(paymentResponseRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("idempotency:key-123")).thenReturn(cached);

        Optional<PaymentResponse> result = idempotencyService.getIfPresent("key-123");

        assertTrue(result.isPresent());
        assertEquals(cached, result.get());
    }

    @Test
    void getIfPresent_returnsEmpty_whenRedisThrows() {
        when(paymentResponseRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenThrow(new RuntimeException("Redis down"));

        Optional<PaymentResponse> result = idempotencyService.getIfPresent("key-123");

        assertTrue(result.isEmpty());
    }

    @Test
    void store_callsRedisWithCorrectTtl() {
        PaymentResponse response = sampleResponse();
        when(paymentResponseRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(idempotencyProperties.getTtlHours()).thenReturn(24L);

        idempotencyService.store("key-123", response);

        verify(valueOps).set(
                eq("idempotency:key-123"),
                eq(response),
                eq(24L),
                eq(TimeUnit.HOURS)
        );
    }

    @Test
    void store_doesNotThrow_whenRedisThrows() {
        when(paymentResponseRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(idempotencyProperties.getTtlHours()).thenReturn(24L);
        doThrow(new RuntimeException("Redis down"))
                .when(valueOps).set(anyString(), any(), anyLong(), any());

        assertDoesNotThrow(() -> idempotencyService.store("key-123", sampleResponse()));
    }

    @Test
    void acquireLock_returnsTrue_whenLockAcquired() {
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOps);
        when(stringValueOps.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(true);

        boolean result = idempotencyService.acquireLock("key-123", 5000);

        assertTrue(result);
    }

    @Test
    void acquireLock_returnsFalse_whenLockAlreadyHeld() {
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOps);
        when(stringValueOps.setIfAbsent(anyString(), anyString(), anyLong(), any()))
                .thenReturn(false);

        boolean result = idempotencyService.acquireLock("key-123", 5000);

        assertFalse(result);
    }

    @Test
    void acquireLock_defaultTimeout_delegatesToOverloadedMethod() {
        when(stringRedisTemplate.opsForValue()).thenReturn(stringValueOps);
        when(stringValueOps.setIfAbsent(eq("lock:key-123"), eq("LOCKED"),
                eq(5000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);

        boolean result = idempotencyService.acquireLock("key-123");

        assertTrue(result);
        verify(stringValueOps).setIfAbsent("lock:key-123", "LOCKED",
                5000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void releaseLock_deletesLockKey() {
        when(stringRedisTemplate.delete("lock:key-123")).thenReturn(true);

        assertDoesNotThrow(() -> idempotencyService.releaseLock("key-123"));

        verify(stringRedisTemplate).delete("lock:key-123");
    }
}
