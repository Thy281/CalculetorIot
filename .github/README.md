# GitHub Actions Workflow - Docker Build & Push

Este workflow constrói e publica automaticamente a imagem Docker do projeto.

## Como configurar

### 1. Secrets do GitHub

No seu repositório no GitHub, vá em **Settings > Secrets and variables > Actions** e adicione:

#### Opcional: Docker Hub (se quiser publicar lá também)
- `DOCKER_USERNAME` - seu username do Docker Hub
- `DOCKER_PASSWORD` - sua senha ou access token do Docker Hub

#### GitHub Container Registry (GCR)
O workflow já usa o `GITHUB_TOKEN` padrão, que tem permissão automática para o GHCR.

### 2. Nome da imagem

O workflow usa automaticamente o nome do repositório: `ghcr.io/USERNAME/REPO_NAME`

Para personalizar, edite a variável `IMAGE_NAME` no arquivo `.github/workflows/docker-build-publish.yml`.

### 3. Triggers

O workflow roda automaticamente em:
- Push para branches `main`, `master` ou `develop*`
- Pull requests para `main` ou `master`
- Tags seguindo padrão semântico (`v1.0.0`, `v2.1.3-beta`, etc)

### 4. Tags geradas

A imagem será tagged com:
- `latest` (apenas na branch main)
- Nome da branch (ex: `main-abc1234`)
- Versão semântica (se tag for `v1.2.3`: `1.2.3`, `1.2`, `1`)
- SHA do commit

## Como usar a imagem publicada

### Docker Hub (se configurado)
```
docker pull <seu-username>/calculator-iot:latest
```

### GitHub Container Registry
```
docker pull ghcr.io/<seu-username>/<repositorio>:latest
```

## Rodar com Docker Compose

```bash
# Clone o repositório
git clone https://github.com/USERNAME/REPO.git
cd PROJETO-IOT

# Copie o arquivo de variáveis de ambiente
cp .env.example .env
# Edite o .env com suas configurações (GROQ_API_KEY, etc.)

# Suba os serviços
docker-compose up -d

# A aplicação estará disponível em:
# - Backend: http://localhost:8080
# - PostgreSQL: localhost:5432
```
