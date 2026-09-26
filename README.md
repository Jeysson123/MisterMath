# MisterMath

![CI/CD](https://github.com/Jeysson123/MisterMath/actions/workflows/ci-cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Redis](https://img.shields.io/badge/Redis-7-red)

Two numbers, one operation, and **you pick the design pattern that runs it**.
A learning project for **SOLID** and **design patterns** in Spring Boot.

---

## Contents

1. [Quick start](#1-quick-start)
2. [Architecture](#2-architecture)
3. [Request flow](#3-request-flow-post)
4. [Pattern selection](#4-pattern-selection)
5. [The five patterns](#5-the-five-patterns)
6. [SOLID map](#6-solid-map)
7. [Endpoints](#7-endpoints)
8. [Response wrapper and errors](#8-response-wrapper-and-errors)
9. [Security (JWT)](#9-security-jwt)
10. [Persistence: PostgreSQL + Redis](#10-persistence-postgresql--redis)
11. [Logging](#11-logging)
12. [Docker](#12-docker)
13. [CI/CD](#13-cicd)
14. [Tests](#14-tests)
15. [Project structure](#15-project-structure)

---

## 1. Quick start

```bash
docker compose up --build
```

```bash
# 1) token
TOKEN=$(curl -s -X POST localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r .data.accessToken)

# 2) calculate
curl -s -X POST localhost:8080/api/v1/calculations \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"num1":10,"num2":5,"operation":"+","pattern":"STRATEGY"}'

# 3) recent transactions (Redis)
curl -s localhost:8080/api/v1/transactions -H "Authorization: Bearer $TOKEN"

# 4) one transaction (PostgreSQL)
curl -s localhost:8080/api/v1/transactions/1 -H "Authorization: Bearer $TOKEN"
```

More examples: [`requests.http`](requests.http).

---

## 2. Architecture

```mermaid
flowchart LR
    Client([Client])

    subgraph API["api"]
        AC[AuthController]
        CC[CalculationController]
        TC[TransactionController]
        GEH[GlobalExceptionHandler]
        LRA[LoggingResponseAdvice]
    end

    subgraph APP["application (CQRS)"]
        CH[CalculateCommandHandler]
        QH1[GetCachedTransactionsQueryHandler]
        QH2[GetTransactionByIdQueryHandler]
        P1{{TransactionWriter}}
        P2{{TransactionReader}}
        P3{{TransactionCacheWriter}}
        P4{{TransactionCacheReader}}
    end

    subgraph DOMAIN["domain"]
        R[CalculationEngineResolver]
        E{{CalculationEngine}}
        OP{{MathOperation}}
    end

    subgraph INFRA["infrastructure"]
        SEC[JWT filter + SecurityConfig]
        JPA[JpaTransactionStore]
        RED[RedisTransactionCache]
    end

    PG[(PostgreSQL)]
    RD[(Redis)]

    Client --> SEC --> AC & CC & TC
    CC -->|Command| CH
    TC -->|Query| QH1 & QH2
    CH --> R --> E --> OP
    CH --> P1 & P3
    QH1 --> P4
    QH2 --> P2
    JPA -. implements .-> P1 & P2
    RED -. implements .-> P3 & P4
    JPA --> PG
    RED --> RD
```

```mermaid
flowchart TB
    api["api<br/>controllers · DTOs · validation · advices"]
    application["application<br/>commands · queries · ports"]
    domain["domain<br/>operations · patterns · exceptions"]
    infrastructure["infrastructure<br/>JPA · Redis · JWT"]

    api --> application --> domain
    infrastructure -. implements ports .-> application
```

---

## 3. Request flow (POST)

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant F as JwtAuthenticationFilter
    participant CC as CalculationController
    participant V as Bean Validation
    participant H as CalculateCommandHandler
    participant R as CalculationEngineResolver
    participant E as CalculationEngine
    participant DB as PostgreSQL
    participant RD as Redis
    participant L as LoggingResponseAdvice

    C->>F: POST /api/v1/calculations + Bearer token
    F->>F: validate JWT
    F->>CC: authenticated request
    CC->>V: @Valid CalculationRequest
    V-->>CC: ok (else 400)
    CC->>H: CalculateCommand
    H->>R: resolve(pattern)
    R-->>H: engine for that pattern
    H->>E: calculate(num1, operation, num2)
    E-->>H: result + trace (else 422)
    H->>DB: INSERT request, response, created_at
    DB-->>H: id
    H->>RD: LPUSH + LTRIM
    H-->>CC: CalculationResult
    CC->>L: ApiResponse 201
    L->>L: log method, endpoint, result
    L-->>C: { code, status, data }
```

---

## 4. Pattern selection

```mermaid
flowchart TD
    P["payload.pattern"] --> R{CalculationEngineResolver}
    R -->|SINGLETON| S[SingletonCalculationEngine]
    R -->|FACTORY| F[FactoryCalculationEngine]
    R -->|STRATEGY| ST[StrategyCalculationEngine]
    R -->|BUILDER| B[BuilderCalculationEngine]

    S --> S1["SingletonCalculator.getInstance()<br/>same object every time"]
    F --> F1["MathOperationFactory.create(symbol)<br/>new object every time"]
    ST --> ST1["OperationStrategyContext.strategyFor(symbol)<br/>pick an existing Spring bean"]
    B --> B1["Expression.builder()...build().evaluate()<br/>immutable object, built step by step"]

    S1 & F1 & ST1 & B1 --> OP{{"MathOperation.apply(a, b)"}}
    OP --> A[Addition] & SU[Subtraction] & M[Multiplication] & D[Division] & MO[Modulo] & PW[Power]
```

Same input, same result. Only the path changes, and the `trace` field in the response shows it:

| `pattern` | `data.engine` | `data.trace` (example for `10 + 5`) |
|---|---|---|
| `SINGLETON` | `SingletonCalculationEngine` | `SingletonCalculator.getInstance() returned the shared instance @1b2c3d` |
| `FACTORY` | `FactoryCalculationEngine` | `MathOperationFactory.create("+") built a new Addition instance` |
| `STRATEGY` | `StrategyCalculationEngine` | `OperationStrategyContext selected the Spring bean Addition for '+'` |
| `BUILDER` | `BuilderCalculationEngine` | `build() validated the parts and created the immutable expression [10 + 5]` |

---

## 5. The five patterns

### Singleton — `domain/pattern/singleton`

```mermaid
classDiagram
    class SingletonCalculator {
        -Map~OperationType, MathOperation~ operations
        -SingletonCalculator()
        +getInstance()$ SingletonCalculator
        +calculate(num1, symbol, num2) BigDecimal
    }
    class Holder {
        -INSTANCE$ SingletonCalculator
    }
    SingletonCalculator +-- Holder
    SingletonCalculationEngine ..> SingletonCalculator : getInstance()
```

```mermaid
sequenceDiagram
    participant R1 as Request 1
    participant R2 as Request 2
    participant S as SingletonCalculator
    R1->>S: getInstance()
    Note over S: Holder class loads<br/>instance created ONCE
    S-->>R1: @1b2c3d
    R2->>S: getInstance()
    S-->>R2: @1b2c3d (same)
```

### Factory — `domain/pattern/factory`

```mermaid
classDiagram
    class MathOperationFactory {
        +create(symbol)$ MathOperation
        +create(OperationType)$ MathOperation
    }
    class MathOperation {
        <<interface>>
        +type() OperationType
        +apply(left, right) BigDecimal
    }
    MathOperationFactory ..> Addition : new
    MathOperationFactory ..> Division : new
    MathOperationFactory ..> Power : new
    MathOperation <|.. Addition
    MathOperation <|.. Division
    MathOperation <|.. Power
    FactoryCalculationEngine ..> MathOperationFactory
```

### Strategy — `domain/pattern/strategy`

```mermaid
classDiagram
    class OperationStrategyContext {
        -Map~String, MathOperation~ strategies
        +OperationStrategyContext(List~MathOperation~)
        +strategyFor(symbol) MathOperation
    }
    class MathOperation {
        <<interface>>
    }
    OperationStrategyContext o-- MathOperation : injected by Spring
    MathOperation <|.. Addition
    MathOperation <|.. Subtraction
    MathOperation <|.. Multiplication
    MathOperation <|.. Division
    MathOperation <|.. Modulo
    MathOperation <|.. Power
    StrategyCalculationEngine --> OperationStrategyContext
```

### Builder — `domain/pattern/builder`

```mermaid
classDiagram
    class Expression {
        -BigDecimal left
        -String operator
        -BigDecimal right
        -Expression(Builder)
        +builder()$ Builder
        +evaluate() BigDecimal
    }
    class Builder {
        +left(BigDecimal) Builder
        +operator(String) Builder
        +right(BigDecimal) Builder
        +build() Expression
    }
    Expression +-- Builder
    BuilderCalculationEngine ..> Builder
```

```mermaid
flowchart LR
    A["builder()"] --> B[".left(10)"] --> C[".operator('*')"] --> D[".right(4)"] --> E[".build()<br/>validates"] --> F["Expression 10 * 4<br/>(immutable)"] --> G[".evaluate() = 40"]
```

### CQRS — `application/cqrs`, `application/command`, `application/query`

```mermaid
flowchart LR
    subgraph WRITE["Command side"]
        POST["POST /calculations"] --> CMD[CalculateCommand] --> CH[CalculateCommandHandler]
        CH --> PG1[(PostgreSQL)]
        CH --> RD1[(Redis)]
    end
    subgraph READ["Query side"]
        GET1["GET /transactions"] --> Q1[GetCachedTransactionsQuery] --> QH1[...QueryHandler] --> RD2[(Redis)]
        GET2["GET /transactions/{id}"] --> Q2[GetTransactionByIdQuery] --> QH2[...QueryHandler] --> PG2[(PostgreSQL)]
    end
```

```mermaid
classDiagram
    class Command {
        <<interface>>
    }
    class Query {
        <<interface>>
    }
    class CommandHandler {
        <<interface>>
        +handle(command) result
    }
    class QueryHandler {
        <<interface>>
        +handle(query) result
    }
    Command <|.. CalculateCommand
    Query <|.. GetCachedTransactionsQuery
    Query <|.. GetTransactionByIdQuery
    CommandHandler <|.. CalculateCommandHandler
    QueryHandler <|.. GetCachedTransactionsQueryHandler
    QueryHandler <|.. GetTransactionByIdQueryHandler
```

---

## 6. SOLID map

```mermaid
mindmap
  root((SOLID))
    S Single Responsibility
      CalculationController: HTTP only
      CalculateCommandHandler: orchestrates only
      JpaTransactionStore: SQL only
      RedisTransactionCache: Redis only
      GlobalExceptionHandler: errors only
      LoggingResponseAdvice: logs only
    O Open/Closed
      new MathOperation class
      new CalculationEngine bean
      Resolver and Strategy context discover them
    L Liskov
      4 engines interchangeable
      6 operations interchangeable
      LiskovSubstitutionTest
    I Interface Segregation
      CommandHandler vs QueryHandler
      TransactionWriter vs TransactionReader
      TransactionCacheWriter vs TransactionCacheReader
    D Dependency Inversion
      handlers depend on ports
      infrastructure implements ports
      TokenProvider hides jjwt
```

| Principle | Where | Image example → MisterMath |
|---|---|---|
| **S** | `CalculationController`, `CalculateCommandHandler`, `JpaTransactionStore`, `RedisTransactionCache`, `TransactionViewMapper` | `Invoice / InvoiceRepository / InvoicePrinter` → controller / handler / store / cache |
| **O** | `MathOperation`, `CalculationEngine`, `CalculationEngineResolver`, `OperationStrategyContext` | `Payment` + `CreditCardPayment`, `PaypalPayment` → `MathOperation` + `Addition`, `Division`... |
| **L** | every `MathOperation`, every `CalculationEngine` (`LiskovSubstitutionTest`) | `Bird bird = new Eagle()` → `CalculationEngine engine = new FactoryCalculationEngine()` |
| **I** | `CommandHandler` / `QueryHandler`, `TransactionWriter` / `TransactionReader`, `TransactionCacheWriter` / `TransactionCacheReader` | `Printer`, `Scanner` → `Writer`, `Reader` |
| **D** | `CalculateCommandHandler` → ports; `JwtAuthenticationFilter` → `TokenProvider` | `UserService(Database)` → `CalculateCommandHandler(TransactionWriter)` |
| **Polymorphism** | `operation.apply(a, b)`, `engine.calculate(...)` | `Payment payment = new CreditCardPayment(); payment.pay()` → `MathOperation op = factory.create("+"); op.apply(a, b)` |

### Open/Closed: adding a new operation

```mermaid
flowchart LR
    A["1. OperationType.SQUARE_ROOT('√')"] --> B["2. class SquareRoot implements MathOperation<br/>@Component"] --> C["3. case SQUARE_ROOT -> new SquareRoot()<br/>in MathOperationFactory"]
    C --> D["Validation, Strategy, Singleton, Builder<br/>pick it up with no other change"]
```

### Open/Closed: adding a new pattern

```mermaid
flowchart LR
    A["1. CalculationPattern.PROTOTYPE"] --> B["2. @Component class PrototypeCalculationEngine<br/>implements CalculationEngine"]
    B --> C["CalculationEngineResolver finds it<br/>no switch to edit"]
```

---

## 7. Endpoints

```mermaid
flowchart LR
    subgraph PUBLIC["Public"]
        L["POST /api/v1/auth/login"]
        H["GET /actuator/health"]
    end
    subgraph PRIVATE["Bearer token required"]
        C["POST /api/v1/calculations"]
        T1["GET /api/v1/transactions?limit=20"]
        T2["GET /api/v1/transactions/{id}"]
    end
    C --> PG[(PostgreSQL)] & RD[(Redis)]
    T1 --> RD
    T2 --> PG
```

| Method | Endpoint | Auth | Source | Success |
|---|---|---|---|---|
| `POST` | `/api/v1/auth/login` | — | in-memory user | `200` |
| `POST` | `/api/v1/calculations` | Bearer | engine → PostgreSQL + Redis | `201` |
| `GET` | `/api/v1/transactions?limit=1..100` | Bearer | Redis | `200` |
| `GET` | `/api/v1/transactions/{id}` | Bearer | PostgreSQL | `200` |
| `GET` | `/actuator/health` | — | Spring Actuator | `200` |

### Payload

```json
{
  "num1": 10,
  "num2": 5,
  "operation": "+",
  "pattern": "STRATEGY"
}
```

| Field | Rules |
|---|---|
| `num1`, `num2` | required, up to 30 integer digits and 10 decimals |
| `operation` | `+` `-` `*` `/` `%` `^` |
| `pattern` | `SINGLETON` `FACTORY` `STRATEGY` `BUILDER` (any case) |

---

## 8. Response wrapper and errors

```mermaid
classDiagram
    class ApiResponse~T~ {
        +int code
        +String status
        +T data
        +ok(T)$ ApiResponse~T~
        +of(HttpStatus, T)$ ApiResponse~T~
    }
    class CalculationResult {
        +Long transactionId
        +BigDecimal num1
        +BigDecimal num2
        +String operation
        +CalculationPattern pattern
        +BigDecimal result
        +String engine
        +List~String~ trace
    }
    class ApiError {
        +String error
        +String message
        +Map~String,String~ fields
    }
    ApiResponse ..> CalculationResult : T on success
    ApiResponse ..> ApiError : T on error
```

```json
{
  "code": 201,
  "status": "CREATED",
  "data": {
    "transactionId": 1,
    "num1": 10,
    "num2": 5,
    "operation": "+",
    "pattern": "STRATEGY",
    "result": 15,
    "engine": "StrategyCalculationEngine",
    "trace": [
      "OperationStrategyContext selected the Spring bean Addition for '+'",
      "Strategy Addition applied = 15"
    ]
  }
}
```

```json
{
  "code": 400,
  "status": "BAD_REQUEST",
  "data": {
    "error": "Bad Request",
    "message": "Validation failed",
    "fields": {
      "num1": "num1 is required",
      "operation": "operation must be one of + - * / % ^",
      "pattern": "pattern must be one of SINGLETON, FACTORY, STRATEGY, BUILDER"
    }
  }
}
```

```mermaid
flowchart TD
    EX[Exception] --> A{type}
    A -->|MethodArgumentNotValid<br/>HandlerMethodValidation<br/>HttpMessageNotReadable<br/>TypeMismatch| B400[400 BAD_REQUEST]
    A -->|AuthenticationException| B401[401 UNAUTHORIZED]
    A -->|TransactionNotFound<br/>NoResourceFound| B404[404 NOT_FOUND]
    A -->|HttpRequestMethodNotSupported| B405[405 METHOD_NOT_ALLOWED]
    A -->|MathDomainException<br/>DivisionByZero · InvalidOperand<br/>UnsupportedOperationSymbol · UnsupportedPattern| B422[422 UNPROCESSABLE_ENTITY]
    A -->|anything else| B500[500 INTERNAL_SERVER_ERROR]
    F[Security filters] -->|no / bad token| S401[401 via JsonSecurityErrorHandler]
```

---

## 9. Security (JWT)

```mermaid
sequenceDiagram
    autonumber
    actor C as Client
    participant AC as AuthController
    participant AS as AuthenticationService
    participant AM as AuthenticationManager (BCrypt)
    participant TP as JwtTokenProvider
    participant F as JwtAuthenticationFilter
    participant API as Protected endpoint

    C->>AC: POST /auth/login {username, password}
    AC->>AS: login()
    AS->>AM: authenticate()
    AM-->>AS: ok (else 401)
    AS->>TP: generate(username)
    TP-->>C: { accessToken, tokenType: Bearer, expiresIn }

    C->>F: any request + Authorization: Bearer token
    F->>TP: validate(token)
    alt valid signature and not expired
        TP-->>F: username
        F->>API: SecurityContext = username
        API-->>C: 2xx
    else invalid / expired / missing
        F-->>C: 401 { code, status, data }
    end
```

| Variable | Default |
|---|---|
| `JWT_SECRET` | `mistermath-local-secret-change-me-at-least-32-bytes` |
| `JWT_EXPIRATION_SECONDS` | `3600` |
| `APP_USERNAME` | `admin` |
| `APP_PASSWORD` | `admin123` |

---

## 10. Persistence: PostgreSQL + Redis

```mermaid
erDiagram
    TRANSACTIONS {
        BIGSERIAL id PK
        TEXT request "JSON payload"
        TEXT response "JSON result"
        TIMESTAMPTZ created_at
    }
```

```mermaid
flowchart LR
    H[CalculateCommandHandler] -->|1. INSERT| PG[(PostgreSQL<br/>source of truth)]
    PG -->|id| H
    H -->|"2. LPUSH mistermath:transactions<br/>3. LTRIM 0 99"| RD[(Redis<br/>last 100)]
    RD -. write fails .-> W[WARN log, request still succeeds]
    Q1["GET /transactions"] -->|LRANGE 0 limit-1| RD
    Q2["GET /transactions/{id}"] -->|SELECT by id| PG
```

---

## 11. Logging

```mermaid
flowchart LR
    C[Controller] -->|ApiResponse| RBA[LoggingResponseAdvice<br/>ResponseBodyAdvice]
    GEH[GlobalExceptionHandler] -->|ApiResponse with ApiError| RBA
    RBA -->|INFO success / WARN error| LOG[(log)]
    RBA --> J[JSON to client]
    SEC[JsonSecurityErrorHandler<br/>401 / 403] -->|WARN| LOG
```

```text
INFO  method=POST endpoint=/api/v1/calculations code=201 status=CREATED result=CalculationResult[transactionId=1, num1=10, num2=5, operation=+, pattern=STRATEGY, result=15, ...]
WARN  method=POST endpoint=/api/v1/calculations code=422 status=UNPROCESSABLE_ENTITY error=ApiError[error=Unprocessable Entity, message=Division by zero is not allowed, fields={}]
WARN  method=GET endpoint=/api/v1/transactions code=401 status=UNAUTHORIZED error=ApiError[error=Unauthorized, message=A valid Bearer token is required, fields={}]
```

---

## 12. Docker

```mermaid
flowchart LR
    subgraph compose["docker compose"]
        APP["app<br/>:8080"]
        PG[("postgres:16-alpine<br/>:5432")]
        RD[("redis:7-alpine<br/>:6379")]
        VOL[/postgres-data volume/]
    end
    APP -->|DB_URL| PG
    APP -->|REDIS_HOST| RD
    PG --- VOL
    PG -. healthy .-> APP
    RD -. healthy .-> APP
```

```mermaid
flowchart LR
    A["maven:3.9-temurin-21<br/>mvn package"] -->|mistermath.jar| B["eclipse-temurin:21-jre-alpine<br/>non-root user"]
```

```bash
docker compose up --build        # everything
docker compose up postgres redis # only infra, run the app from the IDE
docker compose down -v           # stop and wipe data
```

---

## 13. CI/CD

```mermaid
flowchart LR
    PUSH([push / pull request to main]) --> CI
    subgraph CI["build-and-test"]
        A[checkout] --> B[JDK 21] --> C["mvn verify<br/>JUnit + Testcontainers + JaCoCo"] --> D[upload reports]
    end
    CI -->|push to main only| CD
    subgraph CD["docker"]
        E[login ghcr.io] --> F[metadata: latest + sha] --> G[build and push image]
    end
    G --> REG[(ghcr.io/jeysson123/mistermath)]
```

```bash
docker pull ghcr.io/jeysson123/mistermath:latest
```

---

## 14. Tests

```mermaid
pie title 109 JUnit 5 tests
    "Operations" : 19
    "Liskov (engines x operations)" : 28
    "Factory / Singleton / Strategy / Builder / Resolver" : 25
    "CQRS handlers" : 6
    "Controllers + advices (MockMvc)" : 13
    "JWT + filter" : 8
    "JPA + Redis adapters" : 6
    "Integration: full context + Testcontainers" : 4
```

```bash
mvn verify                         # tests + coverage (integration test needs Docker, skipped without it)
open target/site/jacoco/index.html # coverage report
```

---

## 15. Project structure

```text
src/main/java/com/math
├── MisterMathApplication.java
├── api
│   ├── advice          GlobalExceptionHandler, LoggingResponseAdvice
│   ├── controller      AuthController, CalculationController, TransactionController
│   ├── dto             ApiResponse<T>, ApiError, CalculationRequest, LoginRequest, TokenResponse
│   └── validation      @SupportedOperation, @SupportedPattern
├── application
│   ├── cqrs            Command, Query, CommandHandler, QueryHandler
│   ├── command         CalculateCommand, CalculateCommandHandler, CalculationResult
│   ├── query           Get...Query, Get...QueryHandler, TransactionView, TransactionViewMapper
│   ├── model           TransactionRecord
│   └── port            TransactionWriter/Reader, TransactionCacheWriter/Reader
├── domain
│   ├── exception       MathDomainException and friends
│   ├── operation       MathOperation, OperationType, Addition ... Power
│   └── pattern         CalculationPattern, CalculationEngine, CalculationEngineResolver
│       ├── singleton   SingletonCalculator, SingletonCalculationEngine
│       ├── factory     MathOperationFactory, FactoryCalculationEngine
│       ├── strategy    OperationStrategyContext, StrategyCalculationEngine
│       └── builder     Expression, BuilderCalculationEngine
└── infrastructure
    ├── cache           RedisTransactionCache, CacheProperties
    ├── persistence     TransactionEntity, TransactionJpaRepository, JpaTransactionStore
    └── security        SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, ...
```

| Tool | Used for |
|---|---|
| **Lombok** | `@RequiredArgsConstructor` (DI), `@Builder`, `@Data`, `@Getter`, `@Slf4j` |
| **Javadoc** | every class, in Spanish, with ASCII diagrams — `mvn javadoc:javadoc` |
| **Jakarta Validation** | `@NotNull`, `@NotBlank`, `@Digits`, `@Min`, `@Max`, `@Positive`, custom constraints |
