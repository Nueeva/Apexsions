# AGENTS.md

# Apexsions — Universal Coding Agent Guidelines

> **Repository:** `Nueeva/Apexsions`
> **Primary Branch:** `main`
> **Project Type:** Minecraft Server Plugin Suite
> **Architecture:** Multi-plugin modular ecosystem
> **Brand:** `Apexsions`
> **Tagline:** `The Peak Civilizations`

This file defines **agent-agnostic engineering rules** for AI coding agents working in the Apexsions repository.

It intentionally does **not** duplicate the complete contents of `GEMINI.md`.

---

# 00. Purpose & Scope

`AGENTS.md` answers:

> **How should any coding agent safely and correctly work in this repository?**

It governs:

- repository safety;
- engineering practices;
- architecture discipline;
- implementation quality;
- testing and validation;
- dependency management;
- configuration safety;
- security;
- documentation synchronization;
- Git hygiene.

Project-specific facts, Gemini-specific behavior, official plugin definitions, rank hierarchy, exact technology baseline, and detailed Apexsions architecture belong in `GEMINI.md` and technical documentation.

---

# 01. Instruction Hierarchy

When instructions conflict, use this order:

1. Platform/system safety requirements.
2. Explicit user request.
3. Repository safety and data integrity.
4. `GEMINI.md` for Apexsions/Gemini-specific rules.
5. This `AGENTS.md` for universal engineering rules.
6. Existing source code and established architecture.
7. Developer convenience.

Never sacrifice repository integrity merely to complete a task faster.

---

# 02. Core Engineering Principles

Always prefer:

```text
Search
  ↓
Understand
  ↓
Reuse
  ↓
Extend
  ↓
Create only when necessary
```

Do not follow:

```text
Assume
  ↓
Rewrite
  ↓
Hope
```

Core principles:

1. Understand before modifying.
2. Preserve working behavior unless change is intentional.
3. Avoid duplicate implementations.
4. Keep changes focused and reviewable.
5. Respect existing architecture.
6. Prefer simple solutions over unnecessary abstraction.
7. Never invent repository facts when they can be verified.
8. Validate behavior, not merely compilation.
9. Preserve backward compatibility where practical.
10. Treat security and data integrity as first-class requirements.

---

# 03. Read Before You Change

Before modifying code:

1. Read `GEMINI.md`.
2. Read this `AGENTS.md`.
3. Read relevant technical documentation.
4. **Consult Knowledge Graph (`graphify query` / `graphify path`):** Search the persistent graph at `graphify-out/` to discover existing classes, call chains, and module dependencies before manual file inspection.
5. Locate the existing implementation.
6. Search for related classes, services, APIs, commands, events, and configuration.
7. Identify affected modules/plugins.
8. Identify consumers and dependencies.
9. Determine what must remain untouched.

Never implement a feature in isolation without checking whether equivalent functionality already exists.

## 03.1. File Discovery — Semantic Search First (MANDATORY)

This applies to the whole repository — both the Minecraft plugins (`Minecraft/`,
Java/Paper) and the web platform (`Website/`, PHP/Azuriom). The Knowledge Graph
indexes both.

When looking for an existing file, class, function, command, config key, or
feature, **use semantic search / the Knowledge Graph first**. Do not start by
glob-listing the whole tree or reading files blindly.

Preferred order:

```text
1. Knowledge Graph semantic search  → graphify query "<concept>" / graphify path "<A>" "<B>"
2. Content search                   → grep/ripgrep with include + path filters
3. Filename search                  → glob with a specific pattern
4. Read (targeted, 20–60 line window) via offset/limit
```

Rules:

- Before manually reading a file, ask the graph/search where the symbol lives.
- Search by concept (e.g. "grave item storage", "bounty payout", "mob stacking"),
  not only by exact identifier — the codebase may name things differently.
