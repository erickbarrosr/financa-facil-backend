# Design Spec — Backend Finança Fácil (Clone Organizze)

**Data:** 2026-07-18  
**Status:** Aprovado  
**Escopo:** Backend completo (Java 21 + Spring Boot 3) + integração com frontend React existente  

---

## 1. Contexto

O frontend já existe em `financa-facil-frontend` (React 19 + TanStack Router + Zustand), com dados totalmente mockados localmente. O objetivo é:

1. Construir o backend production-ready em `financa-facil-backend`
2. Migrar o frontend para consumir a API REST (substituir o mock Zustand por React Query + chamadas HTTP)

Deploy alvo: **Railway**. Stack obrigatória conforme prompt original.

---

## 2. Decisões de design

| Decisão | Escolha |
|---|---|
| Integração frontend | Sim — backend + migração do frontend |
| Prioridade MVP | Auth + Accounts + Categories + Transactions + Dashboard (telas existentes); Budget/Transfer/Recurring prontos no backend sem tela |
| Saldo de conta | Desnormalizado (`balance` atualizado via `@Transactional`) |
| Status de transação | Enum rico: `PENDING`, `PAID`, `OVERDUE`, `CANCELLED` |
| Rate Limiting | Sem por enquanto (Railway protege na borda) |
| Email | Envio real via JavaMailSender (SMTP configurável — SendGrid, SES, Gmail) |
| Arquitetura | Clean Architecture estrita com módulos por domínio |

---

## 3. Arquitetura

### 3.1 Estrutura de pacotes

```
com.financafacil
├── domain/
│   ├── model/                  # Entidades de domínio (sem anotações JPA)
│   ├── port/
│   │   ├── in/                 # Use case interfaces (entrada)
│   │   └── out/                # Repository/email interfaces (saída)
│   └── exception/              # DomainException, NotFoundException, etc.
│
├── application/
│   ├── usecase/                # Implementações dos ports in
│   └── dto/                    # DTOs internos da camada de application
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/             # JPA @Entity
│   │   ├── repository/         # Spring Data JPA + Adapters dos ports out
│   │   ├── mapper/             # MapStruct: domain ↔ JPA entity
│   │   └── specification/      # JPA Specifications para filtros dinâmicos
│   ├── security/
│   │   ├── jwt/                # JwtService, JwtAuthenticationFilter
│   │   ├── userdetails/        # UserDetailsServiceImpl
│   │   └── config/             # SecurityFilterChain, CorsConfig
│   ├── email/                  # Adaptador JavaMailSender
│   └── config/                 # Beans globais (Clock, ObjectMapper, etc.)
│
└── presentation/
    ├── controller/             # @RestController por módulo
    ├── dto/
    │   ├── request/            # Request DTOs com Bean Validation
    │   └── response/           # Response DTOs padronizados
    ├── mapper/                 # MapStruct: domain ↔ presentation DTOs
    ├── exception/              # @ControllerAdvice, GlobalExceptionHandler
    └── openapi/                # Configuração Springdoc/Swagger
```

### 3.2 Módulos funcionais

| Módulo | Camadas atravessadas | Frontend integrado |
|---|---|---|
| `auth` | todas | Sim (login, signup, forgot-password) |
| `account` | todas | Sim |
| `category` | todas | Sim |
| `transaction` | todas | Sim |
| `dashboard` | application + infrastructure + presentation | Sim |
| `budget` | todas | Não (backend pronto) |
| `transfer` | todas | Não (backend pronto) |
| `recurring` | todas | Não (backend pronto) |

### 3.3 Envelope de resposta padronizado

