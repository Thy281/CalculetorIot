package com.calculator.Iot.service.impl;

import com.calculator.Iot.service.RateLimitService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Implementação de Rate Limiting em memória usando Caffeine + Sliding Window Log.
 * Usa um NavigableMap por identificador para armazenar timestamps das requisições.
 *
 * NOTA: Esta implementação é por processo (não compartilhada entre instâncias).
 * Use Redis para ambientes multi-instância.
 */
@Service
public class CaffeineRateLimitService implements RateLimitService {

    /**
     * Estrutura que armazena timestamps ordenados para cada identificador.
     * NavigableMap permitequick range queries (sliding window).
     */
    private final ConcurrentMap<String, NavigableMap<Long, Boolean>> requestsMap = new ConcurrentHashMap<>();

    /**
     * Cache para expiração automática das entradas (limpa mapa após período).
     * A chave é o identificador, o valor é o timestamp da última requisição.
     */
    private final Cache<String, Long> expirationCache;

    public CaffeineRateLimitService() {
        this.expirationCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(30)) // margem maior que o período máximo esperado
                .maximumSize(10_000)
                .build();
    }

    @Override
    public boolean isAllowed(String identifier, int maxRequests, Duration windowDuration) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }

        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowDuration.toMillis();

        // Obter ou criar o mapa para este identificador
        NavigableMap<Long, Boolean> timestamps = requestsMap.computeIfAbsent(identifier,
                k -> new TreeMap<>());

        synchronized (timestamps) {
            // Remover entradas antigas fora da janela deslizante
            timestamps.headMap(windowStart, false).clear();

            // Verificar limite
            int currentCount = timestamps.size();
            if (currentCount >= maxRequests) {
                return false;
            }

            // Adicionar timestamp atual
            timestamps.put(now, Boolean.TRUE);
        }

        // Atualizar TTL de expiração
        expirationCache.put(identifier, now);
        return true;
    }

    @Override
    public int getRemainingRequests(String identifier, int maxRequests, Duration windowDuration) {
        if (identifier == null || identifier.isEmpty()) {
            return 0;
        }

        NavigableMap<Long, Boolean> timestamps = requestsMap.get(identifier);
        if (timestamps == null) {
            return maxRequests;
        }

        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowDuration.toMillis();

        synchronized (timestamps) {
            timestamps.headMap(windowStart, false).clear();
            int count = timestamps.size();
            int remaining = maxRequests - count;
            return Math.max(0, remaining);
        }
    }

    @Override
    public long getTimeToReset(String identifier, Duration windowDuration) {
        if (identifier == null || identifier.isEmpty()) {
            return windowDuration.getSeconds();
        }

        NavigableMap<Long, Boolean> timestamps = requestsMap.get(identifier);
        if (timestamps == null || timestamps.isEmpty()) {
            return windowDuration.getSeconds();
        }

        synchronized (timestamps) {
            // Obter primeiro timestamp (mais antigo)
            Long oldest = timestamps.firstKey();
            if (oldest == null) {
                return windowDuration.getSeconds();
            }

            long now = Instant.now().toEpochMilli();
            long timeDiff = oldest + windowDuration.toMillis() - now;
            return Math.max(0, timeDiff / 1000);
        }
    }

    @Override
    public void cleanup() {
        // Limpar entradas expiradas
        expirationCache.asMap().keySet().forEach(requestsMap::remove);
    }
}
