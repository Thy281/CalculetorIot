package com.calculator.Iot.service.impl;

import com.calculator.Iot.service.RateLimitService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * Implementação de Rate Limiting usando Redis com algoritmo Sliding Window Log.
 * Usa Sorted Sets (ZSET) para precise rate limiting.
 *
 * Chave Redis: ratelimit:{identifier}
 * Score: timestamp em milissegundos
 * Member: string do timestamp (opcional, pode ser o mesmo)
 *
 * Vantagens:
 * - Preciso: permite exatamente maxRequests no período deslizante
 * - Distribuído: funciona em múltiplas instâncias
 * - Auto-cleanup: entries expiram automaticamente
 */
@Service
public class RedisRateLimitService implements RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisRateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String identifier, int maxRequests, Duration windowDuration) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        String key = "ratelimit:" + identifier;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowDuration.toMillis();

        // Remover entradas antigas da janela (sliding window)
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Contar requisições atuais na janela
        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= maxRequests) {
            return false;
        }

        // Adicionar timestamp atual
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);

        // Definir TTL para cleanup automático (período + margem)
        Long ttl = redisTemplate.getExpire(key);
        if (ttl == null || ttl == -1) {
            redisTemplate.expire(key, Duration.ofMillis(windowDuration.toMillis() + 60000));
        }

        return true;
    }

    @Override
    public int getRemainingRequests(String identifier, int maxRequests, Duration windowDuration) {
        if (identifier == null || identifier.isEmpty()) {
            return 0;
        }

        String key = "ratelimit:" + identifier;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowDuration.toMillis();

        // Cleanup otimista (não bloqueante)
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        Long count = redisTemplate.opsForZSet().zCard(key);
        int remaining = maxRequests - (count != null ? count.intValue() : 0);
        return Math.max(0, remaining);
    }

    @Override
    public long getTimeToReset(String identifier, Duration windowDuration) {
        if (identifier == null || identifier.isEmpty()) {
            return windowDuration.getSeconds();
        }

        String key = "ratelimit:" + identifier;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowDuration.toMillis();

        // Encontrar o membro mais antigo ainda na janela (primeiro elemento do ZSET)
        Set<String> oldest = redisTemplate.opsForZSet()
                .rangeByScore(key, windowStart, now, 0, 1);

        if (oldest == null || oldest.isEmpty()) {
            return windowDuration.getSeconds();
        }

        try {
            String oldestStr = oldest.iterator().next();
            long oldestTime = Long.parseLong(oldestStr);
            long secondsUntilExpire = (oldestTime + windowDuration.toMillis() - now) / 1000;
            return Math.max(0, secondsUntilExpire);
        } catch (NumberFormatException | NullPointerException e) {
            return windowDuration.getSeconds();
        }
    }

    @Override
    public void cleanup() {
        // Redis auto-expira as chaves, mas pode-se implementar limpeza manual se necessário
        // Por enquanto, nada a fazer pois usamos TTL
    }
}