```json
// Sucesso
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "timestamp": "2026-07-18T10:00:00Z"
}

// Erro
{
  "success": false,
  "error": {
    "code": "ACCOUNT_NOT_FOUND",
    "message": "Conta não encontrada"
  },
  "timestamp": "2026-07-18T10:00:00Z"
}

// Lista paginada
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

## 4. Segurança

### 4.1 Fluxo JWT

- **Access Token:** JWT assinado com `JWT_ACCESS_SECRET`, expiração configurável (`JWT_ACCESS_EXPIRATION`, default 15 min). Payload: `userId`, `email`, `roles`
- **Refresh Token:** UUID opaco; persiste no banco como hash BCrypt; duração configurável (`JWT_REFRESH_EXPIRATION`, default 7 dias)
- **Rotation:** a cada `/auth/refresh`, o token atual é revogado e um novo par é emitido
- **Logout:** revoga o refresh token; access token expira naturalmente (sem blacklist)
- **Refresh Token no cookie:** `HttpOnly; Secure; SameSite=Strict` — nunca exposto ao JavaScript

### 4.2 Pipeline de filtros

```
Request
  → JwtAuthenticationFilter       # valida JWT, popula SecurityContext
  → SecurityFilterChain           # rotas públicas vs protegidas
  → Controller
