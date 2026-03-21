# GitHub Actions Workflows - Docker Build & Push

Este projeto possui **dois workflows** disponíveis para construir e publicar a imagem Docker:

## 📋 Opção 1: Usando GITHUB_TOKEN (Recomendado)

**Arquivo:** `.github/workflows/docker-build-publish.yml`

Esta é a opção padrão e mais simples.

### Configuração:

1. **Verifique as permissões do workflow:**
   - Vá em **Settings > Actions > General**
   - Em **Workflow permissions**, selecione **"Read and write permissions"**
   - Clique em **Save**

2. **Ative o GitHub Container Registry:**
   - Vá em **Settings > Pages** (ou **Settings > Code and automation > Packages**)
   - Garanta que o GHCR está habilitado para o repositório

3. Não é necessário adicionar nenhum segredo!

4. **Faça um push** para a branch `main` ou crie uma tag `v*.*.*`

A imagem será publicada em:
```
ghcr.io/USERNAME/REPO_NAME:latest
```

---

## 🔐 Opção 2: Usando Personal Access Token (PAT)

**Arquivo:** `.github/workflows/docker-build-publish-pat.yml`

Use esta opção se o GITHUB_TOKEN não tiver permissão suficiente.

### Configuração:

1. **Crie um Personal Access Token (PAT):**
   - Vá em **Settings > Developer settings > Personal access tokens > Tokens (classic)**
   - Clique em **Generate new token (classic)**
   - Dê um nome como `docker-publish`
   - Selecione as seguintes permissões:
     - ✓ `write:packages`
     - ✓ `read:packages`
     - ✓ `repo` (ou `public_repo` se for repositório público)
   - Defina **Expiration** (ex: 90 dias ou no expiration se preferir)
   - Clique em **Generate token**
   - **Copie o token** (você não verá novamente!)

2. **Adicione o token como segredo:**
   - No repositório: **Settings > Secrets and variables > Actions**
   - Clique em **New repository secret**
   - Nome: `PAT_TOKEN`
   - Valor: Cole o token gerado
   - Clique em **Add secret**

3. **Renomeie o workflow (opcional):** Se quiser usar o workflow PAT como padrão, renomeie:
   ```bash
   mv .github/workflows/docker-build-publish-pat.yml .github/workflows/docker-build-publish.yml
   ```

4. **Faça um push** para a branch `main` ou crie uma tag `v*.*.***

A imagem será publicada em:
```
ghcr.io/USERNAME/REPO_NAME:latest
```

---

## 🔍 Verificação

Após o workflow rodar:

1. Acesse: https://github.com/Thy281/CalculetorIot/packages
2. Clique na imagem `calculetoriot`
3. Você verá as tags: `latest`, `main`, `main-<commit-sha>`, e tags semver se aplicável

---

## 🐳 Usando a imagem

### Docker CLI:
```bash
docker pull ghcr.io/thy281/calculetoriot:latest
```

### Docker Compose:
```yaml
version: '3.8'
services:
  backend:
    image: ghcr.io/thy281/calculetoriot:latest
    container_name: calculator-iot-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/calculetoriot
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      GROQ_API_KEY: ${GROQ_API_KEY}
    ports:
      - "8080:8080"
    depends_on:
      - postgres
```

Consulte o `docker-compose.yml` na raiz para a configuração completa.

---

## ⚠️ Troubleshooting

### Erro: `permission_denied: write_package`
- **Solução 1:** Use a Opção 2 (PAT) acima
- **Solução 2:** Verifique se o PAT tem as permissões `write:packages` e `repo`
- **Solução 3:** Verifique se o repositório está configurado para permitir actions de terceiros (se for um fork)

### Erro: `invalid tag "/calculator-iot:..."`
- Isso ocorria em versões anteriores quando havia referência ao Docker Hub
- Foi corrigido - agora usa apenas GHCR com nomes normalizados

### Erro: `SecretsUsedInArgOrEnv`
- Variaveis sensíveis não devem estar no Dockerfile ENV
- Foi corrigido removendo `SPRING_DATASOURCE_PASSWORD` e `GROQ_API_KEY` do Dockerfile
- Passe estas variáveis via docker-compose ou `docker run -e`
