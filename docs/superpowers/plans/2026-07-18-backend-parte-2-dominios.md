# Backend Finança Fácil — Plano Parte 2: Domínios de Negócio (Accounts, Categories, Transactions, Dashboard)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar os módulos de Accounts, Categories, Transactions e Dashboard com CRUD completo, filtros, paginação, atualização transacional de saldo e endpoints de agregação para o Dashboard.

**Architecture:** Clean Architecture — cada módulo tem domain model, ports (in/out), use case (application), JPA entity + adapter (infrastructure) e controller + DTOs (presentation). O saldo de Account é desnormalizado e atualizado via `@Transactional` a cada mutação de Transaction.

**Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, MapStruct, Lombok, Bean Validation, JUnit 5, Mockito, Testcontainers, REST Assured.

## Global Constraints

- Todos os IDs são UUID gerados pela aplicação
- Nunca expor JPA entities — sempre DTOs via MapStruct
- Todo endpoint extrai `userId` do `SecurityContext` (nunca do body)
- Saldo de conta (`balance`) atualizado atomicamente em toda mutação de transação
- Respostas no envelope `ApiResponse<T>` (definido na Parte 1)
- Base package: `com.financafacil`
- `@Transactional` em todo método de use case que escreve no banco
- Cobertura JaCoCo mínima: 70% global, 80% em `domain` e `application`

---

### Task 6: Módulo Account — domínio, infra e controller

**Files:**
- Create: `src/main/java/com/financafacil/domain/model/Account.java`
- Create: `src/main/java/com/financafacil/domain/port/in/AccountUseCase.java`
- Create: `src/main/java/com/financafacil/domain/port/out/AccountRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/AccountJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/AccountJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/mapper/AccountJpaMapper.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/AccountRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/application/usecase/AccountUseCaseImpl.java`
- Create: `src/main/java/com/financafacil/presentation/controller/AccountController.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/CreateAccountRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/UpdateAccountRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/AccountResponse.java`
- Create: `src/main/java/com/financafacil/presentation/mapper/AccountPresentationMapper.java`
- Create: `src/test/java/com/financafacil/application/usecase/AccountUseCaseImplTest.java`
- Create: `src/test/java/com/financafacil/presentation/controller/AccountControllerIntegrationTest.java`

**Interfaces:**
- Consumes: `SecurityContext` (userId do JWT, Task 4)
- Produces:
  - `AccountRepository.save(Account): Account`
  - `AccountRepository.findById(UUID, UUID): Optional<Account>` (id, userId)
  - `AccountRepository.findAllByUserId(UUID): List<Account>`
  - `AccountRepository.deleteById(UUID, UUID): void`
  - `AccountRepository.updateBalance(UUID, BigDecimal): void`
  - `AccountUseCase.create(userId, name, type, initialBalance, color): AccountResponse`
  - `AccountUseCase.findAll(userId): List<AccountResponse>`
  - `AccountUseCase.update(userId, accountId, name, type, color): AccountResponse`
  - `AccountUseCase.delete(userId, accountId): void`
  - `AccountUseCase.getBalance(userId, accountId): BigDecimal`

- [ ] **Step 1: Criar `Account.java` (domain model)**

```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class Account {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String type;
    private final BigDecimal balance;
    private final String color;
    private final Instant createdAt;
    private final Instant updatedAt;
}
```

- [ ] **Step 2: Criar `AccountRepository.java` (port out)**

```java
package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Account;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id, UUID userId);
    List<Account> findAllByUserId(UUID userId);
    void deleteById(UUID id, UUID userId);
    void updateBalance(UUID accountId, BigDecimal newBalance);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    boolean hasTransactions(UUID accountId);
}
```

- [ ] **Step 3: Criar `AccountUseCase.java` (port in)**

```java
package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.AccountResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountUseCase {
    AccountResponse create(UUID userId, String name, String type, BigDecimal initialBalance, String color);
    List<AccountResponse> findAll(UUID userId);
    AccountResponse update(UUID userId, UUID accountId, String name, String type, String color);
    void delete(UUID userId, UUID accountId);
    BigDecimal getBalance(UUID userId, UUID accountId);
}
```

- [ ] **Step 4: Criar `AccountJpaEntity.java`**

```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AccountJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 20) private String type;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal balance;
    @Column(nullable = false, length = 7) private String color;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
```

- [ ] **Step 5: Criar `AccountJpaRepository.java`**

```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    Optional<AccountJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<AccountJpaEntity> findAllByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE AccountJpaEntity a SET a.balance = :balance WHERE a.id = :accountId")
    void updateBalance(UUID accountId, BigDecimal balance);

    @Query("SELECT COUNT(t) > 0 FROM TransactionJpaEntity t WHERE t.accountId = :accountId")
    boolean hasTransactions(UUID accountId);
}
```

- [ ] **Step 6: Criar `AccountJpaMapper.java`**

```java
package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Account;
import com.financafacil.infrastructure.persistence.entity.AccountJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountJpaMapper {
    AccountJpaEntity toEntity(Account account);
    Account toDomain(AccountJpaEntity entity);
}
```

- [ ] **Step 7: Criar `AccountRepositoryAdapter.java`**

```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Account;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.infrastructure.persistence.jpa.AccountJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.AccountJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository jpaRepository;
    private final AccountJpaMapper mapper;

    @Override public Account save(Account a) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(a))); }
    @Override public Optional<Account> findById(UUID id, UUID userId) { return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain); }
    @Override public List<Account> findAllByUserId(UUID userId) { return jpaRepository.findAllByUserId(userId).stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public void deleteById(UUID id, UUID userId) { jpaRepository.findByIdAndUserId(id, userId).ifPresent(jpaRepository::delete); }
    @Override @Transactional public void updateBalance(UUID accountId, BigDecimal newBalance) { jpaRepository.updateBalance(accountId, newBalance); }
    @Override public boolean existsByIdAndUserId(UUID id, UUID userId) { return jpaRepository.existsByIdAndUserId(id, userId); }
    @Override public boolean hasTransactions(UUID accountId) { return jpaRepository.hasTransactions(accountId); }
}
```

- [ ] **Step 8: Criar `AccountResponse.java` e `AccountPresentationMapper.java`**

`AccountResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder
public class AccountResponse {
    private final UUID id;
    private final String name;
    private final String type;
    private final BigDecimal balance;
    private final String color;
    private final Instant createdAt;
}
```

`AccountPresentationMapper.java`:
```java
package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Account;
import com.financafacil.presentation.dto.response.AccountResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountPresentationMapper {
    AccountResponse toResponse(Account account);
}
```

- [ ] **Step 9: Criar DTOs de request**

`CreateAccountRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotBlank @Size(max = 150) private String name;
    @NotBlank @Pattern(regexp = "checking|wallet|credit_card|investment") private String type;
    @NotNull private BigDecimal initialBalance;
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
}
```

`UpdateAccountRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateAccountRequest {
    @NotBlank @Size(max = 150) private String name;
    @NotBlank @Pattern(regexp = "checking|wallet|credit_card|investment") private String type;
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
}
```

- [ ] **Step 10: Escrever testes unitários para `AccountUseCaseImpl`**

```java
// src/test/java/com/financafacil/application/usecase/AccountUseCaseImplTest.java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.presentation.mapper.AccountPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    @Mock AccountPresentationMapper presentationMapper;
    @InjectMocks AccountUseCaseImpl accountUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_savesAccountWithInitialBalance() {
        var account = buildAccount(BigDecimal.valueOf(1000));
        when(accountRepository.save(any())).thenReturn(account);
        when(presentationMapper.toResponse(account)).thenCallRealMethod();

        accountUseCase.create(userId, "Nubank", "checking", BigDecimal.valueOf(1000), "#8B5CF6");

        var captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void delete_throwsConflict_whenAccountHasTransactions() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(accountRepository.hasTransactions(accountId)).thenReturn(true);

        assertThatThrownBy(() -> accountUseCase.delete(userId, accountId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("transações");
    }

    @Test
    void delete_throwsNotFound_whenAccountBelongsToOtherUser() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(false);

        assertThatThrownBy(() -> accountUseCase.delete(userId, accountId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_returnsListForUser() {
        var account = buildAccount(BigDecimal.ZERO);
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of(account));
        when(presentationMapper.toResponse(account)).thenCallRealMethod();

        var result = accountUseCase.findAll(userId);
        assertThat(result).hasSize(1);
    }

    private Account buildAccount(BigDecimal balance) {
        return Account.builder().id(UUID.randomUUID()).userId(userId)
            .name("Test").type("checking").balance(balance).color("#000000")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
```

- [ ] **Step 11: Executar testes — verificar falha**

```bash
./gradlew test --tests "com.financafacil.application.usecase.AccountUseCaseImplTest" 2>&1 | tail -10
```

Esperado: FAIL — `AccountUseCaseImpl` não existe.

- [ ] **Step 12: Criar `AccountUseCaseImpl.java`**

```java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.port.in.AccountUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.presentation.dto.response.AccountResponse;
import com.financafacil.presentation.mapper.AccountPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountUseCaseImpl implements AccountUseCase {
    private final AccountRepository accountRepository;
    private final AccountPresentationMapper presentationMapper;

    @Override
    @Transactional
    public AccountResponse create(UUID userId, String name, String type, BigDecimal initialBalance, String color) {
        var account = accountRepository.save(Account.builder()
            .id(UUID.randomUUID()).userId(userId).name(name).type(type)
            .balance(initialBalance).color(color)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        return presentationMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> findAll(UUID userId) {
        return accountRepository.findAllByUserId(userId).stream()
            .map(presentationMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, String name, String type, String color) {
        var account = accountRepository.findById(accountId, userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
        var updated = accountRepository.save(account.withName(name).withType(type).withColor(color)
            .withUpdatedAt(Instant.now()));
        return presentationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID accountId) {
        if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
            throw new NotFoundException("Conta não encontrada");
        }
        if (accountRepository.hasTransactions(accountId)) {
            throw new ConflictException("Não é possível remover uma conta com transações");
        }
        accountRepository.deleteById(accountId, userId);
    }

    @Override
    public BigDecimal getBalance(UUID userId, UUID accountId) {
        return accountRepository.findById(accountId, userId)
            .map(Account::getBalance)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
    }
}
```

- [ ] **Step 13: Criar `AccountController.java`**

```java
package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.AccountUseCase;
import com.financafacil.presentation.dto.request.*;
import com.financafacil.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountUseCase accountUseCase;

    @GetMapping
    public ApiResponse<List<AccountResponse>> findAll(@AuthenticationPrincipal Object principal) {
        return ApiResponse.ok(accountUseCase.findAll(toUuid(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> create(@AuthenticationPrincipal Object principal,
                                               @Valid @RequestBody CreateAccountRequest req) {
        return ApiResponse.ok(accountUseCase.create(toUuid(principal), req.getName(),
            req.getType(), req.getInitialBalance(), req.getColor()));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountResponse> update(@AuthenticationPrincipal Object principal,
                                               @PathVariable UUID id,
                                               @Valid @RequestBody UpdateAccountRequest req) {
        return ApiResponse.ok(accountUseCase.update(toUuid(principal), id, req.getName(), req.getType(), req.getColor()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        accountUseCase.delete(toUuid(principal), id);
    }

    @GetMapping("/{id}/balance")
    public ApiResponse<java.math.BigDecimal> getBalance(@AuthenticationPrincipal Object principal,
                                                        @PathVariable UUID id) {
        return ApiResponse.ok(accountUseCase.getBalance(toUuid(principal), id));
    }

    private UUID toUuid(Object principal) { return UUID.fromString(principal.toString()); }
}
```

- [ ] **Step 14: Criar teste de integração para Account**

```java
// src/test/java/com/financafacil/presentation/controller/AccountControllerIntegrationTest.java
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
class AccountControllerIntegrationTest {

    @LocalServerPort int port;
    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        // Registrar e obter token
        accessToken = given().contentType(ContentType.JSON)
            .body("""
                {"name":"Account Test User","email":"accounttest@test.com","password":"password123"}
                """)
            .post("/auth/register")
            .jsonPath().getString("data.accessToken");
    }

    @Test
    void createAndListAccounts() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("""
                {"name":"Nubank","type":"checking","initialBalance":1000.00,"color":"#8B5CF6"}
                """)
        .when()
            .post("/accounts")
        .then()
            .statusCode(201)
            .body("data.name", equalTo("Nubank"))
            .body("data.balance", equalTo(1000.0f));

        given()
            .header("Authorization", "Bearer " + accessToken)
        .when()
            .get("/accounts")
        .then()
            .statusCode(200)
            .body("data", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void deleteAccount_returns404_whenNotOwner() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .delete("/accounts/" + java.util.UUID.randomUUID())
        .then()
            .statusCode(404);
    }
}
```

- [ ] **Step 15: Executar testes**

```bash
./gradlew test --tests "com.financafacil.application.usecase.AccountUseCaseImplTest"
./gradlew test --tests "com.financafacil.presentation.controller.AccountControllerIntegrationTest"
```

Esperado: `BUILD SUCCESSFUL`, todos os testes passando.

- [ ] **Step 16: Commit**

```bash
git add src/
git commit -m "feat: implement account module with crud and balance management"
```

---

### Task 7: Módulo Category — domínio, infra e controller