```

**Rotas públicas:**
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
- `POST /api/v1/auth/verify-email`
- `GET /actuator/health/**`
- `GET /swagger-ui/**`, `GET /v3/api-docs/**`

Tudo mais exige `Authorization: Bearer <access_token>`.

### 4.3 Fluxo de email

**Reset de senha:**
1. `POST /auth/forgot-password` → token UUID gerado, hash BCrypt persistido em `password_reset_tokens` com TTL de 1h; email enviado com link `FRONTEND_URL/reset-password?token=<raw>`
2. `POST /auth/reset-password` → valida token (hash + expiração + não usado), aplica BCrypt no novo password, marca token como `used = true`

**Verificação de email:**
- Mesmo padrão, tabela `email_verification_tokens`, marca `user.email_verified = true`

### 4.4 Medidas adicionais

| Medida | Implementação |
|---|---|
| Anti-enumeração | Mensagens genéricas em login/forgot-password |
| Mass Assignment | Nunca expor JPA entities; DTOs explícitos |
| SQL Injection | Spring Data JPA + Specifications (sem SQL manual) |
| Secrets | Zero no código; `@ConfigurationProperties` + env vars |
| Security Headers | Configurados no `SecurityFilterChain` |
| Audit Trail | `AuditLog` gravado nos eventos críticos |
| CORS | Configurável via `CORS_ALLOWED_ORIGINS` |
| CSRF | Desabilitado (API stateless com JWT) |
| Isolamento de dados | Todo endpoint valida `userId` extraído do JWT |

### 4.5 RBAC

- Role inicial: `ROLE_USER`
- Arquitetura preparada para `ROLE_ADMIN` sem refatoração (campo `roles` no JWT, tabela preparada)

---

## 5. Modelagem do banco de dados

### 5.1 Convenções

- IDs: `UUID` gerado pela aplicação (não serial/sequence do banco)
- `created_at` / `updated_at`: `@CreationTimestamp` / `@UpdateTimestamp`
- Flyway gerencia o schema exclusivamente; `ddl-auto=validate` em produção
- Soft delete apenas em `users` (`is_active`); demais entidades: delete físico

### 5.2 Schema

**`users`**
```sql
id            UUID PRIMARY KEY,
name          VARCHAR(150) NOT NULL,
email         VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
last_login_at TIMESTAMPTZ,
email_verified BOOLEAN NOT NULL DEFAULT false,
is_active     BOOLEAN NOT NULL DEFAULT true
```

**`accounts`**
```sql
id         UUID PRIMARY KEY,
user_id    UUID NOT NULL REFERENCES users(id),
name       VARCHAR(150) NOT NULL,
type       VARCHAR(20) NOT NULL CHECK (type IN ('checking','wallet','credit_card','investment')),
balance    DECIMAL(19,4) NOT NULL DEFAULT 0,
color      VARCHAR(7) NOT NULL,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`categories`**
```sql
id         UUID PRIMARY KEY,
user_id    UUID NOT NULL REFERENCES users(id),
name       VARCHAR(100) NOT NULL,
type       VARCHAR(10) NOT NULL CHECK (type IN ('income','expense')),
icon       VARCHAR(50),
color      VARCHAR(7) NOT NULL,
created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`transactions`**
```sql
id               UUID PRIMARY KEY,
user_id          UUID NOT NULL REFERENCES users(id),
account_id       UUID NOT NULL REFERENCES accounts(id),
category_id      UUID REFERENCES categories(id),
type             VARCHAR(10) NOT NULL CHECK (type IN ('income','expense')),
amount           DECIMAL(19,4) NOT NULL,
description      TEXT,
transaction_date DATE NOT NULL,
status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                   CHECK (status IN ('PENDING','PAID','OVERDUE','CANCELLED')),
created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`transfers`**
```sql
id              UUID PRIMARY KEY,
user_id         UUID NOT NULL REFERENCES users(id),
from_account_id UUID NOT NULL REFERENCES accounts(id),
to_account_id   UUID NOT NULL REFERENCES accounts(id),
amount          DECIMAL(19,4) NOT NULL,
transfer_date   DATE NOT NULL,
description     TEXT,
created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`budgets`**
```sql
id          UUID PRIMARY KEY,
user_id     UUID NOT NULL REFERENCES users(id),
category_id UUID NOT NULL REFERENCES categories(id),
amount      DECIMAL(19,4) NOT NULL,
month       SMALLINT NOT NULL CHECK (month BETWEEN 1 AND 12),
year        SMALLINT NOT NULL,
created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
UNIQUE (user_id, category_id, month, year)
```

**`recurring_transactions`**
```sql
id             UUID PRIMARY KEY,
user_id        UUID NOT NULL REFERENCES users(id),
account_id     UUID NOT NULL REFERENCES accounts(id),
category_id    UUID REFERENCES categories(id),
amount         DECIMAL(19,4) NOT NULL,
type           VARCHAR(10) NOT NULL CHECK (type IN ('income','expense')),
frequency      VARCHAR(10) NOT NULL CHECK (frequency IN ('daily','weekly','monthly','yearly')),
next_execution DATE NOT NULL,
active         BOOLEAN NOT NULL DEFAULT true,
created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`refresh_tokens`**
```sql
id         UUID PRIMARY KEY,
user_id    UUID NOT NULL REFERENCES users(id),
token_hash VARCHAR(255) NOT NULL,
expires_at TIMESTAMPTZ NOT NULL,
revoked    BOOLEAN NOT NULL DEFAULT false,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`audit_logs`**
```sql
id         UUID PRIMARY KEY,
user_id    UUID REFERENCES users(id),
action     VARCHAR(100) NOT NULL,
entity     VARCHAR(100),
entity_id  UUID,
metadata   JSONB,
ip         VARCHAR(45),
user_agent TEXT,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`password_reset_tokens`**
```sql
id         UUID PRIMARY KEY,
user_id    UUID NOT NULL REFERENCES users(id),
token_hash VARCHAR(255) NOT NULL,
expires_at TIMESTAMPTZ NOT NULL,
used       BOOLEAN NOT NULL DEFAULT false,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

**`email_verification_tokens`**
```sql
id         UUID PRIMARY KEY,
user_id    UUID NOT NULL REFERENCES users(id),
token_hash VARCHAR(255) NOT NULL,
expires_at TIMESTAMPTZ NOT NULL,
used       BOOLEAN NOT NULL DEFAULT false,
created_at TIMESTAMPTZ NOT NULL DEFAULT now()
```

### 5.3 Migrations Flyway

```
V1__create_users.sql
V2__create_accounts.sql
V3__create_categories.sql
V4__create_transactions.sql
V5__create_transfers.sql
V6__create_budgets.sql
V7__create_recurring_transactions.sql
V8__create_refresh_tokens.sql
V9__create_audit_logs.sql
V10__create_password_reset_tokens.sql
V11__create_email_verification_tokens.sql
V12__create_indexes.sql
```

**Índices (`V12`):** `user_id` em todas as tabelas, `transaction_date`, `status`, `type` em `transactions`, `expires_at` + `revoked` em `refresh_tokens`.

---

## 6. Endpoints da API

Base path: `/api/v1`

### Auth

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/auth/register` | Pública | Cria usuário + envia email de verificação |
| POST | `/auth/login` | Pública | Retorna access token; refresh token em cookie HttpOnly |
| POST | `/auth/refresh` | Pública (cookie) | Rotation: revoga atual, emite novo par |
| POST | `/auth/logout` | Bearer | Revoga refresh token |
| POST | `/auth/forgot-password` | Pública | Envia email com link de reset |
| POST | `/auth/reset-password` | Pública | Aplica novo password via token |
| POST | `/auth/verify-email` | Pública | Marca email como verificado |
| GET | `/auth/me` | Bearer | Retorna perfil do usuário |

### Accounts

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/accounts` | Lista contas do usuário |
| POST | `/accounts` | Cria conta; o campo `initialBalance` do request é persistido diretamente como `balance` inicial |
| PUT | `/accounts/{id}` | Atualiza nome/tipo/cor |
| DELETE | `/accounts/{id}` | Remove conta (sem transações vinculadas) |
| GET | `/accounts/{id}/balance` | Saldo atual desnormalizado |

### Categories

| Método | Endpoint |
|---|---|
| GET | `/categories?type=income\|expense` |
| POST | `/categories` |
| PUT | `/categories/{id}` |
| DELETE | `/categories/{id}` |

### Transactions

| Método | Endpoint | Observação |
|---|---|---|
| GET | `/transactions` | Paginado com filtros |
| POST | `/transactions` | Atualiza `account.balance` via `@Transactional` |
| PUT | `/transactions/{id}` | Recalcula delta no saldo |
| DELETE | `/transactions/{id}` | Reverte saldo |

**Query params para GET /transactions:**
```
startDate, endDate   (formato: yyyy-MM-dd)
categoryId           (UUID)
accountId            (UUID)
type                 (income | expense)
status               (PENDING | PAID | OVERDUE | CANCELLED)
page                 (default: 0)
size                 (default: 20)
sort                 (default: transactionDate,desc)
```

### Dashboard

| Método | Endpoint | Dados |
|---|---|---|
| GET | `/dashboard/summary` | Saldo total, receitas e despesas do mês (`?year=&month=`) |
| GET | `/dashboard/monthly-series` | Receitas vs despesas (`?months=6`) |
| GET | `/dashboard/by-category` | Gastos por categoria (`?year=&month=&type=expense`) |
| GET | `/dashboard/recent-transactions` | Últimas transações (`?limit=6`) |

### Outros módulos (backend pronto, sem tela no frontend por ora)

- `GET/POST/PUT/DELETE /budgets`
- `GET/POST/PUT/DELETE /transfers`
- `GET/POST/PUT/DELETE /recurring-transactions`

---

## 7. Integração com o Frontend

### 7.1 Mudanças no frontend

**1. Cliente HTTP (`src/lib/api-client.ts`)**
- Axios com interceptor de request: injeta `Authorization: Bearer <token>`
- Interceptor de response: detecta 401, chama `/auth/refresh` automaticamente, reenvia request original
- Access token em memória (variável de módulo); refresh token em cookie HttpOnly (transparente)

**2. Migração da autenticação**
- `login`/`signup`/`logout` no Zustand passam a chamar a API
- Access token armazenado em memória; sem `localStorage` para tokens
- Zustand mantém apenas `user: User | null` e `authed: boolean`

**3. Migração dos dados para React Query**
- Remover `accounts`, `categories`, `transactions` do Zustand store
- Criar hooks: `useAccounts()`, `useCategories()`, `useTransactions(filters)`, `useDashboardSummary()`, `useDashboardSeries()`, `useDashboardByCategory()`, `useDashboardRecent()`
- Zustand fica apenas para estado de UI (modais abertos, filtros selecionados)

**4. Campo `paid` → `status`**
- Frontend atualizado para usar enum `PENDING | PAID | OVERDUE | CANCELLED`
- UI exibe badge de status; filtro de transações usa o novo enum

---

## 8. Testes

### 8.1 Estratégia

| Tipo | Ferramentas | Cobertura alvo |
|---|---|---|
| Unit | JUnit 5 + Mockito | Use cases e lógica de domínio (≥ 80%) |
| Integration | Testcontainers + REST Assured | Fluxos críticos ponta a ponta |
| JaCoCo | Build falha se cobertura global < 70% | — |

### 8.2 Fluxos críticos com cobertura obrigatória

- Register → Login → Refresh → Logout
- Create Transaction → verificar `account.balance` atualizado
- Update Transaction com mudança de valor → verificar delta no saldo
- Delete Transaction → verificar reversão do saldo
- Transfer entre contas → verificar consistência dos dois saldos (ACID)
- Forgot Password → Reset Password (token válido, expirado e já usado)
- Filtros de Transaction com paginação

---

## 9. Observabilidade

| Endpoint | Descrição |
|---|---|
| `/actuator/health` | Status geral |
| `/actuator/health/liveness` | Liveness probe (Railway) |
| `/actuator/health/readiness` | Readiness probe (Railway) |
| `/actuator/info` | Versão, build info |
| `/actuator/metrics` | Métricas Micrometer |

- Log texto em dev; log JSON estruturado em produção
- Level controlado por `LOG_LEVEL` (env var)
- Eventos de segurança logados via SLF4J com campos estruturados

---

## 10. Docker

### Dockerfile (multi-stage)

```dockerfile
# Stage 1: build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test --no-daemon

# Stage 2: runtime
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
USER app
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s \
  CMD wget -qO- http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml

```yaml
services:
  app:
    build: .
    ports: ["8080:8080"]
    env_file: .env
    depends_on:
      postgres:
        condition: service_healthy

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: financafacil
      POSTGRES_USER: ${DATABASE_USERNAME}
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DATABASE_USERNAME}"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

---

## 11. Variáveis de ambiente

```env
# Aplicação
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080

# Banco
DATABASE_URL=jdbc:postgresql://host:5432/financafacil
DATABASE_USERNAME=
DATABASE_PASSWORD=

# JWT
JWT_ACCESS_SECRET=
JWT_REFRESH_SECRET=
JWT_ACCESS_EXPIRATION=900000        # 15 min em ms
JWT_REFRESH_EXPIRATION=604800000    # 7 dias em ms

# CORS
CORS_ALLOWED_ORIGINS=https://seu-frontend.railway.app

# Email
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=
MAIL_FROM=noreply@financafacil.app

# Frontend (para links nos emails)
FRONTEND_URL=https://seu-frontend.railway.app

# Logs
LOG_LEVEL=INFO
```

Arquivos: `.env.example` (commitado), `.env.development`, `.env.test`, `.env.production` (não commitados).

---

## 12. CI/CD — GitHub Actions

### `ci.yml` — Pull Requests

```
checkout
→ setup Java 21 + Gradle cache
→ Checkstyle
→ SpotBugs
→ Unit Tests
→ Integration Tests (Testcontainers via Docker-in-Docker)
→ JaCoCo (falha se < 70%)
→ Build Docker image (sem push)
→ Security Scan (Trivy — imagem)
```

### `cd.yml` — push em `main`

```
(todos os steps do CI)
→ Flyway validation
→ Push imagem para registry
→ Deploy no Railway (Railway CLI ou webhook)
```

**Secrets necessários no GitHub:** `RAILWAY_TOKEN`, `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`, `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL`

---

## 13. Configuração por ambiente

| Arquivo | Perfil Spring | Uso |
|---|---|---|
| `application.yml` | — | Base compartilhada |
| `application-dev.yml` | `dev` | Local; log texto; Flyway `clean-on-validation-error=true` |
| `application-test.yml` | `test` | Testcontainers; Flyway habilitado; sem email real |
| `application-prod.yml` | `prod` | Railway; log JSON; `ddl-auto=validate`; email real |

---

## 14. Fora do escopo (desta iteração)

- Telas de Budget, Transfer e RecurringTransaction no frontend
- Rate Limiting (adicionado quando necessário)
- Multi-tenancy
- Notificações push
- Exportação de relatórios (PDF/CSV)
