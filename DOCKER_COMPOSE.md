# Docker Compose - CalculatorIot

## 🚀 Quick Start

### 1. Configurar variáveis de ambiente

```bash
cp .env.example .env
# Edite o arquivo .env com seus valores:
# - GROQ_API_KEY: sua chave da API Groq
# - POSTGRES_PASSWORD: senha do PostgreSQL ( padrão: postgres )
```

### 2. Usar imagem do GHCR (recomendado)

O `docker-compose.yml` já está configurado para usar a imagem pública do GitHub Container Registry:

```bash
docker-compose up -d
```

A aplicação estará disponível em: **http://localhost:8080**

### 3. Ou build local (opcional)

Para construir a imagem localmente ao invés de usar a do GHCR:

1. No `docker-compose.yml`, comente a linha `image:` e descomente a seção `build:`
2. Execute:

```bash
docker-compose up -d --build
```

---

## 📋 Comandos úteis

```bash
# Subir os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f backend
docker-compose logs -f postgres

# Parar tudo
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados do PostgreSQL!)
docker-compose down -v

# Reiniciar apenas o backend
docker-compose restart backend

# Executar comando no container
docker-compose exec backend bash

# Ver status dos serviços
docker-compose ps

# Ver estatísticas de recursos
docker-compose top
```

---

## 🔧 Variáveis de Ambiente

| Variável | Descrição | Obrigatório | Padrão |
|----------|-----------|-------------|--------|
| `POSTGRES_PASSWORD` | Senha do PostgreSQL | Não | `postgres` |
| `GROQ_API_KEY` | Chave da API Groq para IA | **Sim** | - |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estratégia do Hibernate | Não | `update` |

### Hibernate DDL Auto modes:
- `update` - Atualiza schema automaticamente (desenvolvimento)
- `validate` - Valida schema sem modificar
- `create` - Cria schema novo a cada iniciada
- `create-drop` - Cria ao iniciar e dropa ao parar

---

## 🏥 Healthchecks

O docker-compose inclui healthchecks para garantir a ordem de inicialização:

1. **PostgreSQL**: Aguarda até 50s (5 tentativas × 10s) para estar pronto
2. **Backend**: Aguarda até 130s antes de iniciar verificações de saúde

O backend só iniciará após o PostgreSQL estar saudável.

---

## 🔄 Atualizando a aplicação

### Quando uma nova imagem é publicada no GHCR:

```bash
# Pull da última imagem
docker-compose pull backend

# Reiniciar o serviço
docker-compose up -d backend
```

### Ou se estiver usando build local:

```bash
docker-compose up -d --build backend
```

---

## 🗄️ Persistência de dados

Os dados do PostgreSQL são persistidos no volume Docker `postgres_data`:

```bash
# Listar volumes
docker volume ls | grep postgres_data

# Fazer backup do volume
docker run --rm -v calculator-iot_postgres_data:/data -v $(pwd):/backup alpine tar czf /backup/postgres_backup.tar.gz -C /data .

# Restaurar backup
docker run --rm -v calculator-iot_postgres_data:/data -v $(pwd):/backup alpine sh -c "cd /data && tar xzf /backup/postgres_backup.tar.gz"
```

---

## 🐛 Troubleshooting

### Erro: "Connection refused" ao conectar ao PostgreSQL

O backend está tentando conectar antes do PostgreSQL estar pronto. Aguarde alguns segundos ou verifique:

```bash
docker-compose logs postgres
docker-compose logs backend
```

### Erro: "Port 8080 already in use"

Mude a porta no docker-compose.yml:

```yaml
ports:
  - "8081:8080"  # Mapeia porta 8081 do host para 8080 do container
```

### Containers não sobem

```bash
# Remover containers órfãos
docker-compose down
docker-compose up -d
```

### Limpar tudo e recomeçar

```bash
docker-compose down -v --remove-orphans
docker system prune -f
docker-compose up -d
```

---

## 📊 Acessando a aplicação

- **Backend API**: http://localhost:8080
- **Health check**: http://localhost:8080/actuator/health
- **PostgreSQL**:
  - Host: localhost
  - Porta: 5432
  - Database: `calculetoriot`
  - Usuário: `postgres`
  - Senha: definida no `.env`

---

## 🔍 Monitoramento

```bash
# Logs em tempo real de todos os serviços
docker-compose logs -f

# Estatísticas de recursos
docker stats

# Ver IP dos containers
docker-compose exec backend hostname -i
docker-compose exec postgres hostname -i
```

---

## 🏗️ Estrutura dos serviços

```
┌─────────────────┐        ┌─────────────────┐
│   Postgres      │:5432   │   Backend       │:8080
│   ( alpine )    │───────▶│   (Java 21)     │
│                 │        │                 │
│  DB: calculetoriot      │  Spring Boot    │
│  User: postgres          │  App            │
└─────────────────┘        └─────────────────┘
        ▲                          │
        │                          │ healthcheck
        └─────── depends_on ────────┘
```

---

## 📝 Notas

- O backend usa multi-stage build para imagem otimizada (~100-150MB)
- O PostgreSQL usa Alpine para menor footprint
- Redes isoladas para segurança
- Volumes nomeados para persistência
- Healthchecks para inicialização confiável
