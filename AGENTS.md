# AGENTS.md

This document formalizes the de facto rules of the `kenetre` repository for human and AI contributors.

> Scope: rules inferred from the Maven structure, module POM files, and patterns visible in code/tests.

## 1) Project context

- Java multi-module Maven project (`packaging=pom` at the root).
- Target Java version: `17` (`maven.compiler.source/target/release=17`).
- Main modules: `kenetre-core`, `kenetre-bio`, `kenetre-illumina`, `kenetre-nanopore`, `kenetre-it`, etc.

## 2) Style and quality rules

### Mandatory before commit

- Java code must pass `spotless:check` (triggered during `compile`).
- Expected formatting is `google-java-format` (version `1.24.0`) with import ordering and removal of unused imports.
- Code must pass `modernizer-maven-plugin` (phase `verify`) with build failure on violations.
- Use English for code and documentation:
  - Source identifiers and API naming should be English when introducing new code.
  - Comments, JavaDoc, and user/developer documentation must be written in English.
  - Keep existing legal/license texts unchanged when they are provided in another language.

### Recommended

- Keep existing license headers on modified/created Java files.
- Stay consistent with the historical project style (JavaDoc on public APIs, explicit naming).
- Use argument guards (`requireNonNull`, simple validations) in critical utilities.

## 3) Module and resource structure

### Structure conventions

- Java sources: `src/main/java`.
- Java tests: `src/test/java`.
- Embedded resources (frequent pattern): `src/main/java/files`.
- Test resources: `src/test/java/files`.
- SPI/service resources: `src/main/java/META-INF` (copied to `META-INF` in the jar).

### SPI / ServiceLoader

- Files under `src/main/java/META-INF/services/<fqcn-interface>`.
- Content: implementation classes (FQCN), one per line.
- `# ...` comments are accepted and used.

## 4) Dependencies and compatibility

- Centralize versions in the root POM when possible (`guava.version`, `junit.version`, `htsjdk.version`, `poi.version`).
- Prefer internal dependencies via `${project.version}` between `kenetre-*` modules.
- Keep Java 17 compatibility (no API requiring Java > 17).
- Any update of historical dependencies must be justified and tested module by module.

## 5) Tests

- Main framework: JUnit 4.
- Module `kenetre-it` also uses TestNG for some IT classes.
- Keep Maven-detectable test class names (`*Test`).
- Update test data in `src/test/java/files` when parsing/format behavior changes.

## 6) Local build: recommended workflow

Run from the repository root:

```bash
mvn clean verify
```

Fast development loop:

```bash
mvn -DskipTests compile
mvn test
```

Formatting check only:

```bash
mvn spotless:check
```

## 7) Changelog and releases

- `CHANGELOG.md` is **automatically managed by [release-please](https://github.com/googleapis/release-please)** via a GitHub Actions workflow. **Do not edit `CHANGELOG.md` manually.**
- Releases are tagged `vX.Y.Z`.
- `CHANGELOG.md` follows a semver-like structure with sections `Features`, `Bug Fixes`, `Miscellaneous Chores`; these entries are generated from conventional commit messages.

## 8) Practical rules for AI agents

### Do

- Keep change scope limited to the relevant module.
- Strictly follow existing folder structure (`files`, `META-INF/services`).
- Check cross-module impact before adding a dependency.
- Prefer minimal and explicit fixes.

### Don't

- Do not migrate the test stack (JUnit 4/TestNG) without explicit request.
- Do not raise the target Java version without explicit request.
- Do not remove historical license headers.
- Do not move test resources out of `src/test/java/files`.
- **Do not edit `CHANGELOG.md`** — it is automatically updated by `release-please` during the GitHub Actions release workflow.

## 9) Quick PR checklist

- [ ] Full Maven build is green (`mvn clean verify`).
- [ ] Spotless formatting check is green.
- [ ] Tests for modified module(s) are green.
- [ ] Resources (`files`, `META-INF/services`) updated if needed.
- [ ] `CHANGELOG.md` **not modified** (managed automatically by `release-please`).
