# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DataEase is an open-source BI tool by FIT2Cloud for data analysis and visualization via drag-and-drop chart creation. Version 3.0.0, GPL v3 license.

**Branch:** `dev-v3` (current), PRs target `dev-v2`

## Tech Stack

- **Backend:** Java 21, Spring Boot 3.3.x, JPA (Hibernate) + QueryDSL, JDK 21
- **Frontend:** Vue 3, TypeScript, Vite, Element Plus, ECharts, AntV (G2, L7, S2)
- **DB:** MySQL 8 (primary), also PostgreSQL, Oracle, SQL Server, DM, Kingbase, GreatSQL, H2 (desktop)
- **Other:** Knife4j (API docs), EhCache, Quartz

## Module Structure

```
dataease/
├── core/
│   ├── core-backend/    # Spring Boot app (main artifact: CoreApplication.jar)
│   └── core-frontend/   # Vue 3 SPA
├── sdk/                 # Shared SDK (common, api, extensions, distributed)
├── de-xpack/            # Enterprise/premium features (separate git repo)
├── drivers/             # JDBC drivers
└── installer/           # Docker Compose, deployment scripts
```

## Backend Domain Structure

Each business domain under `core/core-backend/src/main/java/io/dataease/<domain>/`:

```
<domain>/
├── dao/auto/entity/      # JPA @Entity classes
├── auto/repository/      # JpaRepository interfaces
├── dao/ext/po/           # QueryDSL projection POJOs
├── dao/ext/mapper/       # Custom repository interfaces
├── dto/                  # Data Transfer Objects
├── bo/                   # Business Objects
├── manage/               # Business logic (@Component)
├── server/               # API implementations (implements sdk interfaces)
├── request/              # Request body classes
└── utils/                # Domain utilities
```

## Key Commands

### Build

```bash
# Full build (all modules)
mvn clean install

# Build community standalone JAR
cd core && mvn clean package -Pstandalone -U -Dmaven.test.skip=true
# Output: core/core-backend/target/CoreApplication.jar

# Build desktop edition
cd core && mvn clean package -Pdesktop -U -Dmaven.test.skip=true

# Build enterprise/distributed edition
cd core && mvn clean package -Pdistributed -U -Dmaven.test.skip=true
```

### Run

```bash
# Direct JAR execution
java -jar core/core-backend/target/CoreApplication.jar

# Docker (recommended for testing)
curl -sSL https://dataease.oss-cn-hangzhou.aliyuncs.com/quick_start_v2.sh | bash
# Default credentials: admin / DataEase@123456

# Local installer
cd installer && /bin/bash install.sh
```

### Frontend Dev

```bash
cd core/core-frontend
npm run dev          # Dev server
npm run build:distributed  # Production build
```

### QueryDSL Regeneration

```bash
mvn compile  # Regenerates Q-classes in target/generated-sources/java/
```

## Build Profiles

| Profile | Database | Description |
|---------|----------|-------------|
| `standalone` (default) | MySQL | Community/single-server |
| `desktop` | H2 | Desktop/lightweight |
| `distributed` | MySQL | Enterprise/multi-node |

Database-specific profiles: `standalone-oracle`, `standalone-pg`, `standalone-dm`, `standalone-kingbase`, `standalone-sqlserver`, `standalone-GreatSQL`

## Critical Dev Conventions

### JPA > MyBatis-Plus

MyBatis-Plus has been replaced by JPA. **Do not use MyBatis-Plus patterns.**

- Use `jakarta.persistence.*` (not `javax.persistence.*`)
- Entities: `@Getter`/`@Setter` via Lombok (not `@Data`), `@Comment` on table and every field
- IDs: `Long`, no `@GeneratedValue` — IDs generated manually via `SnowflakeUtil`
- Repositories: `JpaRepository<Xxx, Long>`, `JpaSpecificationExecutor<Xxx>`

### JpaUpdateNonNullAspect

Intercepts all `save`/`saveAndFlush` calls:
- If entity exists (non-null ID), **only non-null fields are copied** before saving — mimics MyBatis-Plus `updateById`
- To intentionally set a field to null: use QueryDSL `@Modifying` + JPQL, `save()` cannot do this

### Multi-DB Compatibility

All QueryDSL/JPQL queries **must work across all supported databases** (MySQL, Oracle, PostgreSQL, SQL Server, DM, Kingbase, GreatSQL). Avoid database-specific dialect functions; branch per-dialect when necessary (see `DynamicCaseNamingStrategy`).

### Layering

- **server/** = API endpoint implementation, implements interfaces from `sdk` module
- **manage/** = business logic, `@Component`, called by server
- **dao/auto/** = JPA entities + repositories
- **dao/ext/** = custom query POJOs and non-standard repositories

### ID Generation

Use `SnowflakeUtil` for all new entity IDs.

## Architecture Notes

- **Profile-based feature toggling:** Three Maven profiles control which modules are included (community vs enterprise)
- **Frontend bundled into JAR:** `maven-antrun-plugin` copies frontend dist into backend `static/`
- **Schema management:** Flyway disabled, `ddl-auto: update` manages schema in dev and production
- **xpack independence:** de-xpack is a separate git repo; core-backend optionally depends on it for debugging
