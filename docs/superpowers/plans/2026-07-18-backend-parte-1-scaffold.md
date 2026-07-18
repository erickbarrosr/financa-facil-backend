# Backend Finança Fácil — Plano Parte 1: Scaffold, Configuração e Segurança

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Criar o projeto Spring Boot 3 com Clean Architecture, banco de dados, Flyway, JWT, autenticação completa (register/login/refresh/logout/forgot-password/reset-password/verify-email) e testes de integração rodando com Testcontainers.

**Architecture:** Clean Architecture estrita com módulos por domínio. Camadas: `domain → application → infrastructure → presentation`. Cada módulo tem seus próprios ports (interfaces), use cases e adaptadores JPA + REST.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Security 6, Spring Data JPA, Hibernate, Flyway, PostgreSQL, Gradle (Kotlin DSL), JWT (jjwt), BCrypt, MapStruct, Lombok, Springdoc OpenAPI, JUnit 5, Mockito, Testcontainers, REST Assured, JaCoCo, JavaMailSender.

## Global Constraints

- Java 21 LTS obrigatório
- Spring Boot 3.x obrigatório
- Gradle Kotlin DSL (build.gradle.kts)
- Todos os IDs são UUID gerados pela aplicação
- Nunca expor JPA entities diretamente — sempre DTOs
- `ddl-auto=validate` em produção; Flyway gerencia schema
- Secrets sempre via variáveis de ambiente — zero hardcoded
- Todo endpoint autenticado valida `userId` do JWT (nunca do body)
- Respostas sempre no envelope `ApiResponse<T>`
- Cobertura JaCoCo mínima: 70% global, 80% em `domain` e `application`
- Base package: `com.financafacil`

---

### Task 1: Scaffold do projeto Gradle + dependências

**Files:**
- Create: `build.gradle.kts`
- Create: `settings.gradle.kts`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-test.yml`
- Create: `src/main/resources/application-prod.yml`
- Create: `src/main/java/com/financafacil/FinancaFacilApplication.java`
- Create: `.env.example`
- Create: `.gitignore`

**Interfaces:**
- Produces: projeto compilável com `./gradlew build -x test`

- [ ] **Step 1: Criar `settings.gradle.kts`**

```kotlin
rootProject.name = "financa-facil-backend"
```

- [ ] **Step 2: Criar `build.gradle.kts`**

```kotlin
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("jacoco")
    id("checkstyle")
    id("com.github.spotbugs") version "6.0.26"
}

group = "com.financafacil"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

configurations {
    compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
}

repositories {
    mavenCentral()
}

val jjwtVersion = "0.12.6"
val mapstructVersion = "1.6.3"
val lombokMapstructBindingVersion = "0.2.0"
val testcontainersVersion = "1.20.4"
val restAssuredVersion = "5.5.0"
val springdocVersion = "2.6.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Database
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    // MapStruct + Lombok
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:$lombokMapstructBindingVersion")

    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Observability
    implementation("io.micrometer:micrometer-registry-prometheus")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
    testImplementation("io.rest-assured:spring-mock-mvc:$restAssuredVersion")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

checkstyle {
    toolVersion = "10.18.2"
    configFile = file("config/checkstyle/checkstyle.xml")
}
```

- [ ] **Step 3: Criar `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

- [ ] **Step 4: Criar `src/main/resources/application.yml`**

```yaml
spring:
  application:
    name: financa-facil-backend
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
    open-in-view: false
  flyway:
    enabled: true
    locations: classpath:db/migration
  mail:
    host: ${MAIL_HOST:smtp.sendgrid.net}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: /api/v1

app:
  jwt:
    access-secret: ${JWT_ACCESS_SECRET}
    refresh-secret: ${JWT_REFRESH_SECRET}
    access-expiration-ms: ${JWT_ACCESS_EXPIRATION:900000}
    refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION:604800000}
  cors:
    allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:5173}
  mail:
    from: ${MAIL_FROM:noreply@financafacil.app}
  frontend:
    url: ${FRONTEND_URL:http://localhost:5173}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

- [ ] **Step 5: Criar `src/main/resources/application-dev.yml`**

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  flyway:
    clean-on-validation-error: true

logging:
  level:
    com.financafacil: DEBUG
    org.springframework.security: DEBUG
```

- [ ] **Step 6: Criar `src/main/resources/application-test.yml`**

```yaml
spring:
  datasource:
    url: jdbc:tc:postgresql:16-alpine:///financafacil_test
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true

app:
  jwt:
    access-secret: test-access-secret-must-be-at-least-256-bits-long-for-hs256
    refresh-secret: test-refresh-secret-must-be-at-least-256-bits-long-for-hs256
    access-expiration-ms: 900000
    refresh-expiration-ms: 604800000
  cors:
    allowed-origins: http://localhost:5173
  mail:
    from: test@test.com
  frontend:
    url: http://localhost:5173

spring.mail:
  host: localhost
  port: 25
```

- [ ] **Step 7: Criar `src/main/resources/application-prod.yml`**

```yaml
spring:
  jpa:
    show-sql: false

logging:
  pattern:
    console: '{"timestamp":"%d{yyyy-MM-dd HH:mm:ss.SSS}","level":"%p","logger":"%logger{36}","message":"%m"}%n'
  level:
    root: ${LOG_LEVEL:INFO}
    com.financafacil: ${LOG_LEVEL:INFO}
```

