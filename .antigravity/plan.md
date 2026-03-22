# PayRoute — Agent Execution Plan
> **Runtime Instruction File** | Model: Claude Opus 4.6
> When this file is run by an agent, execute all steps below sequentially and produce `implemented.md`.

---

## Agent Identity & Context

You are a senior software engineer and technical documentation specialist working on **PayRoute** — an Intelligent Payment Orchestration Engine built with Java, Spring Boot, Apache Kafka, PostgreSQL, Redis, Docker, and Kubernetes.

Your job is to **deeply analyze the entire codebase** and produce a single `implemented.md` file that captures the complete state of the project. This file must be detailed enough that any developer or AI agent — with zero access to the codebase — can fully understand, continue, debug, or extend the project.

---

## ⚡ FIRST ACTION — Check for `implemented.md` (Decision Gate)

**Before doing anything else**, check whether `implemented.md` exists at the project root.

```
CHECK: Does ./implemented.md exist?
```

### → If `implemented.md` does NOT exist → Run in CREATE MODE

1. Print: `[plan.md] implemented.md not found. Running in CREATE MODE.`
2. Proceed through **STEP 1 → STEP 10** sequentially (full codebase scan).
3. At STEP 10, write a brand new `implemented.md` from scratch using the full output format defined below.

---

### → If `implemented.md` ALREADY EXISTS → Run in UPDATE MODE

1. Print: `[plan.md] implemented.md found. Running in UPDATE MODE.`
2. **Read the entire existing `implemented.md` file into memory first** — before scanning a single code file.
3. Extract the `Last updated:` timestamp from line 2 of the file. This becomes your **baseline date**.
4. Print: `[plan.md] Baseline date: {BASELINE_DATE}. Scanning only files changed since then.`
5. Run **STEP 1 → STEP 9** with a **change filter** applied:
   - For each file discovered in STEP 1, check its last-modified timestamp
   - If modified **after** the baseline date → **re-analyze fully**
   - If last modified **before or on** the baseline date → **skip re-analysis**, carry forward the existing documented state from the file you already read
6. At STEP 10, **do not overwrite the file**. Instead make surgical updates:
   - For each section where something changed → replace only that section's content
   - For sections with no changes → leave them exactly as they are, word for word
   - Update the `Last updated:` timestamp on line 2 to today's date
   - Append a new entry under `## Changelog` at the bottom (create the section if it does not exist yet)

> **The golden rule for UPDATE MODE:**
> You read `implemented.md` first so you carry full project memory from the previous run.
> Every update builds on top of what was already documented.
> Nothing is ever deleted unless the corresponding code was actually deleted.

---

## Execution Steps

### STEP 1 — Discover Project Structure

Scan and map the full directory tree of the project. Identify:
- All top-level modules and their purpose
- Package structure within each module
- Configuration files (pom.xml, application.yml, docker-compose.yml, Dockerfile, etc.)
- CI/CD pipeline files (GitHub Actions, Jenkinsfile, etc.)
- Test directories and test file counts
- Any README or documentation files already present

```
Scan: ./
Output: Full directory tree with annotations
```

---

### STEP 2 — Analyze Core Implementation

For each module/service found, read and analyze:

**a) Domain Models & Entities**
- All `@Entity` classes → field names, types, constraints, relationships
- All DTOs, request/response models
- Enums and their values

**b) Service Layer**
- All `@Service` classes → method signatures, business logic summary, dependencies injected
- Key algorithms (routing strategies, retry logic, idempotency checks)

**c) Repository Layer**
- All `@Repository` interfaces → custom query methods, named queries

**d) Controller / API Layer**
- All `@RestController` classes → endpoint paths, HTTP methods, request/response types
- Validation rules applied
- Swagger/OpenAPI annotations if present

**e) Kafka Configuration**
- Topics defined (names, partitions, replication)
- Producer configs
- Consumer configs and listener methods

**f) Redis Configuration**
- Connection setup
- Key patterns used
- TTL values set
- What data is cached/stored

**g) Database Configuration**
- DataSource config
- Flyway/Liquibase migration files (list each migration and what it does)
- JPA/Hibernate settings

**h) Security Configuration**
- Any Spring Security setup
- OWASP measures implemented
- API key / auth mechanisms