- Prefer one semantic/grep query over many glob+read cycles.
- After code changes, keep the graph fresh with `graphify update .`.
- Only fall back to broad recursive listing when semantic search returns nothing,
  and state clearly in the task summary that no graph/match was found.

Rationale: semantic search finds the real implementation and its callers in one
step, avoids duplicate implementations, and protects the token budget.

---

# 04. Repository & Workspace Safety

Work only inside the assigned repository/project boundary.

Do not:

- inspect unrelated projects;
- copy code from unrelated repositories without authorization;
- modify external Minecraft/server directories;
- deploy artifacts outside the repository;
- access unrelated credentials or secrets;
- modify operating-system configuration without explicit task requirements.

Do not silently overwrite another developer's work.

---

# 05. Git Safety

Apexsions is a multi-developer repository.

Before beginning work, inspect repository state and synchronize safely according to `GEMINI.md`.

At minimum:

```powershell
git status --short
git fetch origin
git log HEAD..origin/main --oneline
```

If the working tree is clean and remote commits exist, fast-forward when safe:

```powershell
git pull --ff-only origin main
```

If local changes exist, do not use destructive commands merely to synchronize.

Never automatically use:

```text
git reset --hard
git clean -fd
git restore
git checkout -- <file>
```

to discard work.

Never force-push a shared branch:

```text
git push --force
git push --force-with-lease
```

Before committing:

```powershell
git status --short
git diff --check
git diff
```

Before pushing, fetch again and check for newly arrived remote commits.

### [GIT SAFETY] — Revocation of Automatic Push Mandate
> [!CAUTION]
> **ANY PRIOR MANDATE REQUIRING AUTO-PUSH TO MAIN IS FORMALLY REVOKED AND VOID.**
> - **NEVER push directly to `main` or `master`.**
> - All work must be conducted on isolated feature/audit branches (`audit/YYYY-MM-roundN`, `feat/...`, `fix/...`) and submitted via Pull Request.
> - The ONLY exception is when the user explicitly types "push ke main" in the current prompt message.

### [EVIDENCE PER CLAIM]
- Every "VERIFIED" claim must be backed by concrete proof:
  * Commit SHA (if code changed).
  * Verbatim test output (not artificial summaries).
  * File path + exact line numbers verified.
  * Command logs or screenshots.
- Any claim lacking empirical proof automatically defaults to **UNVERIFIED**.

### [TEST INTEGRITY]
- **NEVER alter or weaken test files** simply to make failing tests pass.
- If a test file must legitimately be changed:
  * Provide line-by-line justification.
  * Show the test diff transparently.
  * Confirm that assertions were not weakened or neutralized.
- Run `git diff <base>..<head> -- tests/` and report the diff (must be empty or justified).

### [COVERAGE HONESTY]
- Never claim "100% PASS" without stating:
  * Total test count executed.
  * Coverage per package/file.
  * Missing tests for modified code paths.
  * Official coverage reports: Go (`go test -cover ./...`), JS/TS (Vitest/Jest coverage), PHP (`phpunit --coverage-text`).

### [BEHAVIOR TEST, BUKAN LIBRARY TEST]
- Tests must verify end-to-end behavior through application lifecycles, not merely unit wrappers around third-party libraries.

### [ITERATION LOG]
- Always track and preserve iterative progress: `OBSERVE` → `ANALYZE` → `HYPOTHESIZE` → `FIX` → `VERIFY` → `CHECK` → Result → Takeaways.
- Attach the complete iteration log in final reports rather than omitting it.

### [SELF-AUDIT LOOP]
- After every audit cycle, **MANDATORY Round-2: "Audit your own report"**.
- Round-2 searches for: unevidenced claims, tampered tests, uncommitted files, gap between findings and fixes, unlisted committed files, and weak coverage.
- Round-2 **MUST NEVER push anything**.

