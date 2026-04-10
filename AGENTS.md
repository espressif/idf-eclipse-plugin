# Agent guide

This document provides AI coding agents with the rules and context needed to work on [Espressif-IDE / idf-eclipse-plugin](https://github.com/espressif/idf-eclipse-plugin). For human contributor setup, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Project shape

- **Build:** Maven + Tycho (Eclipse/OSGi). From the repo root:

  ```bash
  mvn clean verify -Djarsigner.skip=true
  ```

- **Prerequisites:** Java 17+, Maven 3.9+.
- **Layout:** Plugin source under `bundles/`, tests under `tests/`, release engineering under `releng/`, user documentation under `docs/`.

### Repository structure

```text
idf-eclipse-plugin/
├── bundles/                          # OSGi plugin projects (the actual source)
│   ├── com.espressif.idf.core/       #   Core logic (non-UI)
│   ├── com.espressif.idf.ui/         #   UI layer (wizards, views, editors)
│   ├── com.espressif.idf.branding/   #   Product branding (splash, about)
│   ├── com.espressif.idf.debug.gdbjtag.openocd/
│   ├── com.espressif.idf.launch.serial.core/
│   ├── com.espressif.idf.launch.serial.ui/
│   ├── com.espressif.idf.lsp/
│   ├── com.espressif.idf.sdk.config.core/
│   ├── com.espressif.idf.sdk.config.ui/
│   ├── com.espressif.idf.serial.monitor/
│   ├── com.espressif.idf.swt.custom/
│   ├── com.espressif.idf.terminal.connector/
│   ├── com.espressif.idf.terminal.connector.serial/
│   ├── com.espressif.idf.wokwi/
│   └── com.espressif.idf.help/
├── features/
│   └── com.espressif.idf.feature/    # Eclipse feature grouping all plugins
├── releng/                           # Release engineering
│   ├── com.espressif.idf.configuration/  # Parent POM (Tycho version, shared config)
│   ├── com.espressif.idf.target/     # Target platform definition (.target)
│   ├── com.espressif.idf.update/     # p2 update site (category.xml)
│   ├── com.espressif.idf.product/    # Product definition (idf.product)
│   └── ide-dmg-builder/              # macOS DMG packaging scripts
├── tests/
│   ├── com.espressif.idf.core.test/  # Core unit tests (JUnit 5)
│   └── com.espressif.idf.ui.test/    # UI functional tests (SWTBot)
├── docs/                             # User documentation (Sphinx / esp-docs)
└── resources/                        # Shared resources (formatter profile, etc.)
```

### Eclipse RCP / Tycho build system

This is an **Eclipse RCP** product built with **Tycho 4.0.12**. It is **not** a standard Maven JAR project.

- **OSGi bundles:** Each directory under `bundles/` is a plugin with its own `META-INF/MANIFEST.MF` and `build.properties`.
- **Target platform:** [releng/com.espressif.idf.target/com.espressif.idf.target.target](releng/com.espressif.idf.target/com.espressif.idf.target.target) pins the exact Eclipse platform, CDT, LSP4E/J, JustJ JRE, and third-party dependency versions the build resolves against.
- **Feature:** [features/com.espressif.idf.feature/feature.xml](features/com.espressif.idf.feature/feature.xml) lists all Espressif plugins. A new bundle MUST be added here.
- **Product:** [releng/com.espressif.idf.product/idf.product](releng/com.espressif.idf.product/idf.product) defines the standalone Espressif-IDE application. Update when adding new features to the product.
- **Update site:** [releng/com.espressif.idf.update/category.xml](releng/com.espressif.idf.update/category.xml) categorizes features and bundles for the p2 repository.
- **Parent POM:** [releng/com.espressif.idf.configuration/pom.xml](releng/com.espressif.idf.configuration/pom.xml) holds shared Tycho plugin configuration, jarsigner setup, SpotBugs, and version properties.

### Key constraints (OSGi / Tycho)

These rules differ from standard Maven projects. Violating them **will break the build**.

- **Dependencies go in `MANIFEST.MF`, NOT in `pom.xml`.** Use `Require-Bundle` or `Import-Package` in `META-INF/MANIFEST.MF`. NEVER add `<dependency>` blocks in a bundle's `pom.xml` for compile-time dependencies — Tycho resolves everything from the OSGi metadata and the target platform.
- **Packaging is `eclipse-plugin` or `eclipse-test-plugin`**, not `jar`. Do not change the `<packaging>` element in a bundle's `pom.xml`.
- **Version format in MANIFEST.MF is `x.y.z.qualifier`** (e.g. `1.0.1.qualifier`), not `x.y.z-SNAPSHOT`. Tycho maps between the two automatically.
- **`build.properties` MUST stay in sync.** When adding new source folders, resource directories, or JARs, update both `source..` and `bin.includes` in `build.properties`. A missing entry means the file will not be included in the built plugin.
- **New dependencies MUST exist in the target platform.** If a library is not available in the `.target` file, the build will fail. Either add it to the target definition (if available in Eclipse Orbit / release train) or bundle the JAR in the plugin's `lib/` folder and update `Bundle-ClassPath` in `MANIFEST.MF` and `bin.includes` in `build.properties`.

### Before editing a bundle

ALWAYS read these files first to understand the bundle's structure and dependencies:

1. `META-INF/MANIFEST.MF` — dependencies (`Require-Bundle`, `Import-Package`), exported packages, bundle classpath.
2. `build.properties` — what source and resources are included in the build.
3. `plugin.xml` (if present) — Eclipse extension points the bundle contributes to or consumes.

### Adding a new plugin (bundle)

1. Create the plugin project under `bundles/` following existing naming (`com.espressif.idf.<name>`).
2. Configure `META-INF/MANIFEST.MF` — set `Bundle-SymbolicName` (with `singleton:=true` if it has `plugin.xml` extensions), `Bundle-RequiredExecutionEnvironment: JavaSE-17`, and declare dependencies via `Require-Bundle` or `Import-Package`.
3. Configure `build.properties` — include `META-INF/`, `.`, `plugin.xml`, and any resource folders in `bin.includes`.
4. Add the module to [bundles/pom.xml](bundles/pom.xml).
5. Add the plugin to [features/com.espressif.idf.feature/feature.xml](features/com.espressif.idf.feature/feature.xml).
6. If needed in the product, update [releng/com.espressif.idf.product/idf.product](releng/com.espressif.idf.product/idf.product).

## Documentation

End-user documentation is maintained in [docs/](docs/) (Sphinx / [esp-docs](https://github.com/espressif/esp-docs), reStructuredText). Published at [docs.espressif.com](https://docs.espressif.com/projects/espressif-ide/en/latest/).

- **New or changed user-visible behavior** MUST include documentation updates in the same change or a follow-up PR.
- Sources live under **`docs/en/`** (English). **`docs/zh_CN/`** mirrors the same structure — update Chinese pages when adding or substantially changing English content.
- Match tone, heading style, and cross-references used in neighboring `.rst` files.

## Code formatting (Java)

Use the **Espressif Eclipse Java formatter** profile defined in [resources/espressif_eclipse_formatter.xml](resources/espressif_eclipse_formatter.xml). When generating or editing Java code, ALWAYS follow these rules:

- **Indentation:** Tab character, tab size **4**; continuation indent **2**.
- **Braces:** Opening brace on the **next line** for types, methods, constructors, blocks, `switch`, etc.; **lambda body** and **array initializers** use **end of line**.
- **Comments:** Javadoc is formatted; **comment line length 120**.
- **Formatter on/off tags:** `@formatter:off` / `@formatter:on` exist but are disabled by default.

### Eclipse CDT–related bundles (match existing file style)

These plugins carry **mixed or upstream-style formatting** from Eclipse CDT / embed-cdt. **Do NOT reformat whole files.** Follow the style already in each file — make minimal edits and keep new code consistent with the surrounding class.

- [bundles/com.espressif.idf.debug.gdbjtag.openocd](bundles/com.espressif.idf.debug.gdbjtag.openocd)
- [bundles/com.espressif.idf.launch.serial.core](bundles/com.espressif.idf.launch.serial.core)
- [bundles/com.espressif.idf.launch.serial.ui](bundles/com.espressif.idf.launch.serial.ui)
- [bundles/com.espressif.idf.terminal.connector](bundles/com.espressif.idf.terminal.connector)
- [bundles/com.espressif.idf.terminal.connector.serial](bundles/com.espressif.idf.terminal.connector.serial)

Elsewhere in the repo, ALWAYS use the Espressif formatter rules above.

Match surrounding code in non-Java files (XML, properties, Markdown) to the nearest existing bundle.

## Commit messages (Conventional Commits)

Every commit in a PR MUST satisfy the conventional-commit linter enforced by [.github/workflows/pre-commit.yml](.github/workflows/pre-commit.yml).

**Format:** `type(scope): description` — scope is optional.

**Allowed types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`, `ci`, `build`, `revert`

**Examples:**

- `feat: add ESP32-S3 target support`
- `fix(flash): resolve flashing timeout on Windows`
- `test: add unit tests for toolchain manager`
- `docs: update installation guide for macOS`
- `ci: update GitHub Actions runners`

ALWAYS use imperative mood ("add", "fix", not "added", "fixed"). Split unrelated work into separate commits.

## Tests

### Automated build / Tycho

- Full verification: `mvn clean verify -Djarsigner.skip=true`.
- Test modules live under [tests/](tests/): `com.espressif.idf.core.test` and `com.espressif.idf.ui.test`.
- UI tests use **Tycho Surefire** with the Eclipse workbench (`useUIHarness`, `org.eclipse.ui.ide.workbench`). Follow existing patterns when adding suites.

### What to add when you change code

- **Core/plugin logic** (`com.espressif.idf.core` and related): add tests in **`com.espressif.idf.core.test`** (JUnit 5, provider hint `junit512`).
- **UI / workflows / wizards:** add or extend **SWTBot** tests in **`com.espressif.idf.ui.test`**, following the **Given–When–Then** structure and fixtures described in [tests/com.espressif.idf.ui.test/README.md](tests/com.espressif.idf.ui.test/README.md).
- Tests MUST be **independent** — no ordering assumptions; clean workspace/UI state so other tests are unaffected.
- Prefer **resource files** (under test `resources/`) for expected templates/output rather than large string literals in Java.

### Skipping tests (local only)

`-DskipTests=true` — use only when necessary; merged code MUST NOT rely on skipped tests.

## CI checks that gate PRs

These checks run automatically on PRs and MUST pass:

| Check | What it verifies |
|-------|------------------|
| `ci.yml` (Linux + macOS) | Full Maven/Tycho build, unit + UI tests, p2 repo and product packaging |
| `pre-commit.yml` | Conventional commit message format on every commit; codespell on `*.py`, `*.c`, `*.h`, `*.md`, `*.rst`, `*.yml` |
| `docs_build.yml` | Documentation builds without errors |

The main CI builds with **JDK 21** and **Maven 3.9.6**, with **ESP-IDF v5.4** on the runner.

## Quality

SpotBugs is configured in the parent POM and runs as part of the build. Address any warnings it reports rather than suppressing them without justification.

When in doubt, mirror an existing bundle's structure, naming, and test style before introducing new patterns.
