# 🛡️ Rate Limiting - CalculatorIot

Sistema de rate limiting implementado para proteger a API contra abusos, ataques de força bruta e garantir disponibilidade dos recursos.

---

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Como Funciona](#como-funciona)
3. [Configuração](#configuração)
4. [Identificadores](#identificadores)
5. [Algoritmo](#algoritmo)
6. [Headers de Resposta](#headers-de-resposta)
7. [Personalização por Endpoint](#personalização-por-endpoint)
8. [Monitoramento](#monitoramento)
9. [Troubleshooting](#troubleshooting)

---

## 🎯 Visão Geral

**O que faz:** Limita o número de requisições que um cliente pode fazer em um período de tempo.

**Por que é importante:**
- ✅ Previne ataques de força bruta (logins, APIs)
- ✅ Protege recursos contra uso excessivo
- ✅ Melhora a estabilidade e disponibilidade
- ✅ Garante uso justo para todos os usuários

**Tecnologia utilizada:**
- **Redis** (padrão): Para ambientes multi-instância, clustered, ou com alta demanda
- **Caffeine** (fallback): Para desenvolvimento ou single-instance

**Algoritmo:** Sliding Window Log (janela deslizante com logs de timestamps)

---

## ⚙️ Como Funciona

### Fluxo da requisição:

```
┌─────────────┐
│   Request   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────────┐
│ 1. Extrair identificador (IP/User/API Key) │
└─────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────┐
│ 2. Verificar se endpoint está excluído     │
└─────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────┐
│ 3. Buscar contador no Redis ou Cache       │
│    (Remover entradas expiradas)            │
└─────────────────────────────────────────────┘
       │
       ▼
┌─────────────────────────────────────────────┐
│ 4. Verificar se limite foi atingido        │
└─────────────────────────────────────────────┘
       │
   ┌───┴───┐
   │       │
   ▼       ▼
Permitido  Rejeitado (429)
   │       │
   ▼       ▼
Processar  Retornar erro
request    + headers
   │
   ▼
┌─────────────────────────────────────────────┐
│ 5. Adicionar timestamp atual               │
│ 6. Adicionar headers de rate limit        │
└─────────────────────────────────────────────┘
       │
       ▼
   Response
```

---

## 🔧 Configuração

### Propriedades da aplicação (`application.properties`)

```properties
# Habilitar/desabilitar rate limiting
ratelimit.enabled=true

# Limite padrão: 100 requisições a cada 15 minutos
ratelimit.max-requests=100
ratelimit.period-minutes=15

# Tipo de identificador: ip, user ou api-key
ratelimit.identifier-type=ip

# Endpoints excluídos (não sofrem rate limiting)
ratelimit.excluded-paths=/actuator/**,/error

# Configuração Redis (opcional, usa localhost:6379 por padrão)
spring.redis.host=localhost
spring.redis.port=6379
# spring.redis.password=sua_senha
```

### Configuração avançada por endpoint

```properties
# Limite customizado para endpoints específicos
ratelimit.endpoints[0].path=/api/calculate
ratelimit.endpoints[0].max-requests=200
ratelimit.endpoints[0].period-minutes=5

ratelimit.endpoints[1].path=/api/auth/login
ratelimit.endpoints[1].max-requests=5
ratelimit.endpoints[1].period-minutes=1
```

---

## 🆔 Identificadores

O rate limit pode ser aplicado por diferentes critérios:

### 1. Por IP (`identifier-type=ip`) - **Padrão**
```properties
ratelimit.identifier-type=ip
```
- Cada endereço IP tem seu próprio limite
- Funciona sem autenticação
- Considera X-Forwarded-For se atrás de proxy/reverse proxy

### 2. Por Usuário autenticado (`identifier-type=user`)
```properties
ratelimit.identifier-type=user
```
- Usa `request.getUserPrincipal().getName()` (Spring Security)
- Requer que o usuário esteja autenticado
- Se não autenticado, cai para IP

### 3. Por API Key (`identifier-type=api-key`)
```properties
ratelimit.identifier-type=api-key
```
- Lê header `X-API-Key`
- Hash da API key como identificador
- Útil para integrações externas

---

## 📊 Algoritmo: Sliding Window Log

### Como funciona:

```
                   ┌─────────────────► Tempo
                   │
       ┌────────────┬─────┬──────┬──────┬─────┐
       │            │     │      │      │     │
       │  r1   r2   │ r3  │ r4   │ r5   │ r6  │  requisições
       │             │     │      │      │     │
Window: ────────────────────────────────────────────
       └────────────┬─────┬──────┬──────┬─────┘
                   │     │      │      │
                   t0    t1     t2     t3 (agora)

Janela deslizante: [t3 - período, t3]
```

Para cada requisição:
1. Remover todos os timestamps fora da janela deslizante
2. Contar quantos timestamps restam
3. Se COUNT >= maxRequests → BLOQUEAR
4. Caso contrário → PERMITIR e adicionar timestamp atual

### Vantagens:
- ✅ **Preciso**: Permite exatamente N requisições em qualquer janela de tempo
- ✅ **Distribuído**: Funciona em múltiplas instâncias com Redis
- ✅ **Suave**: Não há "burst" no início de cada janela

---

## 📈 Headers de Resposta

### Requisição permitida (200 OK)

```http
X-RateLimit-Limit: 100          # Limite total no período
X-RateLimit-Remaining: 87       # Quantas ainda restam
X-RateLimit-Reset: 312          # Segundos até reset completo
```

### Requisição bloqueada (429 Too Many Requests)

```http
HTTP/1.1 429 Too Many Requests
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 298
Retry-After: 298                # Sugestão de tempo para nova tentativa

{
  "error": "Too Many Requests",
  "retry_after": 298
}
```

---

## 🎛️ Personalização por Endpoint

Você pode configurar limites diferentes para endpoints específicos:

```properties
# API de cálculo (limite alto)
ratelimit.endpoints[0].path=/api/calculate
ratelimit.endpoints[0].max-requests=500
ratelimit.endpoints[0].period-minutes=15

# Login (limite baixo para evitar força bruta)
ratelimit.endpoints[1].path=/api/auth/login
ratelimit.endpoints[1].max-requests=5
ratelimit.endpoints[1].period-minutes=1

# Endpoint padrão para todos os outros
ratelimit.max-requests=100
ratelimit.period-minutes=15
```

Suporta **patternsAntPath** (wildcards):
```properties
ratelimit.endpoints[0].path=/api/v1/**
ratelimit.endpoints[0].max-requests=200
ratelimit.endpoints[0].period-minutes=10
```

---

## 📊 Monitoramento

### Ver logs de rate limit no backend:

```bash
docker-compose logs -f backend | grep "Rate limit"
```

Exemplo de log:
```
WARN  Rate limit exceeded for identifier: ip:192.168.1.100, path: /api/auth/login, reset in: 45s
```

### Ver métricas atuais (se usando Redis):

```bash
# Conectar ao Redis
docker-compose exec redis redis-cli

# Ver chaves de rate limit ativas
KEYS ratelimit:*

# Ver detalhes de uma chave
ZRANGE ratelimit:ip:192.168.1.100 0 -1 WITHSCORES
```

### Health check

O endpoint `/actuator/health` está excluído por padrão e não sofre rate limiting.

---

## 🚀 Exemplos de Uso

### Exemplo 1: Testar rate limit com curl

```bash
# Loop para testar limite (deve falhar após 100 requisições)
for i in {1..110}; do
  echo "Request $i"
  curl -i http://localhost:8080/api/calculate?expression=2+2
done
```

### Exemplo 2: Testar com API Key

```bash
# Configurar identifier-type=api-key
# Enviar header X-API-Key
curl -H "X-API-Key: minha-chave-secreta" \
     http://localhost:8080/api/calculate?expression=2+2
```

---

## 🐛 Troubleshooting

### Problema: Rate limit não está funcionando

**Verificações:**
```bash
# 1. Verificar se rate limit está habilitado
grep ratelimit.enabled backend/src/main/resources/application.properties

# 2. Verificar logs de inicialização
docker-compose logs backend | grep "RateLimit"

# 3. Testar com endpoint excluído
curl http://localhost:8080/actuator/health  # não deve ser limitado

# 4. Verificar se Redis está respondendo (se configurado)
docker-compose exec redis redis-cli ping
```

### Problema: Redis desconectado, fallback não funciona

A aplicação vai lançar exceção se Redis estiver configurado mas inacessível. Opções:

1. **Desabilitar Redis temporariamente:** comente `spring.redis.*` no `application.properties`
2. **Verificar conectividade:** `docker-compose logs redis`
3. **Restartar Redis:** `docker-compose restart redis`

### Problema: Limite muito baixo/alto

Ajuste as propriedades:

```properties
ratelimit.max-requests=1000    # aumentar limite
ratelimit.period-minutes=1     # período menor (mais restritivo)
```

### Problema: Mesmo IP batendo limite em diferentes dispositivos

Se atrás de NAT/Proxy, todos podem parecer ter mesmo IP externo. Soluções:

1. Usar `identifier-type=user` com autenticação
2. Usar `identifier-type=api-key`
3. Configurar `X-Forwarded-For` corretamente no proxy

---

## 📈 Escalabilidade

### Redis Cluster

Para alta escala, configure Redis Cluster ou Sentinel:

```properties
spring.redis.cluster.nodes=redis1:6379,redis2:6379,redis3:6379
spring.redis.cluster.max-redirects=3
```

###性能 (Performance)

- **Redis:** ~100k+ requisições/segundo por nó
- **Caffeine:** ~1M+ requisições/segundo (single instance, local)
- Overhead por requisição: ~1-2ms (Redis), <1ms (Caffeine)

---

## 🔒 Considerações de Segurança

1. **Redis sem senha:** Se estiver na mesma rede interna (Docker), pode não precisar. Para exposto, **sempre usar senha**:
   ```properties
   spring.redis.password=sua_senha_super_segura
   ```

2. **Expor Redis apenas para aplicação:** No docker-compose, Redis só acessível na rede interna:
   ```yaml
   redis:
     ports:
       - "127.0.0.1:6379:6379"  # apenas localhost
   ```

3. **Rate limit para admin endpoints:** Adicione endpoints administrativos à lista de exclusão se necessário:
   ```properties
   ratelimit.excluded-paths=/actuator/**,/admin/**
   ```

---

## 📝 Checklist de Implementação

- [x] Adicionado dependências: Redis + Caffeine
- [x] Criado RateLimitProperties (configuração)
- [x] Criado RateLimitService (interface)
- [x] Implementado RedisRateLimitService (Sliding Window Log com Sorted Sets)
- [x] Implementado CaffeineRateLimitService (fallback em-memory)
- [x] Criado RateLimitFilter (intercepta requests)
- [x] Configurado RateLimitConfig (auto-configuração condicional)
- [x] Atualizado application.properties com configurações padrão
- [x] Adicionado Redis ao docker-compose
- [x] Criada documentação completa

---

## 🔗 Recursos Adicionais

- [Sliding Window Log Algorithm](https://redis.io/docs/data-types/sorted-sets/)
- [Spring Boot Redis Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#data.nosql.redis)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)
- [RFC 6585 - 429 Too Many Requests](https://tools.ietf.org/html/rfc6585#section-4)

---

**Última atualização:** 2025-03-21
**Versão:** 1.0.0