- [ ] **Step 8: Criar `src/main/java/com/financafacil/FinancaFacilApplication.java`**

```java
package com.financafacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinancaFacilApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinancaFacilApplication.class, args);
    }
}
```

- [ ] **Step 9: Criar `.env.example`**

```env
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://localhost:5432/financafacil
DATABASE_USERNAME=financafacil
DATABASE_PASSWORD=financafacil
JWT_ACCESS_SECRET=your-256-bit-secret-here-replace-in-production
JWT_REFRESH_SECRET=your-256-bit-refresh-secret-here-replace-in-production
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=http://localhost:5173
MAIL_HOST=smtp.sendgrid.net
MAIL_PORT=587
MAIL_USERNAME=apikey
MAIL_PASSWORD=your-sendgrid-api-key
MAIL_FROM=noreply@financafacil.app
FRONTEND_URL=http://localhost:5173
LOG_LEVEL=INFO
```

- [ ] **Step 10: Criar `.gitignore`**

```gitignore
.gradle/
build/
.env
.env.development
.env.test
.env.production
*.class
*.jar
!gradle-wrapper.jar
.idea/
*.iml
*.iws
.DS_Store
```

- [ ] **Step 11: Criar `config/checkstyle/checkstyle.xml`**

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="error"/>
    <module name="TreeWalker">
        <module name="UnusedImports"/>
        <module name="AvoidStarImport"/>
        <module name="NeedBraces"/>
        <module name="EqualsHashCode"/>
    </module>
