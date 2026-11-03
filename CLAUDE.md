# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Receipt Analyzer digitizes receipts (PDF via Tesseract OCR, or JSON exports) into wallets of expenses. A main goal is collecting versioned, human-corrected receipt data to train an ML model, so the revision history is a core concept, not an afterthought.

Two apps live in this repo:
- `receipt-analyzer-service/`: Java 21 / Spring Boot 3.5 backend, multi-module Gradle (Kotlin DSL, version catalog in `gradle/libs.versions.toml`)
- `web-client/`: Angular 20 standalone app with Angular Material + Bootstrap, auth via `keycloak-angular`

## Commands

Backend (run from `receipt-analyzer-service/`):
```bash
./gradlew build                      # compile all modules
./gradlew :monolith:bootJar          # build the runnable jar (what the Dockerfile does)
./gradlew :monolith:bootRun          # run locally (needs the env vars below)
./gradlew :monolith:test              # integration tests, need Docker running (Testcontainers)
./gradlew :monolith:test --tests '*ReceiptFacadeIT'          # single test class
```
Tests live in `monolith/src/test` (the only module with the Spring Boot application class). They are integration tests: `IntegrationTest` starts one `postgres:17` container with Testcontainers for the whole run (`@ServiceConnection` wires the datasource) and Flyway migrates it from scratch, so Docker must be running. Each test runs in a rolled-back transaction, uploads go to `monolith/build/test-uploads`, and Keycloak is never contacted. Call `flush()` before checking Hibernate changes with plain SQL. Receipt fixtures are in `monolith/src/test/resources/receipts`.

Frontend (run from `web-client/`):
```bash
npm start        # ng serve, app served at http://localhost:4200/home/
npm run build
npm test         # Karma/Jasmine
npx ng test --include='src/app/path/to/file.spec.ts'   # single spec
```

Local stack: `docker compose -f docker-compose.local.yml up` (from the repo root) starts PostgreSQL 17, Keycloak (`start-dev --import-realm`, served under `/auth`), Nginx on port 80 and the backend (port 8090, JDWP debug port 5005). Variables come from the root `.env` (gitignored). `init.sql` creates the `keycloak` and `receipt-analyzer-service` databases on the first start of the postgres container. `dev/realm-export.json` is the Keycloak realm (`activiti`, client `web-client`), imported on first start; it contains no users, so a fresh Keycloak needs a user created in the admin console. `dev/resetdb.sql` drops the app tables.