**i) Monitoring & Observability**
- Custom Prometheus metrics defined (counters, histograms, gauges)
- Actuator endpoints enabled
- Grafana dashboard configs
- OpenTelemetry / tracing setup

---

### STEP 3 — Extract Architecture Decisions

Search for comments, README notes, or code patterns that reveal **why** decisions were made. Look for:
- Comments starting with `// DECISION:`, `// NOTE:`, `// WHY:`, `// REASON:`
- README sections explaining design choices
- Strategy pattern implementations — document which strategies exist and how selection works
- Error handling patterns — document how exceptions are structured and propagated
- Any `@deprecated` methods with notes

Also infer architectural decisions from the code itself. For example:
- If Redis TTL is 24 hours on idempotency keys → document that decision
- If retry max attempts is 3 → document that
- If a specific routing strategy is the default → document that

---

### STEP 4 — Scan for TODOs, FIXMEs, and Gaps

Search the entire codebase for:
```
// TODO
// FIXME  
// HACK
// XXX
// PENDING
// NOT IMPLEMENTED
```

For each one found, record:
- File path and line number
- The full comment text
- Severity (critical / important / minor) based on context

Also identify **unimplemented stubs** — methods that throw `UnsupportedOperationException` or return `null`/empty without logic.

---

### STEP 5 — Analyze Test Coverage

Scan the test directory and for each test class record:
- What unit/component is being tested
- Test method names and what scenario each covers
- Mocks used
- Whether Testcontainers is used (real DB/Kafka/Redis tests)

Produce a coverage summary:
- List of classes/services that HAVE tests
- List of classes/services that are MISSING tests
- Estimated coverage level (high / medium / low / none) per module

---

### STEP 6 — Capture Configuration State

Extract and document ALL configuration values from:
- `application.yml` / `application.properties`
- `application-dev.yml`, `application-prod.yml` (any profiles)
- `docker-compose.yml` — all services, ports, volumes, env vars
- `Dockerfile` — base image, build steps, exposed ports
- Kubernetes manifests — deployments, services, configmaps, secrets structure
- GitHub Actions / CI pipeline — triggers, steps, what runs on each stage

Flag any configuration values that are hardcoded but should be externalized as env variables.

---

### STEP 7 — Map API Contracts

For every REST endpoint in the project, document:

| Method | Path | Request Body | Response Body | Status Codes | Auth Required |
|--------|------|-------------|---------------|-------------|---------------|

Also document Kafka event schemas:
- Topic name
- Event class / JSON structure
- Producer (which service sends it)
- Consumer (which service receives it)

---

### STEP 8 — Summarize Build & Run Instructions

From Dockerfiles, pom.xml, and any scripts, extract:
- How to build the project locally
- How to run with Docker Compose
- How to run tests
- How to run a specific module
- Any environment variables required before running
- Known issues or prerequisites (Java version, Docker version, etc.)

---

### STEP 9 — Determine Project Completion Status

Based on everything analyzed, assess each major feature area:

| Feature | Status | Notes |
|---------|--------|-------|
| API Gateway | ✅ Complete / 🔄 In Progress / ❌ Not Started | ... |
| Routing Strategies | ... | ... |
| Idempotency (Redis) | ... | ... |
| Provider Adapters | ... | ... |
| Kafka Event Streaming | ... | ... |
| Retry & Fallback | ... | ... |
| PostgreSQL Persistence | ... | ... |
| Unit Tests | ... | ... |
| Integration Tests | ... | ... |
| Docker Compose | ... | ... |
| Kubernetes Manifests | ... | ... |
| CI/CD Pipeline | ... | ... |
| Prometheus Metrics | ... | ... |
| Grafana Dashboard | ... | ... |

---

### STEP 10 — Generate `implemented.md`

Using everything gathered in Steps 1–9, write the `implemented.md` file.

**Follow the exact output format defined in the `## implemented.md Format` section below.**

Save the file as `implemented.md` in the project root.

---

## implemented.md Format

The output file must follow this exact structure:

```markdown
# PayRoute — Implementation State
> Last updated: {CURRENT_DATE}
> Generated by: plan.md agent scan
> Model: Claude Opus 4.6

---

## 1. Project Overview
[2–3 paragraph summary of what the project is, what it does, and its current state]

---

## 2. Directory Structure
[Full annotated tree]

---

## 3. Module Breakdown

### {Module Name}
**Purpose:** ...
**Package root:** `com.payroute.{module}`
**Status:** Complete / In Progress / Stub

#### Entities / Models
[Table or list of all domain classes with fields]

#### Services
[Each service class with method summary]

#### Controllers / Kafka Listeners
[Each endpoint or listener with path/topic, method, input, output]

#### Repositories
[Each repo with custom methods listed]

[Repeat for each module]

---

## 4. Architecture Decisions Log

| Decision | What Was Chosen | Why | Where in Code |
|----------|----------------|-----|----------------|
| ... | ... | ... | ... |

---

## 5. API Contract Reference

### REST Endpoints
[Full table of all endpoints]

### Kafka Event Schemas
[Full table of all topics and event structures]

---

## 6. Configuration Reference

### application.yml (active values)
[Key-value table of all config]

### docker-compose.yml Services
[Each service: image, ports, volumes, env vars]

### Environment Variables Required
[List of all env vars the app needs to run]

---

## 7. Test Coverage Map

### Covered
[List of tested classes and what is tested]

### Not Covered
[List of classes with no tests]

---

## 8. TODO / FIXME Tracker

| File | Line | Type | Description | Severity |
|------|------|------|-------------|----------|
| ... | ... | TODO | ... | ... |

---

## 9. Feature Completion Status

[The status table from Step 9]

---

## 10. How to Run

### Prerequisites
...

### Local Development
```bash
# Build
...
# Run
...
# Test
...
```

### Docker Compose
```bash
...
```

---

## 11. What to Build Next

Based on the current implementation state, the recommended next steps in priority order are:

1. **[Next Feature]** — [Why it should be next, what files to create/modify]
2. ...

---

## 12. Known Issues & Risks

| Issue | Severity | Affected Area | Suggested Fix |
|-------|----------|---------------|---------------|
| ... | ... | ... | ... |
```

---

## Agent Rules

Follow these rules strictly during execution:

1. **Check first, always** — the very first action is checking whether `implemented.md` exists. Never skip this gate.
2. **Read before you write (UPDATE MODE)** — if `implemented.md` exists, read it completely into memory before scanning a single code file. Never overwrite without reading first.
3. **Never hallucinate** — only document what actually exists in the code. If a feature is not found, mark it as `❌ Not Started`.
4. **Be exhaustive** — missing a class or method from `implemented.md` defeats the entire purpose. Scan every file.
5. **Preserve exact names** — use the exact class names, method names, field names, and package paths found in code. Do not paraphrase them.
6. **Flag ambiguities** — if you find code that is unclear or seems broken, note it under Known Issues.
7. **No summaries during gathering** — during Steps 1–9, collect raw data only. Synthesize only at Step 10 when writing.
8. **Never delete existing content without cause** — in UPDATE MODE, only remove a documented item if its corresponding code was actually deleted from the codebase.
9. **Mark confidence** — for any section where you are inferring rather than reading directly from code, add `[INFERRED]` so the next developer knows.
10. **Always append changelog** — every run that modifies `implemented.md` must leave a dated changelog entry at the bottom.

---

## Mode Summary

| Condition | Mode | What Happens |
|-----------|------|--------------|
| `implemented.md` does not exist | **CREATE** | Full scan, full write from scratch |
| `implemented.md` exists | **UPDATE** | Read existing file first, rescan only changed files, patch affected sections, append changelog |

The decision gate is always the **very first action**. See the `⚡ FIRST ACTION` section at the top of this file for the full logic.

---

## Success Criteria

The agent run is successful when:
- [ ] `implemented.md` is created (or updated) in the project root
- [ ] Every module has a corresponding section with entities, services, and controllers documented
- [ ] The Feature Completion Status table is filled with accurate statuses
- [ ] The TODO Tracker contains all `// TODO` and `// FIXME` comments found
- [ ] The API Contract Reference covers every REST endpoint and Kafka topic
- [ ] "What to Build Next" contains at least 3 prioritized next steps