</module>
```

- [ ] **Step 12: Verificar que o projeto compila**

```bash
./gradlew compileJava -x test
```

Esperado: `BUILD SUCCESSFUL`

- [ ] **Step 13: Commit**

```bash
git add -A
git commit -m "feat: scaffold spring boot project with gradle and configurations"
```

---

### Task 2: Migrations Flyway

**Files:**
- Create: `src/main/resources/db/migration/V1__create_users.sql`
- Create: `src/main/resources/db/migration/V2__create_accounts.sql`
- Create: `src/main/resources/db/migration/V3__create_categories.sql`
- Create: `src/main/resources/db/migration/V4__create_transactions.sql`
- Create: `src/main/resources/db/migration/V5__create_transfers.sql`
- Create: `src/main/resources/db/migration/V6__create_budgets.sql`
- Create: `src/main/resources/db/migration/V7__create_recurring_transactions.sql`
- Create: `src/main/resources/db/migration/V8__create_refresh_tokens.sql`
- Create: `src/main/resources/db/migration/V9__create_audit_logs.sql`
- Create: `src/main/resources/db/migration/V10__create_password_reset_tokens.sql`
- Create: `src/main/resources/db/migration/V11__create_email_verification_tokens.sql`
- Create: `src/main/resources/db/migration/V12__create_indexes.sql`

**Interfaces:**
- Produces: schema PostgreSQL completo pronto para uso pelo JPA

- [ ] **Step 1: Criar `V1__create_users.sql`**

```sql
CREATE TABLE users (
    id             UUID PRIMARY KEY,
    name           VARCHAR(150)  NOT NULL,
    email          VARCHAR(255)  NOT NULL UNIQUE,
    password_hash  VARCHAR(255)  NOT NULL,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    last_login_at  TIMESTAMPTZ,
    email_verified BOOLEAN       NOT NULL DEFAULT false,
    is_active      BOOLEAN       NOT NULL DEFAULT true
);
```

- [ ] **Step 2: Criar `V2__create_accounts.sql`**

```sql
CREATE TABLE accounts (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(150) NOT NULL,
    type       VARCHAR(20)  NOT NULL CHECK (type IN ('checking','wallet','credit_card','investment')),
    balance    DECIMAL(19,4) NOT NULL DEFAULT 0,
    color      VARCHAR(7)   NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

- [ ] **Step 3: Criar `V3__create_categories.sql`**

```sql
CREATE TABLE categories (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    type       VARCHAR(10)  NOT NULL CHECK (type IN ('income','expense')),
    icon       VARCHAR(50),
    color      VARCHAR(7)   NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

- [ ] **Step 4: Criar `V4__create_transactions.sql`**

```sql
CREATE TABLE transactions (
    id               UUID          PRIMARY KEY,
    user_id          UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id       UUID          NOT NULL REFERENCES accounts(id),
    category_id      UUID          REFERENCES categories(id) ON DELETE SET NULL,
    type             VARCHAR(10)   NOT NULL CHECK (type IN ('income','expense')),
    amount           DECIMAL(19,4) NOT NULL,
    description      TEXT,
    transaction_date DATE          NOT NULL,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING','PAID','OVERDUE','CANCELLED')),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);
```

- [ ] **Step 5: Criar `V5__create_transfers.sql`**

```sql
CREATE TABLE transfers (
    id              UUID          PRIMARY KEY,
    user_id         UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    from_account_id UUID          NOT NULL REFERENCES accounts(id),
    to_account_id   UUID          NOT NULL REFERENCES accounts(id),
    amount          DECIMAL(19,4) NOT NULL,
    transfer_date   DATE          NOT NULL,
    description     TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);
```

- [ ] **Step 6: Criar `V6__create_budgets.sql`**

```sql
CREATE TABLE budgets (
    id          UUID          PRIMARY KEY,
    user_id     UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id UUID          NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    amount      DECIMAL(19,4) NOT NULL,
    month       SMALLINT      NOT NULL CHECK (month BETWEEN 1 AND 12),
    year        SMALLINT      NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (user_id, category_id, month, year)
);
```

- [ ] **Step 7: Criar `V7__create_recurring_transactions.sql`**

```sql
CREATE TABLE recurring_transactions (
    id             UUID          PRIMARY KEY,
    user_id        UUID          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id     UUID          NOT NULL REFERENCES accounts(id),
    category_id    UUID          REFERENCES categories(id) ON DELETE SET NULL,
    amount         DECIMAL(19,4) NOT NULL,
    type           VARCHAR(10)   NOT NULL CHECK (type IN ('income','expense')),
    frequency      VARCHAR(10)   NOT NULL CHECK (frequency IN ('daily','weekly','monthly','yearly')),
    next_execution DATE          NOT NULL,
    active         BOOLEAN       NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);
```

- [ ] **Step 8: Criar `V8__create_refresh_tokens.sql`**

```sql
CREATE TABLE refresh_tokens (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

- [ ] **Step 9: Criar `V9__create_audit_logs.sql`**

```sql
CREATE TABLE audit_logs (
    id         UUID         PRIMARY KEY,
    user_id    UUID         REFERENCES users(id) ON DELETE SET NULL,
    action     VARCHAR(100) NOT NULL,
    entity     VARCHAR(100),
    entity_id  UUID,
    metadata   JSONB,
    ip         VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

- [ ] **Step 10: Criar `V10__create_password_reset_tokens.sql`**

```sql
CREATE TABLE password_reset_tokens (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

- [ ] **Step 11: Criar `V11__create_email_verification_tokens.sql`**

```sql
CREATE TABLE email_verification_tokens (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

- [ ] **Step 12: Criar `V12__create_indexes.sql`**

```sql
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transfers_user_id ON transfers(user_id);
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_recurring_user_id ON recurring_transactions(user_id);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at) WHERE NOT revoked;
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

- [ ] **Step 13: Commit**

```bash
git add src/main/resources/db/
git commit -m "feat: add flyway migrations for all entities"
```

---

### Task 3: Domínio e Infraestrutura — módulo `auth` e `user`

**Files:**
- Create: `src/main/java/com/financafacil/domain/model/User.java`
- Create: `src/main/java/com/financafacil/domain/exception/DomainException.java`
- Create: `src/main/java/com/financafacil/domain/exception/NotFoundException.java`
- Create: `src/main/java/com/financafacil/domain/exception/ConflictException.java`
- Create: `src/main/java/com/financafacil/domain/exception/UnauthorizedException.java`
- Create: `src/main/java/com/financafacil/domain/port/out/UserRepository.java`
- Create: `src/main/java/com/financafacil/domain/port/out/RefreshTokenRepository.java`
- Create: `src/main/java/com/financafacil/domain/port/out/PasswordResetTokenRepository.java`
- Create: `src/main/java/com/financafacil/domain/port/out/EmailVerificationTokenRepository.java`
- Create: `src/main/java/com/financafacil/domain/port/out/AuditLogRepository.java`
- Create: `src/main/java/com/financafacil/domain/port/out/EmailService.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/UserJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/RefreshTokenJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/PasswordResetTokenJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/EmailVerificationTokenJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/AuditLogJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/UserJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/RefreshTokenJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/PasswordResetTokenJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/EmailVerificationTokenJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/AuditLogJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/UserRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/RefreshTokenRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/PasswordResetTokenRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/EmailVerificationTokenRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/AuditLogRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/mapper/UserJpaMapper.java`
- Create: `src/main/java/com/financafacil/infrastructure/email/EmailServiceAdapter.java`
- Create: `src/main/java/com/financafacil/infrastructure/config/AppProperties.java`

**Interfaces:**
- Produces:
  - `UserRepository`: `save(User): User`, `findByEmail(String): Optional<User>`, `findById(UUID): Optional<User>`, `existsByEmail(String): boolean`
  - `RefreshTokenRepository`: `save(RefreshToken): RefreshToken`, `findByTokenHash(String): Optional<RefreshToken>`, `revokeAllByUserId(UUID): void`
  - `PasswordResetTokenRepository`: `save(PasswordResetToken): PasswordResetToken`, `findByTokenHash(String): Optional<PasswordResetToken>`, `markAsUsed(UUID): void`
  - `EmailVerificationTokenRepository`: `save(EmailVerificationToken): EmailVerificationToken`, `findByTokenHash(String): Optional<EmailVerificationToken>`, `markAsUsed(UUID): void`
  - `EmailService`: `sendPasswordReset(String email, String resetLink): void`, `sendEmailVerification(String email, String verifyLink): void`

- [ ] **Step 1: Criar modelo de domínio `User.java`**

```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastLoginAt;
    private final boolean emailVerified;
    private final boolean active;
}
```

- [ ] **Step 2: Criar exceções de domínio**

`DomainException.java`:
```java
package com.financafacil.domain.exception;

public class DomainException extends RuntimeException {
    public DomainException(String message) { super(message); }
}
```

`NotFoundException.java`:
```java
package com.financafacil.domain.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String message) { super(message); }
}
```

`ConflictException.java`:
```java
package com.financafacil.domain.exception;

public class ConflictException extends DomainException {
    public ConflictException(String message) { super(message); }
}
```

`UnauthorizedException.java`:
```java
package com.financafacil.domain.exception;

public class UnauthorizedException extends DomainException {
    public UnauthorizedException(String message) { super(message); }
}
```

- [ ] **Step 3: Criar ports de saída do domínio**

`UserRepository.java`:
```java
package com.financafacil.domain.port.out;

import com.financafacil.domain.model.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    boolean existsByEmail(String email);
}
```

`RefreshTokenRepository.java`:
```java
package com.financafacil.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(UUID id, UUID userId, String tokenHash, Instant expiresAt);
    Optional<RefreshTokenData> findByTokenHash(String tokenHash);
    void revokeAllByUserId(UUID userId);
    void revokeById(UUID id);

    record RefreshTokenData(UUID id, UUID userId, Instant expiresAt, boolean revoked) {}
}
```

`PasswordResetTokenRepository.java`:
```java
package com.financafacil.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    void save(UUID id, UUID userId, String tokenHash, Instant expiresAt);
    Optional<TokenData> findByTokenHash(String tokenHash);
    void markAsUsed(UUID id);

    record TokenData(UUID id, UUID userId, Instant expiresAt, boolean used) {}
}
```

`EmailVerificationTokenRepository.java`:
```java
package com.financafacil.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository {
    void save(UUID id, UUID userId, String tokenHash, Instant expiresAt);
    Optional<TokenData> findByTokenHash(String tokenHash);
    void markAsUsed(UUID id);

    record TokenData(UUID id, UUID userId, Instant expiresAt, boolean used) {}
}
```

`AuditLogRepository.java`:
```java
package com.financafacil.domain.port.out;

import java.util.Map;
import java.util.UUID;

public interface AuditLogRepository {
    void log(UUID userId, String action, String entity, UUID entityId, Map<String, Object> metadata, String ip, String userAgent);
}
```

`EmailService.java`:
```java
package com.financafacil.domain.port.out;

public interface EmailService {
    void sendPasswordReset(String email, String resetLink);
    void sendEmailVerification(String email, String verifyLink);
}
```

- [ ] **Step 4: Criar `AppProperties.java`**

```java
package com.financafacil.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Mail mail = new Mail();
    private Frontend frontend = new Frontend();

    @Getter @Setter
    public static class Jwt {
        private String accessSecret;
        private String refreshSecret;
        private long accessExpirationMs;
        private long refreshExpirationMs;
    }

    @Getter @Setter
    public static class Cors {
        private String allowedOrigins;
    }

    @Getter @Setter
    public static class Mail {
        private String from;
    }

    @Getter @Setter
    public static class Frontend {
        private String url;
    }
}
```

- [ ] **Step 5: Criar JPA entities para `users`**

`UserJpaEntity.java`:
```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
```

- [ ] **Step 6: Criar JPA entities para tokens**

`RefreshTokenJpaEntity.java`:
```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RefreshTokenJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean revoked;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
```

`PasswordResetTokenJpaEntity.java`:
```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetTokenJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean used;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
```

`EmailVerificationTokenJpaEntity.java`:
```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_verification_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerificationTokenJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "token_hash", nullable = false) private String tokenHash;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(nullable = false) private boolean used;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
```

`AuditLogJpaEntity.java`:
```java
package com.financafacil.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    @Column(nullable = false, length = 100) private String action;
    @Column(length = 100) private String entity;
    @Column(name = "entity_id") private UUID entityId;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb") private Map<String, Object> metadata;
    @Column(length = 45) private String ip;
    @Column(name = "user_agent") private String userAgent;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
```

> **Nota:** Para `AuditLogJpaEntity` o JSONB requer a dependência `io.hypersistence:hypersistence-utils-hibernate-63`. Adicione ao `build.gradle.kts`:
> ```kotlin
> implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.8.3")
> ```

- [ ] **Step 7: Criar Spring Data JPA Repositories**

`UserJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

`RefreshTokenJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(UUID userId);
}
```

`PasswordResetTokenJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenJpaEntity, UUID> {
    Optional<PasswordResetTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE PasswordResetTokenJpaEntity t SET t.used = true WHERE t.id = :id")
    void markAsUsed(UUID id);
}
```

`EmailVerificationTokenJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenJpaEntity, UUID> {
    Optional<EmailVerificationTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("UPDATE EmailVerificationTokenJpaEntity t SET t.used = true WHERE t.id = :id")
    void markAsUsed(UUID id);
}
```

`AuditLogJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, UUID> {}
```

- [ ] **Step 8: Criar mapper JPA ↔ domínio para User**

`UserJpaMapper.java`:
```java
package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.User;
import com.financafacil.infrastructure.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserJpaMapper {
    UserJpaEntity toEntity(User user);
    User toDomain(UserJpaEntity entity);
}
```

- [ ] **Step 9: Criar adapters de repositório**

`UserRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.User;
import com.financafacil.domain.port.out.UserRepository;
import com.financafacil.infrastructure.persistence.jpa.UserJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.UserJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpaRepository;
    private final UserJpaMapper mapper;

    @Override public User save(User user) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(user))); }
    @Override public Optional<User> findByEmail(String email) { return jpaRepository.findByEmail(email).map(mapper::toDomain); }
    @Override public Optional<User> findById(UUID id) { return jpaRepository.findById(id).map(mapper::toDomain); }
    @Override public boolean existsByEmail(String email) { return jpaRepository.existsByEmail(email); }
}
```

`RefreshTokenRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.RefreshTokenRepository;
import com.financafacil.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
        jpaRepository.save(RefreshTokenJpaEntity.builder()
            .id(id).userId(userId).tokenHash(tokenHash)
            .expiresAt(expiresAt).revoked(false).createdAt(Instant.now()).build());
    }

    @Override
    public Optional<RefreshTokenData> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
            .map(e -> new RefreshTokenData(e.getId(), e.getUserId(), e.getExpiresAt(), e.isRevoked()));
    }

    @Override public void revokeAllByUserId(UUID userId) { jpaRepository.revokeAllByUserId(userId); }
    @Override public void revokeById(UUID id) { jpaRepository.findById(id).ifPresent(e -> { e.setRevoked(true); jpaRepository.save(e); }); }
}
```

`PasswordResetTokenRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.PasswordResetTokenRepository;
import com.financafacil.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {
    private final PasswordResetTokenJpaRepository jpaRepository;

    @Override
    public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
        jpaRepository.save(PasswordResetTokenJpaEntity.builder()
            .id(id).userId(userId).tokenHash(tokenHash)
            .expiresAt(expiresAt).used(false).createdAt(Instant.now()).build());
    }

    @Override
    public Optional<TokenData> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
            .map(e -> new TokenData(e.getId(), e.getUserId(), e.getExpiresAt(), e.isUsed()));
    }

    @Override @Transactional
    public void markAsUsed(UUID id) { jpaRepository.markAsUsed(id); }
}
```

`EmailVerificationTokenRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.EmailVerificationTokenRepository;
import com.financafacil.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.EmailVerificationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {
    private final EmailVerificationTokenJpaRepository jpaRepository;

    @Override
    public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
        jpaRepository.save(EmailVerificationTokenJpaEntity.builder()
            .id(id).userId(userId).tokenHash(tokenHash)
            .expiresAt(expiresAt).used(false).createdAt(Instant.now()).build());
    }

    @Override
    public Optional<TokenData> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
            .map(e -> new TokenData(e.getId(), e.getUserId(), e.getExpiresAt(), e.isUsed()));
    }

    @Override @Transactional
    public void markAsUsed(UUID id) { jpaRepository.markAsUsed(id); }
}
```

`AuditLogRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.AuditLogRepository;
import com.financafacil.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {
    private final AuditLogJpaRepository jpaRepository;

    @Override
    public void log(UUID userId, String action, String entity, UUID entityId, Map<String, Object> metadata, String ip, String userAgent) {
        jpaRepository.save(AuditLogJpaEntity.builder()
            .id(UUID.randomUUID()).userId(userId).action(action)
            .entity(entity).entityId(entityId).metadata(metadata)
            .ip(ip).userAgent(userAgent).createdAt(Instant.now()).build());
    }
}
```

- [ ] **Step 10: Criar `EmailServiceAdapter.java`**

```java
package com.financafacil.infrastructure.email;

