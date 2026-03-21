package com.calculator.Iot.filter;

import com.calculator.Iot.config.RateLimitProperties;
import com.calculator.Iot.service.RateLimitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

/**
 * Filtro de Rate Limiting para proteger a API contra abusos.
 *
 * Funcionamento:
 * 1. Extrai identificador da requisição (IP, User ID ou API Key)
 * 2. Verifica se endpoint está excluído
 * 3. Aplica limite configurado
 * 4. Adiciona headers de rate limit na resposta
 * 5. Retorna 429 se limite excedido
 *
 * Configurável via properties: ratelimit.*
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final RateLimitProperties rateLimitProperties;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();

        // Verificar se rate limiting está habilitado globalmente
        if (!rateLimitProperties.isEnabled()) {
            return true;
        }

        // Verificar se endpoint está excluído da regra
        for (String excluded : rateLimitProperties.getExcludedPaths()) {
            if (pathMatcher.match(excluded, path)) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String identifier = extractIdentifier(request);
        Duration windowDuration = Duration.ofMinutes(rateLimitProperties.getPeriodMinutes());
        int maxRequests = getMaxRequestsForEndpoint(request);

        // Verificar se permitido
        boolean allowed = rateLimitService.isAllowed(identifier, maxRequests, windowDuration);

        if (!allowed) {
            long resetTime = rateLimitService.getTimeToReset(identifier, windowDuration);
            int remaining = 0;

            // Adicionar headers de rate limit
            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
            response.setHeader("Retry-After", String.valueOf(resetTime));

            log.warn("Rate limit exceeded for identifier: {}, path: {}, reset in: {}s",
                    identifier, request.getServletPath(), resetTime);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"retry_after\":" + resetTime + "}");
            return;
        }

        // Adicionar headers informativos nas respostas OK
        int remaining = rateLimitService.getRemainingRequests(identifier, maxRequests, windowDuration);
        long resetTime = rateLimitService.getTimeToReset(identifier, windowDuration);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o identificador único do request baseado na configuração.
     * Prioridade: Header de API Key > Usuário autenticado > IP
     */
    private String extractIdentifier(HttpServletRequest request) {
        String identifierType = rateLimitProperties.getIdentifierType();

        switch (identifierType) {
            case "api-key":
                // Buscar API key em header (ex: X-API-Key)
                String apiKey = request.getHeader("X-API-Key");
                if (apiKey != null && !apiKey.isEmpty()) {
                    return "api:" + apiKey.hashCode();
                }
                // Fallback para IP se não houver API key
                return getClientIp(request);

            case "user":
                // Se houver autenticação Spring Security, pegar do principal
                // (assumindo que usuário autenticado está disponível)
                if (request.getUserPrincipal() != null) {
                    return "user:" + request.getUserPrincipal().getName();
                }
                return getClientIp(request);

            case "ip":
            default:
                return "ip:" + getClientIp(request);
        }
    }

    /**
     * Obtém o IP real do cliente considerando proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For pode conter múltiplos IPs, pegar o primeiro (cliente real)
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Obtém o limite de requisições específico para o endpoint, se configurado.
     */
    private int getMaxRequestsForEndpoint(HttpServletRequest request) {
        String path = request.getServletPath();
        RateLimitProperties.EndpointConfig endpointConfig = rateLimitProperties.getEndpoints().get(path);

        if (endpointConfig != null && endpointConfig.getMaxRequests() > 0) {
            return endpointConfig.getMaxRequests();
        }

        // Também suporta pattern matching (wildcards)
        for (String pattern : rateLimitProperties.getEndpoints().keySet()) {
            if (pathMatcher.match(pattern, path)) {
                RateLimitProperties.EndpointConfig config = rateLimitProperties.getEndpoints().get(pattern);
                if (config != null && config.getMaxRequests() > 0) {
                    return config.getMaxRequests();
                }
            }
        }

        return rateLimitProperties.getMaxRequests();
    }
}