### [HONESTY OVER OPTIMISM]
- If uncertain, classify as **UNVERIFIED** or **PARTIAL**, never "VERIFIED".
- Explicitly state environment boundaries (e.g. SQLite local vs MySQL production).
- Transparently report failures; an honest failure is infinitely more valuable than an unverified success.

### Hosting SFTP Deploy Policy
- SFTP deployment (`.\deploy.cmd <Plugin>`) is only executed upon explicit user request or after branch PR approval.
- Never deploy unvalidated code to remote hosting panels.

---

# 06. Existing Architecture First

Respect existing module and plugin boundaries.

Before introducing a new:

```text
class
interface
service
manager
repository
listener
command
event
API
utility
configuration system
database abstraction
dependency
```

search for an existing equivalent.

Do not create parallel abstractions merely because the existing implementation is unfamiliar.

If the existing architecture is inadequate, explain the reason and make the smallest justified architectural change.

---

# 07. Responsibility & Dependency Boundaries

Every feature should have a clear owner.

Prefer:

```text
Public API
    ↓
Owning Module
    ↓
Implementation
```

over:

```text
Plugin A
    ↓
Plugin B internal implementation
```

Do not directly access another plugin's private database or internal implementation when a public contract exists or should exist.

Avoid circular dependencies.

If a circular dependency appears:

```text
STOP
↓
Inspect dependency graph
↓
Redesign the contract
```

Do not hide circular dependencies behind fragile workarounds.

---

# 08. Public APIs

Public APIs should be:

- minimal;
- intentional;
- understandable;
- stable;
- documented;
- thread-safe where applicable.

Do not expose implementation details unnecessarily.

Before changing a public API:

1. Search all consumers.
2. Determine compatibility impact.
3. Identify whether the change is breaking.
4. Update relevant documentation.
5. Provide a migration path when practical.

---

# 09. Threading & Async Work

Never perform blocking database, filesystem, or network operations on the Minecraft main thread.

Long-running or blocking work should be asynchronous where appropriate.

Bukkit/Paper game-state mutations must occur on the correct server thread.

When an API's thread-safety contract is unclear, inspect the API/library documentation and existing usage before implementing.

Do not introduce concurrency merely for complexity's sake.

---

# 10. Database & Persistence

Use the repository's existing database abstraction.

Database code must:

- use parameterized queries;
- respect transaction boundaries;
- avoid connection leaks;
- handle failures explicitly;
- avoid blocking the main thread;
- preserve data integrity;
- respect concurrency requirements.

Do not introduce a second persistence framework without architectural justification.

Never silently destroy or overwrite persistent data.

---

# 11. Transaction & Concurrency Safety

For state-changing operations:

```text
Validate
  ↓
Acquire appropriate consistency boundary
  ↓
Perform atomic operation
  ↓
Commit
  ↓
Publish resulting state/event
```

Consider:

- race conditions;
- duplicate requests;
- stale state;
- retries;
- partial failure;
- transaction ordering;
- idempotency.

Do not assume that single-threaded-looking code is automatically race-free.

---

# 12. Security

Treat all externally supplied data as untrusted.

Validate at appropriate boundaries:

- commands;
- GUI input;
- configuration;
- network/API input;
- serialized data;
- player-supplied values.

Prevent:

- SQL injection;
- command injection;
- path traversal;
- unsafe deserialization;
- privilege escalation;
- secret leakage;
- unintended administrative bypasses.

Never hardcode credentials, API keys, or private secrets.

Do not weaken security to simplify development.

---

# 13. Configuration

Configuration is part of the public behavior of a plugin.

When adding or changing configuration:

1. Use the existing configuration architecture.
2. Provide sensible defaults.
3. Validate values.
4. Preserve compatibility where practical.
5. Document new keys.
6. Avoid silently changing existing configuration semantics.

Never document configuration that the implementation does not actually support.

---

# 14. Commands & Permissions

Commands must have:

- clear ownership;
- validated arguments;
- appropriate permission checks;
- correct player/console restrictions;
- useful failure messages;
- safe handling of malformed input.

Before adding a permission node or command:

```text
Search existing conventions
↓
Reuse if appropriate
↓
Create only when necessary
```

Documentation must match the actual registered command and permission.

### Command & GUI Synchronization Rule (MANDATORY)

Whenever a command is **added, updated, modified, or removed**:
1. **Audit Related GUIs**:
   - Inspect all Master Admin GUIs (`MasterAdminGUI`, `PlayerInspectorGUI`, `PlayerManagerGUI`, and all module sub-GUIs).
   - Inspect any user-facing GUIs that execute or represent that command/feature.
2. **Synchronize Click Handlers**:
   - Ensure GUI buttons execute the updated command syntax or underlying service directly.
   - Remove or adapt obsolete command executions from GUIs.
3. **Verify Tab Completion & Help Messages**:
   - Ensure `onTabComplete` and command help messages reflect the exact current command signatures.

---

# 15. Dependencies

Before adding a dependency:

1. Search existing dependencies for equivalent functionality.
2. Check compatibility.
3. Check runtime implications.
4. Check licensing where applicable.
5. Determine whether the dependency should be hard or optional.
6. Update build configuration.
7. Update documentation when relevant.

Do not add libraries merely to avoid writing a small amount of maintainable code.

---

# 16. Error Handling & Logging

Do not silently swallow exceptions.

Errors should:

- preserve useful diagnostic context;
- use existing project logging conventions;
- avoid leaking secrets;
- distinguish expected failures from programming errors.

Do not spam logs for normal behavior.

Do not hide serious failures merely to make the server appear healthy.

---

# 17. Testing & Validation

Compilation is not equivalent to correctness.

After modifying code:

1. Compile the affected component.
2. Run the narrowest relevant tests.
3. Validate behavior when practical.
4. Expand validation if the change crosses module boundaries.
5. Review warnings and errors.
6. Review the final diff.

Prefer targeted validation over unnecessarily expensive full-project builds.

Follow project-specific build instructions from `GEMINI.md`.

### Token Conservation & Strict Quota Reduction Protocol (MANDATORY)
Untuk mencegah pemborosan kuota token dan menjaga efisiensi context window:
1. **DILARANG Headless Browser & Playwright Loop:** Dilarang keras menggunakan `browser_subagent` atau otomasi browser secara berlebihan/berulang-ulang. Gunakan validasi ringan: inspeksi kode, syntax check (`php -l`), targeted unit test, atau HTTP fetch/cURL ringkas. Otomasi browser hanya boleh digunakan jika diminta eksplisit oleh user atau untuk 1x final verification ringkas tanpa spam screenshot.
2. **Batasi Jangkauan Baca Berkas (`view_file`):** Dilarang melihat berkas secara utuh (ratusan baris) jika hanya membutuhkan bagian tertentu. Selalu gunakan `StartLine` dan `EndLine` dengan rentang terfokus (20–60 baris).
3. **Pencarian Terarah (`grep_search`):** Hindari pencarian global tanpa filter. Selalu tentukan subfolder spesifik atau ekstensi berkas untuk mencegah luapan hasil.
4. **Pembatasan Output Terminal (`run_command`):** Batasi perintah terminal yang berpotensi menghasilkan ribuan baris log menggunakan paging, filtering (`head -n 30`, `Select-Object -First 30`, `--short`, `--stat`).
5. **Komunikasi Ringkas & Padat:** Hilangkan basa-basi percakapan, pengantar berulang, atau mengulang ringkasan yang sudah tercantum di artefak/dokumentasi. Langsung laporkan poin inti perubahan, hasil verifikasi, dan status commit.
6. **Local-First Testing:** Uji dan validasi sintaks serta dependensi secara lokal sebelum menyentuh VPS produksi.
7. **Atomic Batch Execution:** Satukan perbaikan yang saling terkait dalam satu siklus terencana, hindari bolak-balik eksekusi sebaris demi sebaris yang menghabiskan turn context.
8. **Knowledge Graph-First Exploration:** Manfaatkan `graphify query "<keyword>"` dan `graphify path "<A>" "<B>"` untuk memahami arsitektur dan relasi pemanggilan sebelum membaca file besar secara manual. Sinkronkan graf dengan `graphify update .` pasca-perubahan kode (AST lokal tanpa kuota token).