import com.financafacil.domain.port.out.EmailService;
import com.financafacil.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailService {
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    public void sendPasswordReset(String email, String resetLink) {
        var message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(email);
        message.setSubject("Redefinição de senha — Finança Fácil");
        message.setText("Clique no link para redefinir sua senha: " + resetLink + "\n\nO link expira em 1 hora.");
        mailSender.send(message);
        log.info("Password reset email sent to {}", email);
    }

    @Override
    public void sendEmailVerification(String email, String verifyLink) {
        var message = new SimpleMailMessage();
        message.setFrom(appProperties.getMail().getFrom());
        message.setTo(email);
        message.setSubject("Verifique seu e-mail — Finança Fácil");
        message.setText("Clique no link para verificar seu e-mail: " + verifyLink + "\n\nO link expira em 24 horas.");
        mailSender.send(message);
        log.info("Verification email sent to {}", email);
    }
}
```

- [ ] **Step 11: Verificar compilação**

```bash
./gradlew compileJava -x test
```

Esperado: `BUILD SUCCESSFUL`

- [ ] **Step 12: Commit**

```bash
git add src/
git commit -m "feat: add domain models, ports, jpa entities and adapters for auth module"
```

---

### Task 4: Segurança — JWT, Spring Security, Filtros

**Files:**
- Create: `src/main/java/com/financafacil/infrastructure/security/jwt/JwtService.java`
- Create: `src/main/java/com/financafacil/infrastructure/security/jwt/JwtAuthenticationFilter.java`
- Create: `src/main/java/com/financafacil/infrastructure/security/userdetails/UserDetailsServiceImpl.java`
- Create: `src/main/java/com/financafacil/infrastructure/security/config/SecurityConfig.java`
- Create: `src/main/java/com/financafacil/presentation/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/ApiResponse.java`
- Create: `src/test/java/com/financafacil/infrastructure/security/jwt/JwtServiceTest.java`

**Interfaces:**
- Consumes: `AppProperties` (Task 3), `UserRepository` (Task 3)
- Produces:
  - `JwtService.generateAccessToken(UUID userId, String email): String`
  - `JwtService.generateRefreshToken(): String`
  - `JwtService.extractUserId(String token): UUID`
  - `JwtService.isAccessTokenValid(String token): boolean`
  - `ApiResponse<T>` com campos `success`, `data`, `message`, `error`, `timestamp`

- [ ] **Step 1: Escrever testes para `JwtService`**

```java
// src/test/java/com/financafacil/infrastructure/security/jwt/JwtServiceTest.java
package com.financafacil.infrastructure.security.jwt;