**Files:**
- Create: `src/main/java/com/financafacil/domain/model/Category.java`
- Create: `src/main/java/com/financafacil/domain/port/in/CategoryUseCase.java`
- Create: `src/main/java/com/financafacil/domain/port/out/CategoryRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/CategoryJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/CategoryJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/mapper/CategoryJpaMapper.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/CategoryRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/application/usecase/CategoryUseCaseImpl.java`
- Create: `src/main/java/com/financafacil/presentation/controller/CategoryController.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/CreateCategoryRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/UpdateCategoryRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/CategoryResponse.java`
- Create: `src/main/java/com/financafacil/presentation/mapper/CategoryPresentationMapper.java`
- Create: `src/test/java/com/financafacil/application/usecase/CategoryUseCaseImplTest.java`

**Interfaces:**
- Produces:
  - `CategoryRepository.save(Category): Category`
  - `CategoryRepository.findById(UUID, UUID): Optional<Category>`
  - `CategoryRepository.findAllByUserId(UUID): List<Category>`
  - `CategoryRepository.findAllByUserIdAndType(UUID, String): List<Category>`
  - `CategoryRepository.deleteById(UUID, UUID): void`
  - `CategoryRepository.existsByIdAndUserId(UUID, UUID): boolean`

- [ ] **Step 1: Criar `Category.java` (domain model)**

```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @With
public class Category {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String type;
    private final String icon;
    private final String color;
    private final Instant createdAt;
    private final Instant updatedAt;
}
```

- [ ] **Step 2: Criar port out, JPA entity, JPA repository, mapper e adapter**

`CategoryRepository.java`:
```java
package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id, UUID userId);
    List<Category> findAllByUserId(UUID userId);
    List<Category> findAllByUserIdAndType(UUID userId, String type);
    void deleteById(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
```

`CategoryJpaEntity.java`:
```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(nullable = false, length = 100) private String name;
    @Column(nullable = false, length = 10) private String type;
    @Column(length = 50) private String icon;
    @Column(nullable = false, length = 7) private String color;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
```

`CategoryJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
    Optional<CategoryJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<CategoryJpaEntity> findAllByUserId(UUID userId);
    List<CategoryJpaEntity> findAllByUserIdAndType(UUID userId, String type);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
```

`CategoryJpaMapper.java`:
```java
package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Category;
import com.financafacil.infrastructure.persistence.entity.CategoryJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryJpaMapper {
    CategoryJpaEntity toEntity(Category category);
    Category toDomain(CategoryJpaEntity entity);
}
```

`CategoryRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Category;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.infrastructure.persistence.jpa.CategoryJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.CategoryJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository jpaRepository;
    private final CategoryJpaMapper mapper;

    @Override public Category save(Category c) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(c))); }
    @Override public Optional<Category> findById(UUID id, UUID userId) { return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain); }
    @Override public List<Category> findAllByUserId(UUID userId) { return jpaRepository.findAllByUserId(userId).stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public List<Category> findAllByUserIdAndType(UUID userId, String type) { return jpaRepository.findAllByUserIdAndType(userId, type).stream().map(mapper::toDomain).collect(Collectors.toList()); }
    @Override public void deleteById(UUID id, UUID userId) { jpaRepository.findByIdAndUserId(id, userId).ifPresent(jpaRepository::delete); }
    @Override public boolean existsByIdAndUserId(UUID id, UUID userId) { return jpaRepository.existsByIdAndUserId(id, userId); }
}
```

- [ ] **Step 3: Criar DTOs de request/response e mapper de apresentação**

`CategoryResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter @Builder
public class CategoryResponse {
    private final UUID id;
    private final String name;
    private final String type;
    private final String icon;
    private final String color;
}
```

`CategoryPresentationMapper.java`:
```java
package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Category;
import com.financafacil.presentation.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryPresentationMapper {
    CategoryResponse toResponse(Category category);
}
```

`CreateCategoryRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    @NotBlank @Size(max = 100) private String name;
    @NotBlank @Pattern(regexp = "income|expense") private String type;
    @Size(max = 50) private String icon;
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
}
```

`UpdateCategoryRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateCategoryRequest {
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 50) private String icon;
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") private String color;
}
```

- [ ] **Step 4: Escrever testes e criar `CategoryUseCaseImpl.java`**

Testes unitários (`CategoryUseCaseImplTest.java`):
```java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Category;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.presentation.mapper.CategoryPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryUseCaseImplTest {
    @Mock CategoryRepository categoryRepository;
    @Mock CategoryPresentationMapper presentationMapper;
    @InjectMocks CategoryUseCaseImpl categoryUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void findAll_returnsAllForUser() {
        var cat = buildCategory();
        when(categoryRepository.findAllByUserId(userId)).thenReturn(List.of(cat));
        var result = categoryUseCase.findAll(userId, null);
        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_filtersByType_whenProvided() {
        var cat = buildCategory();
        when(categoryRepository.findAllByUserIdAndType(userId, "expense")).thenReturn(List.of(cat));
        categoryUseCase.findAll(userId, "expense");
        verify(categoryRepository).findAllByUserIdAndType(userId, "expense");
        verify(categoryRepository, never()).findAllByUserId(any());
    }

    @Test
    void delete_throwsNotFound_whenCategoryBelongsToOtherUser() {
        var catId = UUID.randomUUID();
        when(categoryRepository.existsByIdAndUserId(catId, userId)).thenReturn(false);
        assertThatThrownBy(() -> categoryUseCase.delete(userId, catId)).isInstanceOf(NotFoundException.class);
    }

    private Category buildCategory() {
        return Category.builder().id(UUID.randomUUID()).userId(userId).name("Test")
            .type("expense").icon("Star").color("#FF0000").createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
```

`CategoryUseCaseImpl.java`:
```java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Category;
import com.financafacil.domain.port.in.CategoryUseCase;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.presentation.dto.response.CategoryResponse;
import com.financafacil.presentation.mapper.CategoryPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryUseCaseImpl implements CategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryPresentationMapper presentationMapper;

    @Override @Transactional
    public CategoryResponse create(UUID userId, String name, String type, String icon, String color) {
        var cat = categoryRepository.save(Category.builder()
            .id(UUID.randomUUID()).userId(userId).name(name).type(type).icon(icon).color(color)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build());
        return presentationMapper.toResponse(cat);
    }

    @Override
    public List<CategoryResponse> findAll(UUID userId, String type) {
        var list = type != null
            ? categoryRepository.findAllByUserIdAndType(userId, type)
            : categoryRepository.findAllByUserId(userId);
        return list.stream().map(presentationMapper::toResponse).collect(Collectors.toList());
    }

    @Override @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, String name, String icon, String color) {
        var cat = categoryRepository.findById(categoryId, userId)
            .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
        var updated = categoryRepository.save(cat.withName(name).withIcon(icon).withColor(color).withUpdatedAt(Instant.now()));
        return presentationMapper.toResponse(updated);
    }

    @Override @Transactional
    public void delete(UUID userId, UUID categoryId) {
        if (!categoryRepository.existsByIdAndUserId(categoryId, userId)) {
            throw new NotFoundException("Categoria não encontrada");
        }
        categoryRepository.deleteById(categoryId, userId);
    }
}
```