---

# 18. Performance

Do not optimize blindly.

Before introducing an optimization, identify the actual bottleneck or architectural reason.

Avoid:

- unnecessary allocations in hot paths;
- repeated database queries;
- blocking operations on main thread;
- unbounded collections;
- excessive event/listener work;
- unnecessary synchronization.

Prefer measurable improvements over speculative complexity.

---

# 19. Backward Compatibility

When changing existing behavior, consider:

```text
Existing users
Existing configuration
Existing data
Existing API consumers
Existing commands
Existing permissions
Existing integrations
```

Breaking changes require explicit justification.

When practical, use:

```text
Deprecate
  ↓
Provide replacement
  ↓
Migration path
  ↓
Remove later
```

Do not silently break consumers.

---

# 20. Documentation Governance

Documentation is part of implementation when behavior or architecture changes.

When a change affects:

```text
Features
Architecture
API
Events
Commands
Permissions
Configuration
Database
Dependencies
Build workflow
Versions
Project structure
```

perform a documentation impact review.

Relevant documentation may include:

```text
README.md
DOKUMENTASI.md
GEMINI.md
AGENTS.md
docs/
docs/placeholders/
architecture/
examples/
CHANGELOG
CONTRIBUTING
```

### PlaceholderAPI Documentation Synchronization
The `docs/placeholders/` directory contains per-plugin PlaceholderAPI reference documents (`ApexsionsCore.md`, `ApexsionsEconomy.md`, `ApexsionsFishing.md`, etc.) and `docs/placeholders/README.md`. Whenever placeholders are added, modified, or removed, always update the corresponding file in `docs/placeholders/` so that placeholder names, types, outputs, and integration guides match code implementation 100%.

Only update documentation that is actually affected.

Do not blindly rewrite every markdown file.

---

# 21. Separation of Documentation Responsibilities

Maintain clear responsibilities:

```text
AGENTS.md
→ Universal coding-agent engineering rules

GEMINI.md
→ Gemini-specific workflow + Apexsions-specific governance and project baseline

README.md
→ Human onboarding and project overview

DOKUMENTASI.md
→ Detailed technical documentation

docs/
→ Detailed subsystem/API/architecture documentation
```

Do not turn `AGENTS.md` into a duplicate copy of `GEMINI.md`.

Do not turn `README.md` into an internal engineering manual.

Do not turn `GEMINI.md` into a copy of every technical document.

---

# 22. Project Evolution

Do not assume the current architecture is permanent.

The repository may gain:

- new plugins;
- new APIs;
- new integrations;
- new database systems;
- new commands;
- new configuration;
- new dependencies;
- new Minecraft/Paper versions;
- new development workflows.

When introducing a significant new system, evaluate:

```text
Ownership
Dependencies
API/Event boundaries
Configuration
Persistence
Security
Performance
Testing
Build integration
Documentation
```

The correct response to project growth is controlled evolution, not uncontrolled duplication.

---

# 23. New Plugin Decision

A new plugin should only be created when the responsibility genuinely warrants a new module.

Before creating one:

```text
Does the functionality already exist?
        ↓
Can an existing plugin own it?
        ↓
Would a new plugin improve separation of responsibility?
        ↓
What dependencies does it require?
        ↓
What APIs/events does it expose?
        ↓
What configuration/data does it own?
        ↓
What documentation becomes affected?
```