import com.financafacil.infrastructure.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var props = new AppProperties();
        props.getJwt().setAccessSecret("test-access-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setRefreshSecret("test-refresh-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setAccessExpirationMs(900000L);
        props.getJwt().setRefreshExpirationMs(604800000L);
        jwtService = new JwtService(props);
    }

    @Test
    void generateAccessToken_returnsValidJwt() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "user@test.com");
        assertThat(token).isNotBlank();
        assertThat(jwtService.isAccessTokenValid(token)).isTrue();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "user@test.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void isAccessTokenValid_returnsFalseForTamperedToken() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "user@test.com") + "tampered";
        assertThat(jwtService.isAccessTokenValid(token)).isFalse();
    }

    @Test
    void generateRefreshToken_returnsNonBlankUuid() {
        var token = jwtService.generateRefreshToken();
        assertThat(token).isNotBlank();
        assertThatCode(() -> UUID.fromString(token)).doesNotThrowAnyException();
    }
}
```

- [ ] **Step 2: Executar teste — verificar falha**

```bash
./gradlew test --tests "com.financafacil.infrastructure.security.jwt.JwtServiceTest" 2>&1 | tail -20
```

Esperado: FAIL — `JwtService` não existe ainda.

- [ ] **Step 3: Criar `JwtService.java`**

```java
package com.financafacil.infrastructure.security.jwt;