`CategoryUseCase.java` (port in):
```java
package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.CategoryResponse;
import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {
    CategoryResponse create(UUID userId, String name, String type, String icon, String color);
    List<CategoryResponse> findAll(UUID userId, String type);
    CategoryResponse update(UUID userId, UUID categoryId, String name, String icon, String color);
    void delete(UUID userId, UUID categoryId);
}
```

`CategoryController.java`:
```java
package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.CategoryUseCase;
import com.financafacil.presentation.dto.request.*;
import com.financafacil.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryUseCase categoryUseCase;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> findAll(@AuthenticationPrincipal Object principal,
                                                       @RequestParam(required = false) String type) {
        return ApiResponse.ok(categoryUseCase.findAll(toUuid(principal), type));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> create(@AuthenticationPrincipal Object principal,
                                                @Valid @RequestBody CreateCategoryRequest req) {
        return ApiResponse.ok(categoryUseCase.create(toUuid(principal), req.getName(), req.getType(), req.getIcon(), req.getColor()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@AuthenticationPrincipal Object principal,
                                                @PathVariable UUID id,
                                                @Valid @RequestBody UpdateCategoryRequest req) {
        return ApiResponse.ok(categoryUseCase.update(toUuid(principal), id, req.getName(), req.getIcon(), req.getColor()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        categoryUseCase.delete(toUuid(principal), id);
    }

    private UUID toUuid(Object principal) { return UUID.fromString(principal.toString()); }
}
```

- [ ] **Step 5: Executar testes**

```bash
./gradlew test --tests "com.financafacil.application.usecase.CategoryUseCaseImplTest"
```

Esperado: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: implement category module with crud"
```

---

### Task 8: Módulo Transaction — domínio, infra, Specifications e controller

**Files:**
- Create: `src/main/java/com/financafacil/domain/model/Transaction.java`
- Create: `src/main/java/com/financafacil/domain/port/in/TransactionUseCase.java`
- Create: `src/main/java/com/financafacil/domain/port/out/TransactionRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/entity/TransactionJpaEntity.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/jpa/TransactionJpaRepository.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/specification/TransactionSpecification.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/mapper/TransactionJpaMapper.java`
- Create: `src/main/java/com/financafacil/infrastructure/persistence/adapter/TransactionRepositoryAdapter.java`
- Create: `src/main/java/com/financafacil/application/usecase/TransactionUseCaseImpl.java`
- Create: `src/main/java/com/financafacil/presentation/controller/TransactionController.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/CreateTransactionRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/UpdateTransactionRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/request/TransactionFilterRequest.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/TransactionResponse.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/PageResponse.java`
- Create: `src/main/java/com/financafacil/presentation/mapper/TransactionPresentationMapper.java`
- Create: `src/test/java/com/financafacil/application/usecase/TransactionUseCaseImplTest.java`
- Create: `src/test/java/com/financafacil/presentation/controller/TransactionControllerIntegrationTest.java`

**Interfaces:**
- Consumes: `AccountRepository.updateBalance()` (Task 6)
- Produces:
  - `TransactionRepository.save(Transaction): Transaction`
  - `TransactionRepository.findById(UUID, UUID): Optional<Transaction>`
  - `TransactionRepository.findAll(UUID, filters, Pageable): Page<Transaction>`
  - `TransactionRepository.deleteById(UUID, UUID): void`
  - `TransactionRepository.findByAccountIdForPeriod(UUID, LocalDate, LocalDate): List<Transaction>`

- [ ] **Step 1: Criar `Transaction.java` (domain model)**

```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder @With
public class Transaction {
    private final UUID id;
    private final UUID userId;
    private final UUID accountId;
    private final UUID categoryId;
    private final String type;
    private final BigDecimal amount;
    private final String description;
    private final LocalDate transactionDate;
    private final String status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
```

- [ ] **Step 2: Criar `TransactionRepository.java` (port out)**

```java
package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id, UUID userId);
    Page<Transaction> findAll(UUID userId, TransactionFilter filter, Pageable pageable);
    void deleteById(UUID id, UUID userId);
    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate start, LocalDate end);
    List<Transaction> findByAccountIdAndDateBetween(UUID accountId, LocalDate start, LocalDate end);

    record TransactionFilter(
        LocalDate startDate, LocalDate endDate,
        UUID categoryId, UUID accountId,
        String type, String status
    ) {}
}
```

- [ ] **Step 3: Criar `TransactionJpaEntity.java`**

```java
package com.financafacil.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "account_id", nullable = false) private UUID accountId;
    @Column(name = "category_id") private UUID categoryId;
    @Column(nullable = false, length = 10) private String type;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal amount;
    @Column private String description;
    @Column(name = "transaction_date", nullable = false) private LocalDate transactionDate;
    @Column(nullable = false, length = 20) private String status;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
```

- [ ] **Step 4: Criar `TransactionSpecification.java`**

```java
package com.financafacil.infrastructure.persistence.specification;