Do not create a plugin simply to avoid touching existing code.

---

# 24. Architecture Changes

For substantial architecture changes:

```text
Current State
    ↓
Impact Analysis
    ↓
Target State
    ↓
Migration Strategy
    ↓
Implementation
    ↓
Validation
    ↓
Documentation Synchronization
```

Document the reason for significant architectural decisions when appropriate.

Do not leave obsolete architecture documentation presented as current.

---

# 25. Technology Upgrades

For major version or dependency upgrades:

```text
Inspect current baseline
        ↓
Assess compatibility
        ↓
Identify affected components
        ↓
Update build configuration
        ↓
Update code only where necessary
        ↓
Build
        ↓
Test / Validate
        ↓
Update documentation
```

Do not upgrade dependencies merely because a newer version exists.

Project-specific technology versions belong in `GEMINI.md` and technical documentation, not duplicated here unless the universal rule itself depends on that version.

---

# 26. Scope Discipline

Keep changes focused.

Do not include unrelated:

- refactors;
- formatting;
- renames;
- dependency upgrades;
- configuration changes;
- architecture changes.

If a task requires a broader change, explain why the broader scope is necessary.

A good change should be understandable from its diff.

---

# 27. Generated Artifacts

Follow repository-specific instructions for generated files.

Do not manually edit generated artifacts when they are reproducible from source.

Do not place build artifacts into external server directories unless explicitly authorized.

When generated artifacts are intentionally version-controlled, ensure they correspond to the current source.

---

# 28. Documentation-Only Tasks

If the user requests documentation-only work:

```text
Do not modify runtime source code.
Do not silently change behavior.
Do not "fix" unrelated implementation.
```

If documentation cannot accurately describe the implementation because the implementation is contradictory or unclear:

```text
STOP
↓
Report the inconsistency
↓
Do not silently change runtime behavior
```

---

# 29. Final Diff Review

Before declaring work complete:

```powershell
git status --short
git diff --stat
git diff
git diff --check
```

Confirm:

```text
[ ] Intended files changed
[ ] No unrelated files changed
[ ] No accidental generated files
[ ] No secrets
[ ] No destructive changes
[ ] Architecture remains coherent
[ ] Documentation is synchronized
```

---

# 30. Final Validation Checklist

Before completion:

```text
[ ] Existing implementation inspected
[ ] Existing abstractions reused where appropriate
[ ] Dependencies checked
[ ] Threading checked
[ ] Security checked
[ ] Configuration checked
[ ] Database impact checked
[ ] Commands/permissions checked
[ ] Tests/build performed as appropriate
[ ] Documentation impact reviewed
[ ] Git diff reviewed
[ ] Remote state checked before push
```

---

# 31. Final Agent Behavior

When uncertain:

```text
Verify
```

not:

```text
Guess
```

When an existing abstraction exists:

```text
Reuse
```

not:

```text
Duplicate
```

When a change is risky:

```text
Stop and inspect
```

not:

```text
Proceed and hope
```

When documentation is stale:

```text
Synchronize
```

not:

```text
Ignore
```

When a requested change conflicts with repository safety:

```text
Protect the repository
```

---

# 32. Relationship With GEMINI.md

`GEMINI.md` is the authoritative location for:

- Gemini-specific instructions;
- Apexsions project identity;
- official plugin suite;
- rank hierarchy;
- project-specific technology baseline;
- exact build commands;
- detailed inter-plugin architecture;
- project-specific Git workflow;
- long-term Apexsions governance;
- project-specific documentation rules.

`AGENTS.md` provides the reusable engineering foundation underneath those project-specific rules.

The two files should be **complementary, not duplicates**.

If either file changes, check the other for contradictions, but do not copy the entire document.

---

# 33. Golden Rule

> **Understand the repository before changing it, make the smallest correct change, validate the result, protect other developers' work, and leave the repository more coherent than you found it.**