import com.financafacil.infrastructure.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessExpirationMs;

    public JwtService(AppProperties props) {
        this.accessKey = Keys.hmacShaKeyFor(props.getJwt().getAccessSecret().getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(props.getJwt().getRefreshSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = props.getJwt().getAccessExpirationMs();
    }

    public String generateAccessToken(UUID userId, String email) {
        var now = Instant.now();
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(accessExpirationMs)))
            .signWith(accessKey)
            .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean isAccessTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token).getPayload();
    }
}
```

- [ ] **Step 4: Executar testes — verificar sucesso**

```bash
./gradlew test --tests "com.financafacil.infrastructure.security.jwt.JwtServiceTest"
```

Esperado: `BUILD SUCCESSFUL`, 4 testes passando.

- [ ] **Step 5: Criar `ApiResponse.java`**

```java
package com.financafacil.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final ErrorDetail error;
    private final Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).message("OK").timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder().success(false)
            .error(new ErrorDetail(code, message)).timestamp(Instant.now()).build();
    }

    public record ErrorDetail(String code, String message) {}
}
```

- [ ] **Step 6: Criar `GlobalExceptionHandler.java`**

```java
package com.financafacil.presentation.exception;

import com.financafacil.domain.exception.*;
import com.financafacil.presentation.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(NotFoundException ex) {
        return ApiResponse.error("NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleConflict(ConflictException ex) {
        return ApiResponse.error("CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleUnauthorized(UnauthorizedException ex) {
        return ApiResponse.error("UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        return ApiResponse.error("VALIDATION_ERROR", details);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleForbidden(AccessDeniedException ex) {
        return ApiResponse.error("FORBIDDEN", "Acesso negado");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiResponse.error("INTERNAL_ERROR", "Erro interno do servidor");
    }
}
```

- [ ] **Step 7: Criar `UserDetailsServiceImpl.java`**

```java
package com.financafacil.infrastructure.security.userdetails;

import com.financafacil.domain.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .filter(u -> u.isActive())
            .map(u -> new org.springframework.security.core.userdetails.User(
                u.getId().toString(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
```

- [ ] **Step 8: Criar `JwtAuthenticationFilter.java`**

```java
package com.financafacil.infrastructure.security.jwt;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        var token = header.substring(7);
        if (jwtService.isAccessTokenValid(token)) {
            var userId = jwtService.extractUserId(token);
            var auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 9: Criar `SecurityConfig.java`**

```java
package com.financafacil.infrastructure.security.config;

import com.financafacil.infrastructure.config.AppProperties;
import com.financafacil.infrastructure.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AppProperties appProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/register", "/auth/login", "/auth/refresh",
                    "/auth/forgot-password", "/auth/reset-password", "/auth/verify-email").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(h -> h
                .frameOptions(f -> f.deny())
                .contentTypeOptions(c -> {})
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(appProperties.getCors().getAllowedOrigins().split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

- [ ] **Step 10: Executar compilação**

```bash
./gradlew compileJava -x test
```

Esperado: `BUILD SUCCESSFUL`

- [ ] **Step 11: Commit**

```bash
git add src/
git commit -m "feat: add jwt service, spring security config and global exception handler"
```

---

### Task 5: Use Cases de Autenticação + Controller

**Files:**
- Create: `src/main/java/com/financafacil/domain/port/in/AuthUseCase.java`
- Create: `src/main/java/com/financafacil/application/usecase/AuthUseCaseImpl.java`
- Create: `src/main/java/com/financafacil/presentation/controller/AuthController.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/RegisterRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/LoginRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/ForgotPasswordRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/ResetPasswordRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/VerifyEmailRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/AuthResponse.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/UserResponse.java`
- Create: `src/test/java/com/financafacil/application/usecase/AuthUseCaseImplTest.java`
- Create: `src/test/java/com/financafacil/presentation/controller/AuthControllerIntegrationTest.java`

**Interfaces:**
- Consumes: `UserRepository`, `RefreshTokenRepository`, `PasswordResetTokenRepository`, `EmailVerificationTokenRepository`, `AuditLogRepository`, `EmailService`, `JwtService`, `PasswordEncoder`
- Produces:
  - `AuthUseCase.register(name, email, password): AuthResponse`
  - `AuthUseCase.login(email, password, ip, userAgent): AuthResponse`
  - `AuthUseCase.refresh(rawRefreshToken): AuthResponse`
  - `AuthUseCase.logout(rawRefreshToken): void`
  - `AuthUseCase.forgotPassword(email): void`
  - `AuthUseCase.resetPassword(token, newPassword): void`
  - `AuthUseCase.verifyEmail(token): void`
  - `AuthUseCase.me(userId): UserResponse`

- [ ] **Step 1: Criar DTOs de request e response**

`RegisterRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 2, max = 150)
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 8, max = 100)
    private String password;
}
```

`LoginRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank @Email private String email;
    @NotBlank private String password;
}
```

`ForgotPasswordRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank @Email private String email;
}
```

`ResetPasswordRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank private String token;
    @NotBlank @Size(min = 8, max = 100) private String newPassword;
}
```

`VerifyEmailRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank private String token;
}
```

`AuthResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
    private final UserResponse user;

    public static AuthResponse of(String accessToken, long expiresIn, UserResponse user) {
        return AuthResponse.builder().accessToken(accessToken).tokenType("Bearer")
            .expiresIn(expiresIn).user(user).build();
    }
}
```

`UserResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private final UUID id;
    private final String name;
    private final String email;
    private final boolean emailVerified;
}
```

- [ ] **Step 2: Criar port de entrada `AuthUseCase.java`**

```java
package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.AuthResponse;
import com.financafacil.presentation.dto.response.UserResponse;

