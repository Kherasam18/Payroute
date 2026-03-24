package com.payroute.orchestrate.common.idempotency;

import com.payroute.orchestrate.config.IdempotencyProperties;
import com.payroute.orchestrate.domain.dto.PaymentResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis-backed idempotency guard.
 *
 * <p>Implements a fail-open design: Redis errors are caught and logged
 * as warnings but never propagated — payments should still process even
 * if Redis is temporarily unavailable.</p>
 *
 * <p>Only loaded when a {@link RedisConnectionFactory} is available.</p>
 */
@Slf4j
@Service
@ConditionalOnBean(RedisConnectionFactory.class)
public class IdempotencyService {

    private static final long DEFAULT_LOCK_TIMEOUT_MS = 5000;

    private final RedisTemplate<String, PaymentResponse> paymentResponseRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final IdempotencyProperties idempotencyProperties;

    public IdempotencyService(
            @Qualifier("paymentResponseRedisTemplate")
            RedisTemplate<String, PaymentResponse> paymentResponseRedisTemplate,
            @Qualifier("lockRedisTemplate")
            RedisTemplate<String, String> stringRedisTemplate,
            IdempotencyProperties idempotencyProperties) {
        this.paymentResponseRedisTemplate = paymentResponseRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.idempotencyProperties = idempotencyProperties;
    }

    private String buildKey(String idempotencyKey) {
        return "idempotency:" + idempotencyKey;
    }

    /**
     * Checks if a cached response exists for the given idempotency key.
     * Returns Optional.empty() on cache miss OR if Redis is unavailable (fail-open).
     */
    public Optional<PaymentResponse> getIfPresent(String idempotencyKey) {
        try {
            String key = buildKey(idempotencyKey);
            PaymentResponse cached = paymentResponseRedisTemplate.opsForValue().get(key);
            if (cached != null) {
                log.info("Idempotency hit for key: {}, returning cached response", key);
                return Optional.of(cached);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Redis unavailable for idempotency check: {}, proceeding without cache",
                    e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Stores a payment response in Redis with the configured TTL.
     * Fails silently if Redis is unavailable (fail-open).
     */
    public void store(String idempotencyKey, PaymentResponse response) {
        try {
            String key = buildKey(idempotencyKey);
            paymentResponseRedisTemplate.opsForValue()
                    .set(key, response, idempotencyProperties.getTtlHours(), TimeUnit.HOURS);
            log.info("Stored idempotency result for key: {}, TTL: {}h",
                    key, idempotencyProperties.getTtlHours());
        } catch (Exception e) {
            log.warn("Failed to store idempotency result for key: {}: {}",
                    buildKey(idempotencyKey), e.getMessage());
        }
    }

    /**
     * Acquires a distributed lock to prevent race conditions on concurrent
     * identical requests. Uses the default timeout of 5000ms.
     */
    public boolean acquireLock(String idempotencyKey) {
        return acquireLock(idempotencyKey, DEFAULT_LOCK_TIMEOUT_MS);
    }

    /**
     * Acquires a distributed lock with a specific timeout.
     *
     * @return true if lock was acquired, false if another thread holds it
     */
    public boolean acquireLock(String idempotencyKey, long timeoutMs) {
        try {
            String lockKey = "lock:" + idempotencyKey;
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "LOCKED", timeoutMs, TimeUnit.MILLISECONDS);
            boolean result = Boolean.TRUE.equals(acquired);
            if (result) {
                log.debug("Lock acquired for key: {}", lockKey);
            } else {
                log.debug("Lock contention for key: {}", lockKey);
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to acquire lock for key: {}: {}", idempotencyKey, e.getMessage());
            return true; // fail-open: allow processing if Redis lock fails
        }
    }

    /**
     * Releases the distributed lock for the given idempotency key.
     */
    public void releaseLock(String idempotencyKey) {
        try {
            String lockKey = "lock:" + idempotencyKey;
            stringRedisTemplate.delete(lockKey);
            log.debug("Lock released for key: {}", lockKey);
        } catch (Exception e) {
            log.warn("Failed to release lock for key: {}: {}", idempotencyKey, e.getMessage());
        }
    }
}