Backend env vars required by `adapters/src/main/resources/application.yml`: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KEYCLOAK_ISSUER_URI`, `KEYCLOAK_JWK_SET_URI`, `WEB_CLIENT_HOST`. The schema is versioned with Flyway: migrations live in `adapters/src/main/resources/db/migration` (`V<n>__<description>.sql`) and run on startup. Hibernate runs with `ddl-auto: validate`, so every entity change needs a new migration (never edit an applied one) or the app fails to start. The users table is `app_user` (`user` is reserved in PostgreSQL), and text comparisons in queries are case-sensitive in PostgreSQL, so searches use `LOWER(...)`. The OCR data path defaults to `/app/tesseract-data` (container path); outside Docker, point `ocr.tesseract.dataPath` at `receipt-analyzer-service/tesseract-data`.

## Request routing

The browser talks only to Nginx (`nginx.conf`): `/auth/` goes to Keycloak and `/api/` to the backend. The backend uses servlet context path `/api`, so controller mappings like `/wallets` are served at `/api/wallets`. In dev, `ng serve` proxies `/api/**` to `http://localhost/` (Nginx) through `proxy.conf.json`. The backend is a stateless OAuth2 resource server: every endpoint except `/actuator/health/**` requires a Keycloak JWT, and controllers identify the user by `jwt.getSubject()`.

## Backend architecture (hexagonal + CQRS)

Gradle modules and their dependency direction:
- `domain`: domain entities (`Receipt`, `ReceiptRevision`, `Wallet`, `ProductDict`, …) and **write-side** repository ports (`WalletRepository`, …). Depends only on Lombok. Entities and ports are mostly package-private.
- `app`: use cases. Each feature exposes a `*Facade`, plus **read-side** ports (`*QueryRepository`), factories and DTOs/requests/responses under `model/`. No Spring annotations: facades are plain classes.
- `adapters`: Spring web/JPA/security. REST controllers, JPA entities (`Sql*`), repository implementations, and the `*Configuration` classes that wire facades as `@Bean`s by hand.
- `monolith`: only `ReceiptAnalyzerServiceApplication` (the Spring Boot entry point), which depends on `adapters`.

Conventions that span modules:
- **Split packages by feature.** The same package (e.g. `pl.madzierski.daniel.wallet`) exists in `domain`, `app` and `adapters`. Facade methods, domain classes and ports are package-private, so a controller must live in the same feature package as the facade it calls. Keep new code inside the matching feature package in each module.
- **Write side:** a domain port such as `WalletRepository` is implemented in adapters by a package-private `*RepositoryImpl` that wraps a Spring Data `Sql*Repository` and maps through `SqlX.fromX(...)` / `toX()`.
- **Read side:** an app port such as `WalletQueryRepository` is implemented directly by a Spring Data interface (`SqlWalletQueryRepository extends WalletQueryRepository, Repository<…>`) whose JPQL `@Query` methods project straight into DTOs with `SELECT new …Dto(...)`. Changing a query DTO's constructor means updating these JPQL strings.
- `Sql*Query` entities are lightweight read/reference mappings; `*Query` domain models (e.g. `UserQuery`, `FileGroupQuery`) are id references to other aggregates.
- Every JPA entity extends `common.entity.BaseEntity` (adapters), which holds `id` (`@UuidGenerator`), `createdDate` and `modifiedDate` (Spring Data auditing). Subclasses use `@EqualsAndHashCode(callSuper = true)` so the id stays part of equality, and set the inherited fields with setters, since Lombok constructors and builders only cover a class's own fields.
- Errors: throw `AppRuntimeException` with a constant from `AppRuntimeExceptionMessages`. `GlobalExceptionController` turns it into the HTTP response.
- **Ownership (IDOR):** controllers take an `AuthenticatedUser user` parameter (app record holding `app_user.id`, resolved from the JWT `sub` by `security.AuthenticatedUserArgumentResolver` with a Caffeine cache) and pass it to the facade, which maps it to the domain `UserQuery` with `toUserQuery()` for ports and owner fields. Naming: in requests (controllers, facades, ports) it is `user`, because it is only the requester until the query proves ownership; in domain models and JPA entities the field is `owner` (`Receipt`/`FileGroup`: column `owner_id`, `Wallet`: column `user_id`). Ownership is part of the query (`findAccessible(id, user)`, `... AND r.ownerId = :ownerId`), never a separate check, and someone else's resource throws the same `*_NOT_FOUND` as a missing one. Children (revisions, items, files) are checked through their root.
- Circular facade dependencies are resolved with `@Lazy` in the configuration (e.g. `ProductDictFacade` in `ReceiptConfiguration`).

Features: `receipt`, `wallet`, `file_group` (uploaded files grouped per receipt, type detection via Tika), `product_dict` (product dictionary, aliases and categories; fuzzy matching threshold `product-dict.min-required-similarity`), `store`, `user`.

### Receipts, revisions and resolvers

A `Receipt` has many `ReceiptRevision`s, forming a tree through `parentReceiptRevision`. Each revision records the `resolver` (a `ReceiptResolverStrategyType`) that produced it, plus `isPreferredRevision` and `isCorrect` flags. Machine-produced revisions come from resolvers. User edits create user revisions (`UserResolver`), and only those are meant to be editable in the UI.

Parsing uses the strategy pattern in `app/.../receipt/scan_resolver`. `ReceiptResolverStrategy` implementations (`BiedronkaScanResolver` for PDF→OCR, `BiedronkaJsonResolver`, `UserResolver`) are registered as beans in `ReceiptConfiguration`. `ReceiptResolverLocatorService` then picks one by `strategy()`. To support a new store or format:
1. add an enum value to `ReceiptResolverStrategyType`;
2. implement the strategy;
3. register it as a bean;
4. optionally add a `receipt-resolver-strategy.<name>.version` property.

## Frontend structure

`src/app` has a flat layout. Route-level pages are top-level folders (`wallet-list`, `receipt-details`, …), reusable pieces live in `component/`, HTTP services in `service/` and interfaces in `model/`. `keycloak-angular`'s bearer interceptor attaches the token to every request (`app.config.ts`). The app is served under the `/home/` base path. Environment files hold `keycloakUrl` / `apiUrl`.

## Deployment

`dockhand/` contains the production compose file, nginx config, realm export and a `Jenkinsfile`. Jenkins first runs the integration tests (`Test` stage, `gradle:9.3.0-jdk21` image with the Docker socket mounted for Testcontainers; a failing test stops the pipeline), then builds the backend image and pushes it to a private registry as `receipt-analyzer-service:0.0.<build>` and `:latest`. The backend Dockerfile builds `:monolith:bootJar -x test` and installs `tesseract-ocr` in the runtime image.

## Commits

Branches are named after Jira-style tickets (e.g. `maintenance-3-rec-38`). Commit messages are short, lowercase and end with `closes REC-<n>`.