public interface AuthUseCase {
    AuthResponse register(String name, String email, String password);
    AuthResponse login(String email, String password, String ip, String userAgent);
    AuthResponse refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void verifyEmail(String token);
    UserResponse me(java.util.UUID userId);
}
```

- [ ] **Step 3: Escrever testes unitários para `AuthUseCaseImpl`**

```java
// src/test/java/com/financafacil/application/usecase/AuthUseCaseImplTest.java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.exception.UnauthorizedException;
import com.financafacil.domain.model.User;
import com.financafacil.domain.port.out.*;
import com.financafacil.infrastructure.config.AppProperties;
import com.financafacil.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock EmailService emailService;

    private AuthUseCaseImpl authUseCase;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        var props = new AppProperties();
        props.getJwt().setAccessSecret("test-access-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setRefreshSecret("test-refresh-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setAccessExpirationMs(900000L);
        props.getJwt().setRefreshExpirationMs(604800000L);
        props.getFrontend().setUrl("http://localhost:5173");
        jwtService = new JwtService(props);
        authUseCase = new AuthUseCaseImpl(userRepository, refreshTokenRepository,
            passwordResetTokenRepository, emailVerificationTokenRepository,
            auditLogRepository, emailService, jwtService, passwordEncoder, props);
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
        assertThatThrownBy(() -> authUseCase.register("Name", "existing@test.com", "password123"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void register_savesUser_andSendsVerificationEmail() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        var savedUser = User.builder().id(UUID.randomUUID()).name("Name").email("new@test.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .emailVerified(false).active(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.save(any())).thenReturn(savedUser);

        var result = authUseCase.register("Name", "new@test.com", "password123");

        assertThat(result.getAccessToken()).isNotBlank();
        verify(emailVerificationTokenRepository).save(any(), eq(savedUser.getId()), any(), any());
        verify(emailService).sendEmailVerification(eq("new@test.com"), any());
    }

    @Test
    void login_throwsUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authUseCase.login("notfound@test.com", "pass", "127.0.0.1", "agent"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_throwsUnauthorized_whenPasswordWrong() {
        var user = User.builder().id(UUID.randomUUID()).email("user@test.com")
            .passwordHash(passwordEncoder.encode("correct")).active(true)
            .emailVerified(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authUseCase.login("user@test.com", "wrong", "127.0.0.1", "agent"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void forgotPassword_doesNotThrow_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        assertThatCode(() -> authUseCase.forgotPassword("ghost@test.com")).doesNotThrowAnyException();
        verify(emailService, never()).sendPasswordReset(any(), any());
    }
}
```

- [ ] **Step 4: Executar testes — verificar falha**

```bash
./gradlew test --tests "com.financafacil.application.usecase.AuthUseCaseImplTest" 2>&1 | tail -10
```

Esperado: FAIL — `AuthUseCaseImpl` não existe.

- [ ] **Step 5: Criar `AuthUseCaseImpl.java`**

```java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.exception.UnauthorizedException;
import com.financafacil.domain.model.User;
import com.financafacil.domain.port.in.AuthUseCase;
import com.financafacil.domain.port.out.*;
import com.financafacil.infrastructure.config.AppProperties;
import com.financafacil.infrastructure.security.jwt.JwtService;
import com.financafacil.presentation.dto.response.AuthResponse;
import com.financafacil.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public AuthResponse register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        var user = userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name(name)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .emailVerified(false)
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());

        issueEmailVerificationToken(user);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(String email, String password, String ip, String userAgent) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
        if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }
        auditLogRepository.log(user.getId(), "LOGIN", "users", user.getId(), Map.of(), ip, userAgent);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        var tokenHash = hashToken(rawRefreshToken);
        var tokenData = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        if (tokenData.revoked() || tokenData.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expirado ou revogado");
        }
        refreshTokenRepository.revokeById(tokenData.id());
        var user = userRepository.findById(tokenData.userId())
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        var tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
            .ifPresent(t -> refreshTokenRepository.revokeById(t.id()));
    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            var rawToken = UUID.randomUUID().toString();
            var tokenHash = hashToken(rawToken);
            passwordResetTokenRepository.save(UUID.randomUUID(), user.getId(), tokenHash,
                Instant.now().plusSeconds(3600));
            var link = appProperties.getFrontend().getUrl() + "/reset-password?token=" + rawToken;
            emailService.sendPasswordReset(email, link);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        var tokenHash = hashToken(token);
        var tokenData = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Token inválido"));
        if (tokenData.used() || tokenData.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Token expirado ou já utilizado");
        }
        var user = userRepository.findById(tokenData.userId())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.save(user.withPasswordHash(passwordEncoder.encode(newPassword)));
        passwordResetTokenRepository.markAsUsed(tokenData.id());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        var tokenHash = hashToken(token);
        var tokenData = emailVerificationTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Token inválido"));
        if (tokenData.used() || tokenData.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Token expirado ou já utilizado");
        }
        var user = userRepository.findById(tokenData.userId())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.save(user.withEmailVerified(true));
        emailVerificationTokenRepository.markAsUsed(tokenData.id());
    }

    @Override
    public UserResponse me(UUID userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        var accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        var rawRefresh = jwtService.generateRefreshToken();
        refreshTokenRepository.save(UUID.randomUUID(), user.getId(), hashToken(rawRefresh),
            Instant.now().plusMillis(appProperties.getJwt().getRefreshExpirationMs()));
        return AuthResponse.of(accessToken, appProperties.getJwt().getAccessExpirationMs() / 1000, toUserResponse(user));
    }

    private void issueEmailVerificationToken(User user) {
        var rawToken = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(UUID.randomUUID(), user.getId(), hashToken(rawToken),
            Instant.now().plusSeconds(86400));
        var link = appProperties.getFrontend().getUrl() + "/verify-email?token=" + rawToken;
        emailService.sendEmailVerification(user.getEmail(), link);
    }

    private String hashToken(String rawToken) {
        return org.springframework.util.DigestUtils.md5DigestAsHex(rawToken.getBytes());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder().id(user.getId()).name(user.getName())
            .email(user.getEmail()).emailVerified(user.isEmailVerified()).build();
    }
}
```

- [ ] **Step 6: Executar testes unitários**

```bash
./gradlew test --tests "com.financafacil.application.usecase.AuthUseCaseImplTest"
```

Esperado: `BUILD SUCCESSFUL`, 5 testes passando.

- [ ] **Step 7: Criar `AuthController.java`**

```java
package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.AuthUseCase;
import com.financafacil.presentation.dto.request.*;
import com.financafacil.presentation.dto.response.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authUseCase.register(req.getName(), req.getEmail(), req.getPassword()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpReq) {
        return ApiResponse.ok(authUseCase.login(req.getEmail(), req.getPassword(),
            httpReq.getRemoteAddr(), httpReq.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody java.util.Map<String, String> body) {
        return ApiResponse.ok(authUseCase.refresh(body.get("refreshToken")));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody java.util.Map<String, String> body) {
        authUseCase.logout(body.get("refreshToken"));
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authUseCase.forgotPassword(req.getEmail());
        return ApiResponse.ok(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authUseCase.resetPassword(req.getToken(), req.getNewPassword());
        return ApiResponse.ok(null);
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        authUseCase.verifyEmail(req.getToken());
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal Object principal) {
        var userId = UUID.fromString(principal.toString());
        return ApiResponse.ok(authUseCase.me(userId));
    }
}
```

- [ ] **Step 8: Criar teste de integração para Auth**

```java
// src/test/java/com/financafacil/presentation/controller/AuthControllerIntegrationTest.java
package com.financafacil.presentation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class AuthControllerIntegrationTest {

    @LocalServerPort int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
    }

    @Test
    void register_returnsCreated_withAccessToken() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"name":"Test User","email":"test@integration.com","password":"password123"}
                """)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.accessToken", notNullValue())
            .body("data.user.email", equalTo("test@integration.com"));
    }

    @Test
    void register_returnsConflict_whenEmailAlreadyExists() {
        var body = """
            {"name":"User","email":"duplicate@test.com","password":"password123"}
            """;
        given().contentType(ContentType.JSON).body(body).post("/auth/register");

        given()
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/auth/register")
        .then()
            .statusCode(409)
            .body("success", equalTo(false));
    }

    @Test
    void login_returnsUnauthorized_withWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"email":"notexist@test.com","password":"wrong"}
                """)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401);
    }

    @Test
    void me_returnsUnauthorized_withoutToken() {
        given().get("/auth/me").then().statusCode(403);
    }
}
```

- [ ] **Step 9: Executar testes de integração**

```bash
./gradlew test --tests "com.financafacil.presentation.controller.AuthControllerIntegrationTest"
```

Esperado: `BUILD SUCCESSFUL`, 4 testes passando. (Testcontainers sobe PostgreSQL automaticamente via `jdbc:tc:` URL)

- [ ] **Step 10: Commit**

```bash
git add src/
git commit -m "feat: implement auth use case and controller with integration tests"
```