import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<TransactionJpaEntity> forUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<TransactionJpaEntity> withFilter(UUID userId, TransactionFilter filter) {
        Specification<TransactionJpaEntity> spec = forUser(userId);
        if (filter.startDate() != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("transactionDate"), filter.startDate()));
        }
        if (filter.endDate() != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("transactionDate"), filter.endDate()));
        }
        if (filter.categoryId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("categoryId"), filter.categoryId()));
        }
        if (filter.accountId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("accountId"), filter.accountId()));
        }
        if (filter.type() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("type"), filter.type()));
        }
        if (filter.status() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), filter.status()));
        }
        return spec;
    }
}
```

- [ ] **Step 5: Criar `TransactionJpaRepository.java`, mapper e adapter**

`TransactionJpaRepository.java`:
```java
package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>,
        JpaSpecificationExecutor<TransactionJpaEntity> {
    Optional<TransactionJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT t FROM TransactionJpaEntity t WHERE t.userId = :userId " +
           "AND t.transactionDate BETWEEN :start AND :end")
    List<TransactionJpaEntity> findByUserIdAndDateBetween(
        @Param("userId") UUID userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT t FROM TransactionJpaEntity t WHERE t.accountId = :accountId " +
           "AND t.transactionDate BETWEEN :start AND :end")
    List<TransactionJpaEntity> findByAccountIdAndDateBetween(
        @Param("accountId") UUID accountId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
```

`TransactionJpaMapper.java`:
```java
package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Transaction;
import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionJpaMapper {
    TransactionJpaEntity toEntity(Transaction transaction);
    Transaction toDomain(TransactionJpaEntity entity);
}
```

`TransactionRepositoryAdapter.java`:
```java
package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.financafacil.infrastructure.persistence.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;

    @Override public Transaction save(Transaction t) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(t))); }
    @Override public Optional<Transaction> findById(UUID id, UUID userId) { return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain); }

    @Override
    public Page<Transaction> findAll(UUID userId, TransactionFilter filter, Pageable pageable) {
        var spec = TransactionSpecification.withFilter(userId, filter);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id, UUID userId) {
        jpaRepository.findByIdAndUserId(id, userId).ifPresent(jpaRepository::delete);
    }

    @Override
    public List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate start, LocalDate end) {
        return jpaRepository.findByUserIdAndDateBetween(userId, start, end).stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAccountIdAndDateBetween(UUID accountId, LocalDate start, LocalDate end) {
        return jpaRepository.findByAccountIdAndDateBetween(accountId, start, end).stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
```

- [ ] **Step 6: Criar DTOs de request/response**

`TransactionResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder
public class TransactionResponse {
    private final UUID id;
    private final UUID accountId;
    private final UUID categoryId;
    private final String type;
    private final BigDecimal amount;
    private final String description;
    private final LocalDate transactionDate;
    private final String status;
}
```

`PageResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter @Builder
public class PageResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
```

`CreateTransactionRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateTransactionRequest {
    @NotNull private UUID accountId;
    private UUID categoryId;
    @NotBlank @Pattern(regexp = "income|expense") private String type;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String description;
    @NotNull private LocalDate transactionDate;
    @NotBlank @Pattern(regexp = "PENDING|PAID|OVERDUE|CANCELLED") private String status;
}
```

`UpdateTransactionRequest.java`:
```java
package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTransactionRequest {
    @NotNull private UUID accountId;
    private UUID categoryId;
    @NotBlank @Pattern(regexp = "income|expense") private String type;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String description;
    @NotNull private LocalDate transactionDate;
    @NotBlank @Pattern(regexp = "PENDING|PAID|OVERDUE|CANCELLED") private String status;
}
```

`TransactionPresentationMapper.java`:
```java
package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Transaction;
import com.financafacil.presentation.dto.response.TransactionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionPresentationMapper {
    TransactionResponse toResponse(Transaction transaction);
}
```

- [ ] **Step 7: Escrever testes unitários para `TransactionUseCaseImpl`**

```java
// src/test/java/com/financafacil/application/usecase/TransactionUseCaseImplTest.java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionUseCaseImplTest {
    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransactionPresentationMapper presentationMapper;
    @InjectMocks TransactionUseCaseImpl transactionUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_updatesAccountBalance_forIncome() {
        var accountId = UUID.randomUUID();
        var account = buildAccount(accountId, BigDecimal.valueOf(1000));
        var savedTx = buildTransaction(accountId, "income", BigDecimal.valueOf(500));
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenReturn(savedTx);

        transactionUseCase.create(userId, accountId, null, "income", BigDecimal.valueOf(500), null, LocalDate.now(), "PAID");

        verify(accountRepository).updateBalance(accountId, BigDecimal.valueOf(1500));
    }

    @Test
    void create_updatesAccountBalance_forExpense() {
        var accountId = UUID.randomUUID();
        var account = buildAccount(accountId, BigDecimal.valueOf(1000));
        var savedTx = buildTransaction(accountId, "expense", BigDecimal.valueOf(300));
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenReturn(savedTx);

        transactionUseCase.create(userId, accountId, null, "expense", BigDecimal.valueOf(300), null, LocalDate.now(), "PAID");

        verify(accountRepository).updateBalance(accountId, BigDecimal.valueOf(700));
    }

    @Test
    void create_throwsNotFound_whenAccountNotBelongsToUser() {
        var accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionUseCase.create(userId, accountId, null, "expense",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_revertsAccountBalance() {
        var accountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        var tx = buildTransaction(accountId, "expense", BigDecimal.valueOf(200));
        var account = buildAccount(accountId, BigDecimal.valueOf(800));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(tx));
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.of(account));

        transactionUseCase.delete(userId, txId);

        verify(accountRepository).updateBalance(accountId, BigDecimal.valueOf(1000));
        verify(transactionRepository).deleteById(txId, userId);
    }

    private Account buildAccount(UUID id, BigDecimal balance) {
        return Account.builder().id(id).userId(userId).name("Test").type("checking")
            .balance(balance).color("#000").createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private Transaction buildTransaction(UUID accountId, String type, BigDecimal amount) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId).accountId(accountId)
            .type(type).amount(amount).transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
```

- [ ] **Step 8: Executar testes — verificar falha**

```bash
./gradlew test --tests "com.financafacil.application.usecase.TransactionUseCaseImplTest" 2>&1 | tail -10
```

Esperado: FAIL.

- [ ] **Step 9: Criar `TransactionUseCase.java` (port in) e `TransactionUseCaseImpl.java`**

`TransactionUseCase.java`:
```java
package com.financafacil.domain.port.in;

import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.presentation.dto.response.PageResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionUseCase {
    TransactionResponse create(UUID userId, UUID accountId, UUID categoryId, String type,
        BigDecimal amount, String description, LocalDate transactionDate, String status);
    TransactionResponse update(UUID userId, UUID transactionId, UUID accountId, UUID categoryId,
        String type, BigDecimal amount, String description, LocalDate transactionDate, String status);
    void delete(UUID userId, UUID transactionId);
    PageResponse<TransactionResponse> findAll(UUID userId, TransactionFilter filter, Pageable pageable);
}
```

`TransactionUseCaseImpl.java`:
```java
package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.in.TransactionUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.presentation.dto.response.PageResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionUseCaseImpl implements TransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionPresentationMapper presentationMapper;

    @Override @Transactional
    public TransactionResponse create(UUID userId, UUID accountId, UUID categoryId, String type,
                                      BigDecimal amount, String description, LocalDate transactionDate, String status) {
        var account = accountRepository.findById(accountId, userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        var transaction = transactionRepository.save(Transaction.builder()
            .id(UUID.randomUUID()).userId(userId).accountId(accountId).categoryId(categoryId)
            .type(type).amount(amount).description(description)
            .transactionDate(transactionDate).status(status)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build());

        var delta = "income".equals(type) ? amount : amount.negate();
        accountRepository.updateBalance(accountId, account.getBalance().add(delta));

        return presentationMapper.toResponse(transaction);
    }

    @Override @Transactional
    public TransactionResponse update(UUID userId, UUID transactionId, UUID accountId, UUID categoryId,
                                      String type, BigDecimal amount, String description,
                                      LocalDate transactionDate, String status) {
        var existing = transactionRepository.findById(transactionId, userId)
            .orElseThrow(() -> new NotFoundException("Transação não encontrada"));
        var account = accountRepository.findById(existing.getAccountId(), userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        // Reverter saldo antigo
        var oldDelta = "income".equals(existing.getType()) ? existing.getAmount().negate() : existing.getAmount();
        // Aplicar novo saldo
        var newDelta = "income".equals(type) ? amount : amount.negate();
        accountRepository.updateBalance(existing.getAccountId(), account.getBalance().add(oldDelta).add(newDelta));

        var updated = transactionRepository.save(existing
            .withAccountId(accountId).withCategoryId(categoryId).withType(type)
            .withAmount(amount).withDescription(description)
            .withTransactionDate(transactionDate).withStatus(status)
            .withUpdatedAt(Instant.now()));

        return presentationMapper.toResponse(updated);
    }

    @Override @Transactional
    public void delete(UUID userId, UUID transactionId) {
        var tx = transactionRepository.findById(transactionId, userId)
            .orElseThrow(() -> new NotFoundException("Transação não encontrada"));
        var account = accountRepository.findById(tx.getAccountId(), userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        var revertDelta = "income".equals(tx.getType()) ? tx.getAmount().negate() : tx.getAmount();
        accountRepository.updateBalance(tx.getAccountId(), account.getBalance().add(revertDelta));
        transactionRepository.deleteById(transactionId, userId);
    }

    @Override
    public PageResponse<TransactionResponse> findAll(UUID userId, TransactionFilter filter, Pageable pageable) {
        var page = transactionRepository.findAll(userId, filter, pageable);
        return PageResponse.<TransactionResponse>builder()
            .content(page.getContent().stream().map(presentationMapper::toResponse).collect(Collectors.toList()))
            .page(page.getNumber()).size(page.getSize())
            .totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).build();
    }
}
```

- [ ] **Step 10: Criar `TransactionController.java`**

```java
package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.TransactionUseCase;
import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.presentation.dto.request.*;
import com.financafacil.presentation.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionUseCase transactionUseCase;

    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> findAll(
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String sort) {
        var filter = new TransactionFilter(startDate, endDate, categoryId, accountId, type, status);
        var parts = sort.split(",");
        var pageable = PageRequest.of(page, size,
            Sort.by(parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC, parts[0]));
        return ApiResponse.ok(transactionUseCase.findAll(toUuid(principal), filter, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionResponse> create(@AuthenticationPrincipal Object principal,
                                                   @Valid @RequestBody CreateTransactionRequest req) {
        return ApiResponse.ok(transactionUseCase.create(toUuid(principal), req.getAccountId(),
            req.getCategoryId(), req.getType(), req.getAmount(), req.getDescription(),
            req.getTransactionDate(), req.getStatus()));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal Object principal,
                                                   @PathVariable UUID id,
                                                   @Valid @RequestBody UpdateTransactionRequest req) {
        return ApiResponse.ok(transactionUseCase.update(toUuid(principal), id, req.getAccountId(),
            req.getCategoryId(), req.getType(), req.getAmount(), req.getDescription(),
            req.getTransactionDate(), req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        transactionUseCase.delete(toUuid(principal), id);
    }

    private UUID toUuid(Object principal) { return UUID.fromString(principal.toString()); }
}
```

- [ ] **Step 11: Criar teste de integração para Transaction**

```java
// src/test/java/com/financafacil/presentation/controller/TransactionControllerIntegrationTest.java
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransactionControllerIntegrationTest {

    @LocalServerPort int port;
    private String accessToken;
    private String accountId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        accessToken = given().contentType(ContentType.JSON)
            .body("""
                {"name":"Tx User","email":"txuser@test.com","password":"password123"}
                """)
            .post("/auth/register").jsonPath().getString("data.accessToken");

        accountId = given().contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body("""
                {"name":"Conta","type":"checking","initialBalance":5000.00,"color":"#000000"}
                """)
            .post("/accounts").jsonPath().getString("data.id");
    }

    @Test
    void createTransaction_updatesAccountBalance() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken)
            .body(String.format("""
                {"accountId":"%s","type":"expense","amount":500.00,"transactionDate":"2026-07-01","status":"PAID"}
                """, accountId))
        .when()
            .post("/transactions")
        .then()
            .statusCode(201)
            .body("data.amount", equalTo(500.0f))
            .body("data.status", equalTo("PAID"));

        given()
            .header("Authorization", "Bearer " + accessToken)
            .get("/accounts/" + accountId + "/balance")
        .then()
            .statusCode(200)
            .body("data", equalTo(4500.0f));
    }

    @Test
    void listTransactions_withFilter_returnsFilteredResults() {
        given()
            .header("Authorization", "Bearer " + accessToken)
            .queryParam("type", "expense")
        .when()
            .get("/transactions")
        .then()
            .statusCode(200)
            .body("data.content", notNullValue())
            .body("data.totalElements", greaterThanOrEqualTo(0));
    }
}
```

- [ ] **Step 12: Executar testes**

```bash
./gradlew test --tests "com.financafacil.application.usecase.TransactionUseCaseImplTest"
./gradlew test --tests "com.financafacil.presentation.controller.TransactionControllerIntegrationTest"
```

Esperado: `BUILD SUCCESSFUL`.

- [ ] **Step 13: Commit**

```bash
git add src/
git commit -m "feat: implement transaction module with balance update and filters"
```

---

### Task 9: Módulo Dashboard

**Files:**
- Create: `src/main/java/com/financafacil/domain/port/in/DashboardUseCase.java`
- Create: `src/main/java/com/financafacil/application/usecase/DashboardUseCaseImpl.java`
- Create: `src/main/java/com/financafacil/presentation/controller/DashboardController.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/DashboardSummaryResponse.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/MonthlySeriesResponse.java`
- Create: `src/main/java/com/financafacil/presentation/dto/response/CategoryBreakdownResponse.java`
- Create: `src/test/java/com/financafacil/application/usecase/DashboardUseCaseImplTest.java`

**Interfaces:**
- Consumes: `TransactionRepository.findByUserIdAndDateBetween()`, `AccountRepository.findAllByUserId()`
- Produces:
  - `DashboardUseCase.getSummary(userId, year, month): DashboardSummaryResponse`
  - `DashboardUseCase.getMonthlySeries(userId, months): List<MonthlySeriesResponse>`
  - `DashboardUseCase.getByCategory(userId, year, month, type): List<CategoryBreakdownResponse>`
  - `DashboardUseCase.getRecentTransactions(userId, limit): List<TransactionResponse>`

- [ ] **Step 1: Criar DTOs de resposta do Dashboard**

`DashboardSummaryResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class DashboardSummaryResponse {
    private final BigDecimal totalBalance;
    private final BigDecimal monthIncome;
    private final BigDecimal monthExpense;
    private final int year;
    private final int month;
}
```

`MonthlySeriesResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class MonthlySeriesResponse {
    private final String label;
    private final int year;
    private final int month;
    private final BigDecimal income;
    private final BigDecimal expense;
}
```

`CategoryBreakdownResponse.java`:
```java
package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Builder
public class CategoryBreakdownResponse {
    private final UUID categoryId;
    private final String categoryName;
    private final String color;
    private final BigDecimal total;
}
```

- [ ] **Step 2: Criar `DashboardUseCase.java` (port in)**

```java
package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.*;
import java.util.List;
import java.util.UUID;

public interface DashboardUseCase {
    DashboardSummaryResponse getSummary(UUID userId, int year, int month);
    List<MonthlySeriesResponse> getMonthlySeries(UUID userId, int months);
    List<CategoryBreakdownResponse> getByCategory(UUID userId, int year, int month, String type);
    List<TransactionResponse> getRecentTransactions(UUID userId, int limit);
}
```

- [ ] **Step 3: Escrever testes unitários para `DashboardUseCaseImpl`**

```java
// src/test/java/com/financafacil/application/usecase/DashboardUseCaseImplTest.java
package com.financafacil.application.usecase;

import com.financafacil.domain.model.*;
import com.financafacil.domain.port.out.*;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardUseCaseImplTest {
    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TransactionPresentationMapper presentationMapper;
    @InjectMocks DashboardUseCaseImpl dashboardUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getSummary_calculatesCorrectTotals() {
        var now = LocalDate.now();
        var start = now.withDayOfMonth(1);
        var end = now.withDayOfMonth(now.lengthOfMonth());
        var income = buildTx("income", BigDecimal.valueOf(5000));
        var expense = buildTx("expense", BigDecimal.valueOf(2000));
        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
            .thenReturn(List.of(income, expense));
        when(accountRepository.findAllByUserId(userId)).thenReturn(
            List.of(Account.builder().id(UUID.randomUUID()).userId(userId).name("A")
                .type("checking").balance(BigDecimal.valueOf(3000)).color("#000")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build()));

        var result = dashboardUseCase.getSummary(userId, now.getYear(), now.getMonthValue());

        assertThat(result.getMonthIncome()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(result.getMonthExpense()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(result.getTotalBalance()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void getMonthlySeries_returnsCorrectNumberOfMonths() {
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
            .thenReturn(List.of());
        var result = dashboardUseCase.getMonthlySeries(userId, 6);
        assertThat(result).hasSize(6);
    }

    private Transaction buildTx(String type, BigDecimal amount) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId)
            .accountId(UUID.randomUUID()).type(type).amount(amount)
            .transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
```

- [ ] **Step 4: Executar testes — verificar falha**

```bash
./gradlew test --tests "com.financafacil.application.usecase.DashboardUseCaseImplTest" 2>&1 | tail -10
```

- [ ] **Step 5: Criar `DashboardUseCaseImpl.java`**

```java
package com.financafacil.application.usecase;

import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.in.DashboardUseCase;
import com.financafacil.domain.port.out.*;
import com.financafacil.presentation.dto.response.*;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardUseCaseImpl implements DashboardUseCase {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionPresentationMapper transactionMapper;

    @Override
    public DashboardSummaryResponse getSummary(UUID userId, int year, int month) {
        var start = LocalDate.of(year, month, 1);
        var end = start.withDayOfMonth(start.lengthOfMonth());
        var transactions = transactionRepository.findByUserIdAndDateBetween(userId, start, end);

        var income = transactions.stream().filter(t -> "income".equals(t.getType()))
            .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var expense = transactions.stream().filter(t -> "expense".equals(t.getType()))
            .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalBalance = accountRepository.findAllByUserId(userId).stream()
            .map(a -> a.getBalance()).reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryResponse.builder()
            .totalBalance(totalBalance).monthIncome(income).monthExpense(expense)
            .year(year).month(month).build();
    }

    @Override
    public List<MonthlySeriesResponse> getMonthlySeries(UUID userId, int months) {
        var result = new ArrayList<MonthlySeriesResponse>();
        var now = LocalDate.now();
        for (int i = months - 1; i >= 0; i--) {
            var date = now.minusMonths(i);
            var start = date.withDayOfMonth(1);
            var end = date.withDayOfMonth(date.lengthOfMonth());
            var txs = transactionRepository.findByUserIdAndDateBetween(userId, start, end);
            var income = txs.stream().filter(t -> "income".equals(t.getType()))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            var expense = txs.stream().filter(t -> "expense".equals(t.getType()))
                .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            result.add(MonthlySeriesResponse.builder()
                .label(date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")))
                .year(date.getYear()).month(date.getMonthValue())
                .income(income).expense(expense).build());
        }
        return result;
    }

    @Override
    public List<CategoryBreakdownResponse> getByCategory(UUID userId, int year, int month, String type) {
        var start = LocalDate.of(year, month, 1);
        var end = start.withDayOfMonth(start.lengthOfMonth());
        var transactions = transactionRepository.findByUserIdAndDateBetween(userId, start, end)
            .stream().filter(t -> type.equals(t.getType()) && t.getCategoryId() != null)
            .collect(Collectors.toList());

        var categories = categoryRepository.findAllByUserId(userId).stream()
            .collect(Collectors.toMap(c -> c.getId(), c -> c));

        return transactions.stream()
            .collect(Collectors.groupingBy(Transaction::getCategoryId,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)))
            .entrySet().stream()
            .map(e -> {
                var cat = categories.get(e.getKey());
                return CategoryBreakdownResponse.builder()
                    .categoryId(e.getKey())
                    .categoryName(cat != null ? cat.getName() : "Sem categoria")
                    .color(cat != null ? cat.getColor() : "#999999")
                    .total(e.getValue()).build();
            })
            .sorted(Comparator.comparing(CategoryBreakdownResponse::getTotal).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getRecentTransactions(UUID userId, int limit) {
        var start = LocalDate.now().minusMonths(3);
        var end = LocalDate.now();
        return transactionRepository.findByUserIdAndDateBetween(userId, start, end)
            .stream()
            .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
            .limit(limit)
            .map(transactionMapper::toResponse)
            .collect(Collectors.toList());
    }
}
```

- [ ] **Step 6: Criar `DashboardController.java`**

```java
package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.DashboardUseCase;
import com.financafacil.presentation.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardUseCase dashboardUseCase;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        var now = LocalDate.now();
        int y = year == 0 ? now.getYear() : year;
        int m = month == 0 ? now.getMonthValue() : month;
        return ApiResponse.ok(dashboardUseCase.getSummary(toUuid(principal), y, m));
    }

    @GetMapping("/monthly-series")
    public ApiResponse<List<MonthlySeriesResponse>> getMonthlySeries(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "6") int months) {
        return ApiResponse.ok(dashboardUseCase.getMonthlySeries(toUuid(principal), months));
    }

    @GetMapping("/by-category")
    public ApiResponse<List<CategoryBreakdownResponse>> getByCategory(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month,
            @RequestParam(defaultValue = "expense") String type) {
        var now = LocalDate.now();
        int y = year == 0 ? now.getYear() : year;
        int m = month == 0 ? now.getMonthValue() : month;
        return ApiResponse.ok(dashboardUseCase.getByCategory(toUuid(principal), y, m, type));
    }

    @GetMapping("/recent-transactions")
    public ApiResponse<List<TransactionResponse>> getRecentTransactions(
            @AuthenticationPrincipal Object principal,
            @RequestParam(defaultValue = "6") int limit) {
        return ApiResponse.ok(dashboardUseCase.getRecentTransactions(toUuid(principal), limit));
    }

    private UUID toUuid(Object principal) { return UUID.fromString(principal.toString()); }
}
```

- [ ] **Step 7: Executar testes**

```bash
./gradlew test --tests "com.financafacil.application.usecase.DashboardUseCaseImplTest"
```

Esperado: `BUILD SUCCESSFUL`.

- [ ] **Step 8: Commit**

```bash
git add src/
git commit -m "feat: implement dashboard module with summary, series and category breakdown"
```

---

### Task 10: Módulos extras backend-only (Budget, Transfer, Recurring)

**Files:**
- Create: `src/main/java/com/financafacil/domain/model/Budget.java`
- Create: `src/main/java/com/financafacil/domain/model/Transfer.java`
- Create: `src/main/java/com/financafacil/domain/model/RecurringTransaction.java`
- Create: *(JPA entities, repositories, adapters, use cases e controllers para os 3 módulos — padrão idêntico ao Account/Category)*

**Interfaces:**
- Produces: endpoints `/budgets`, `/transfers`, `/recurring-transactions` funcionais e documentados no Swagger

- [ ] **Step 1: Criar `Budget.java`, `Transfer.java`, `RecurringTransaction.java` (domain models)**

`Budget.java`:
```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @With
public class Budget {
    private final UUID id;
    private final UUID userId;
    private final UUID categoryId;
    private final BigDecimal amount;
    private final int month;
    private final int year;
    private final Instant createdAt;
    private final Instant updatedAt;
}
```

`Transfer.java`:
```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder
public class Transfer {
    private final UUID id;
    private final UUID userId;
    private final UUID fromAccountId;
    private final UUID toAccountId;
    private final BigDecimal amount;
    private final LocalDate transferDate;
    private final String description;
    private final Instant createdAt;
}
```

`RecurringTransaction.java`:
```java
package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder @With
public class RecurringTransaction {
    private final UUID id;
    private final UUID userId;
    private final UUID accountId;
    private final UUID categoryId;
    private final BigDecimal amount;
    private final String type;
    private final String frequency;
    private final LocalDate nextExecution;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
```

- [ ] **Step 2: Implementar Budget — seguindo o mesmo padrão do Account**

Crie os seguintes arquivos com o padrão estabelecido nas Tasks 6–9:
- `BudgetJpaEntity.java` — mapeado para tabela `budgets`
- `BudgetJpaRepository.java` — com `findAllByUserId`, `findByIdAndUserId`, `existsByUserIdAndCategoryIdAndMonthAndYear`
- `BudgetJpaMapper.java` — MapStruct domain ↔ entity
- `BudgetRepositoryAdapter.java` — implementa `BudgetRepository` port out
- `BudgetRepository.java` — port out com `save`, `findById(UUID,UUID)`, `findAllByUserId`, `deleteById(UUID,UUID)`, `existsByUserIdAndCategoryIdAndMonthAndYear(UUID,UUID,int,int): boolean`
- `BudgetUseCase.java` — port in com `create`, `findAll`, `update`, `delete`
- `BudgetUseCaseImpl.java` — valida unicidade `(userId, categoryId, month, year)` com `ConflictException` antes de salvar
- `CreateBudgetRequest.java` — `@NotNull categoryId`, `@NotNull amount`, `@Min(1) @Max(12) month`, `@Min(2020) year`
- `UpdateBudgetRequest.java` — apenas `amount`
- `BudgetResponse.java` — `id, categoryId, amount, month, year`
- `BudgetController.java` — CRUD em `/budgets`

- [ ] **Step 3: Implementar Transfer — com consistência transacional**

Crie:
- `TransferJpaEntity.java` — mapeado para tabela `transfers`
- `TransferJpaRepository.java`
- `TransferJpaMapper.java`
- `TransferRepositoryAdapter.java`
- `TransferRepository.java` — port out com `save`, `findById(UUID,UUID)`, `findAllByUserId`, `deleteById(UUID,UUID)`
- `TransferUseCase.java` — port in com `create(userId, fromAccountId, toAccountId, amount, date, description): TransferResponse`, `findAll(userId): List<TransferResponse>`, `delete(userId, transferId): void`
- `TransferUseCaseImpl.java`:
  - Valida que `fromAccountId` e `toAccountId` pertencem ao `userId`
  - Debita `fromAccount.balance -= amount` e credita `toAccount.balance += amount` em `@Transactional`
  - Na deleção, reverte ambos os saldos
- `CreateTransferRequest.java` — `fromAccountId, toAccountId, amount, transferDate, description`
- `TransferResponse.java` — `id, fromAccountId, toAccountId, amount, transferDate, description`
- `TransferController.java` — `POST /transfers`, `GET /transfers`, `DELETE /transfers/{id}`

- [ ] **Step 4: Implementar RecurringTransaction — seguindo padrão do Account**

Crie:
- `RecurringTransactionJpaEntity.java` — mapeado para `recurring_transactions`
- `RecurringTransactionJpaRepository.java`
- `RecurringTransactionJpaMapper.java`
- `RecurringTransactionRepositoryAdapter.java`
- `RecurringTransactionRepository.java` — port out com `save`, `findById(UUID,UUID)`, `findAllByUserId`, `deleteById(UUID,UUID)`
- `RecurringTransactionUseCase.java` — port in com `create`, `findAll`, `update`, `delete`, `toggleActive(userId, id): RecurringTransactionResponse`
- `RecurringTransactionUseCaseImpl.java`
- `CreateRecurringTransactionRequest.java` — todos os campos do modelo
- `UpdateRecurringTransactionRequest.java`
- `RecurringTransactionResponse.java`
- `RecurringTransactionController.java` — CRUD em `/recurring-transactions` + `PATCH /{id}/toggle`

- [ ] **Step 5: Executar build completo**

```bash
./gradlew build -x test
```

Esperado: `BUILD SUCCESSFUL`

- [ ] **Step 6: Executar todos os testes**

```bash
./gradlew test
```

Esperado: `BUILD SUCCESSFUL`, todos os testes passando.

- [ ] **Step 7: Commit**

```bash
git add src/
git commit -m "feat: implement budget, transfer and recurring transaction modules"
```
