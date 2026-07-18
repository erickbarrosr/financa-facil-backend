# Backend Finança Fácil — Plano Parte 3: Docker, CI/CD, Observabilidade e OpenAPI

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar Dockerfile multi-stage, docker-compose.yml, pipelines GitHub Actions completos (CI para PRs, CD para main), configuração de Observabilidade e documentação OpenAPI/Swagger.

**Architecture:** Aplicação containerizada com Eclipse Temurin 21, usuário não-root, health check via Actuator. CI/CD via GitHub Actions com Testcontainers no pipeline.

**Tech Stack:** Docker, Docker Compose, GitHub Actions, Trivy (security scan), Railway CLI, Springdoc OpenAPI, Spring Boot Actuator, Micrometer.

## Global Constraints

- Imagem base: `eclipse-temurin:21-jdk-alpine` (build), `eclipse-temurin:21-jre-alpine` (runtime)
- Usuário não-root na imagem de runtime
- Health check aponta para `/api/v1/actuator/health`
- Secrets nunca hardcoded — sempre env vars
- GitHub Actions: Java 21, Gradle cache via `actions/cache`
- Base package: `com.financafacil`

---

### Task 11: Dockerfile e docker-compose.yml

**Files:**
- Create: `Dockerfile`
- Create: `docker-compose.yml`
- Create: `.dockerignore`

**Interfaces:**
- Produces: `docker compose up` inicia a aplicação na porta 8080 com PostgreSQL local

- [ ] **Step 1: Criar `.dockerignore`**

```dockerignore
.gradle/
build/
.git/
.gitignore
.env
.env.*
*.md
docs/
.idea/
*.iml
```

- [ ] **Step 2: Criar `Dockerfile`**

```dockerfile
# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew settings.gradle.kts build.gradle.kts ./
RUN ./gradlew dependencies --no-daemon 2>/dev/null || true
COPY src/ src/
RUN ./gradlew build -x test --no-daemon

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD wget -qO- http://localhost:${SERVER_PORT:-8080}/api/v1/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
```

- [ ] **Step 3: Criar `docker-compose.yml`**

```yaml
services:
  postgres:
    image: postgres:16-alpine
    container_name: financafacil-postgres
    environment:
      POSTGRES_DB: financafacil
      POSTGRES_USER: ${DATABASE_USERNAME:-financafacil}
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD:-financafacil}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DATABASE_USERNAME:-financafacil} -d financafacil"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s

  app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: financafacil-backend
    env_file:
      - .env
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://postgres:5432/financafacil
    ports:
      - "${SERVER_PORT:-8080}:8080"
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

volumes:
  postgres_data:
```

- [ ] **Step 4: Verificar build da imagem Docker**

```bash
docker build -t financafacil-backend:local .
```

Esperado: build em dois estágios completo sem erros.

- [ ] **Step 5: Verificar que o docker-compose sobe corretamente**

Crie um `.env` local com:
```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://postgres:5432/financafacil
DATABASE_USERNAME=financafacil
DATABASE_PASSWORD=financafacil
JWT_ACCESS_SECRET=local-test-secret-must-be-at-least-256-bits-ok
JWT_REFRESH_SECRET=local-test-refresh-secret-must-be-at-least-256-bits-ok
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=http://localhost:5173
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=placeholder
MAIL_FROM=noreply@financafacil.app
FRONTEND_URL=http://localhost:5173
LOG_LEVEL=INFO
```

```bash
docker compose up --build -d
sleep 30
curl -f http://localhost:8080/api/v1/actuator/health
docker compose down
```

Esperado: `{"status":"UP",...}`

- [ ] **Step 6: Commit**

```bash
git add Dockerfile docker-compose.yml .dockerignore
git commit -m "feat: add dockerfile multi-stage and docker-compose"
```

---

### Task 12: GitHub Actions — CI (Pull Requests)

**Files:**
- Create: `.github/workflows/ci.yml`

**Interfaces:**
- Produces: pipeline que roda em todo PR para `main` e `develop`

- [ ] **Step 1: Criar `.github/workflows/ci.yml`**

```yaml
name: CI

on:
  pull_request:
    branches: [main, develop]

jobs:
  build-and-test:
    name: Build, Test & Quality
    runs-on: ubuntu-latest

    services:
      docker:
        image: docker:dind
        options: --privileged

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}
          restore-keys: gradle-

      - name: Grant execute permission to gradlew
        run: chmod +x gradlew

      - name: Checkstyle
        run: ./gradlew checkstyleMain checkstyleTest --no-daemon

      - name: SpotBugs
        run: ./gradlew spotbugsMain --no-daemon

      - name: Unit Tests
        run: ./gradlew test --tests "*Test" --exclude-tests "*IntegrationTest" --no-daemon

      - name: Integration Tests
        run: ./gradlew test --tests "*IntegrationTest" --no-daemon
        env:
          TESTCONTAINERS_RYUK_DISABLED: true

      - name: JaCoCo Coverage Report
        run: ./gradlew jacocoTestReport jacocoTestCoverageVerification --no-daemon

      - name: Upload Coverage Report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: jacoco-report
          path: build/reports/jacoco/test/html/

      - name: Build Docker Image
        run: docker build -t financafacil-backend:ci .

      - name: Security Scan (Trivy)
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'financafacil-backend:ci'
          format: 'table'
          exit-code: '1'
          ignore-unfixed: true
          severity: 'CRITICAL,HIGH'
```

- [ ] **Step 2: Commit**

```bash
git add .github/
git commit -m "ci: add github actions ci pipeline for pull requests"
```

---

### Task 13: GitHub Actions — CD (Main Branch)

**Files:**
- Create: `.github/workflows/cd.yml`

**Interfaces:**
- Produces: pipeline que faz deploy no Railway ao fazer push em `main`

- [ ] **Step 1: Criar `.github/workflows/cd.yml`**

```yaml
name: CD

on:
  push:
    branches: [main]

jobs:
  deploy:
    name: Build, Test & Deploy to Railway
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Cache Gradle
        uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: gradle-${{ hashFiles('**/*.gradle.kts', '**/gradle-wrapper.properties') }}
          restore-keys: gradle-

      - name: Grant execute permission to gradlew
        run: chmod +x gradlew

      - name: Run All Tests
        run: ./gradlew test --no-daemon
        env:
          TESTCONTAINERS_RYUK_DISABLED: true

      - name: JaCoCo Coverage Verification
        run: ./gradlew jacocoTestReport jacocoTestCoverageVerification --no-daemon

      - name: Build Application
        run: ./gradlew build -x test --no-daemon

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and Push Docker Image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ghcr.io/${{ github.repository }}:latest
            ghcr.io/${{ github.repository }}:${{ github.sha }}

      - name: Security Scan (Trivy)
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: 'ghcr.io/${{ github.repository }}:${{ github.sha }}'
          format: 'table'
          exit-code: '1'
          ignore-unfixed: true
          severity: 'CRITICAL,HIGH'

      - name: Install Railway CLI
        run: npm install -g @railway/cli

      - name: Deploy to Railway
        run: railway up --detach
        env:
          RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
```

- [ ] **Step 2: Configurar secrets no GitHub**

No repositório GitHub, acesse `Settings > Secrets and variables > Actions` e adicione:
- `RAILWAY_TOKEN` — token do Railway (obtido em railway.com > Account Settings > Tokens)
- `JWT_ACCESS_SECRET`
- `JWT_REFRESH_SECRET`

O `GITHUB_TOKEN` é automático.

- [ ] **Step 3: Commit**

```bash
git add .github/workflows/cd.yml
git commit -m "ci: add github actions cd pipeline for railway deploy"
```

---

### Task 14: Configuração OpenAPI/Swagger

**Files:**
- Create: `src/main/java/com/financafacil/presentation/openapi/OpenApiConfig.java`

**Interfaces:**
- Produces: Swagger UI acessível em `GET /swagger-ui.html` com autenticação Bearer documentada

- [ ] **Step 1: Criar `OpenApiConfig.java`**

```java
package com.financafacil.presentation.openapi;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Finança Fácil API")
                .description("Backend do clone do Organizze — gerenciamento financeiro pessoal")
                .version("v1")
                .contact(new Contact().name("Finança Fácil").email("contato@financafacil.app"))
                .license(new License().name("MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Insira o access token JWT obtido em POST /auth/login")));
    }
}
```

- [ ] **Step 2: Verificar Swagger UI**

```bash
./gradlew bootRun --args='--spring.profiles.active=dev' &
sleep 15
curl -s http://localhost:8080/api/v1/v3/api-docs | python3 -m json.tool | head -20
```

Esperado: JSON OpenAPI com `"title": "Finança Fácil API"`.

```bash
pkill -f "bootRun" || true
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/financafacil/presentation/openapi/
git commit -m "feat: add openapi/swagger configuration with bearer auth"
```

---

### Task 15: Executar suite completa e verificar cobertura

**Files:**
- Nenhum arquivo novo — verificação final

**Interfaces:**
- Produces: build verde, cobertura ≥ 70% global

- [ ] **Step 1: Executar build completo com todos os checks**

```bash
./gradlew clean build --no-daemon
```

Esperado: `BUILD SUCCESSFUL`

- [ ] **Step 2: Verificar relatório JaCoCo**

```bash
./gradlew jacocoTestReport --no-daemon
echo "Relatório em: build/reports/jacoco/test/html/index.html"
```

Abra `build/reports/jacoco/test/html/index.html` no browser e verifique:
- Cobertura global ≥ 70%
- Cobertura em `domain` e `application` ≥ 80%

Se a cobertura estiver abaixo, identifique os pacotes com baixa cobertura e adicione testes unitários correspondentes antes de prosseguir.

- [ ] **Step 3: Verificar que Flyway valida o schema**

```bash
./gradlew bootRun --args='--spring.profiles.active=dev' &
sleep 20
curl -s http://localhost:8080/api/v1/actuator/health | python3 -m json.tool
pkill -f "bootRun" || true
```

Esperado: `"status": "UP"` com `db` e `diskSpace` UP.

- [ ] **Step 4: Commit final da Parte 3**

```bash
git add -A
git commit -m "feat: complete backend infrastructure - docker, ci/cd, openapi, coverage verified"
```
