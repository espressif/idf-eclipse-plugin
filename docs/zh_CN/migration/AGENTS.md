# AGENTS.md — Espressif-IDE → ESP-IDF VS Code migration

You are migrating **an existing ESP-IDF project** from Espressif-IDE (Eclipse) to the **ESP-IDF VS Code Extension**, not creating a new project. When instructed to migrate, **perform the migration**: create or merge the project's `.vscode/settings.json` and `.vscode/launch.json`, validate the result, and report exceptions. Do not respond with a GUI walkthrough instead of performing the task.

## Operating contract: preserve behavior, finish promptly

- **Source of truth:** the selected project's `sdkconfig`, project files and matching Eclipse workspace metadata. Copy explicit, compatible settings; do not silently replace them with defaults or values from unrelated projects.
- **One-shot workflow:** discover → map → write → validate → report. Usually one scoped discovery pass and, only if required, one targeted fallback pass. **Stop** when the required configuration and checks are complete.
- **No research loops:** do not repeatedly scan the filesystem, enumerate every installed tool, dump full environment/build logs, inspect extension source code, or browse documentation unless a specific mapping is unresolved. Capture short, relevant command output; reuse results already found.
- **No unnecessary interaction:** do not request ESP-IDF/OpenOCD paths before checking existing VS Code settings, active setup metadata, and relevant environment information. If a nonessential item cannot be resolved, continue and mark it **pending**. Ask one focused question only when a genuinely essential ambiguity blocks the requested operation.
- **No implicit build or hardware operation:** do **not** run `idf.py set-target`, `fullclean`, `menuconfig`, a full build, flash/erase, or a flashing debug session as part of routine migration. Run build only if explicitly requested; obtain explicit approval before any operation that can modify the device. Checking existing files/tool availability is allowed.
- **Minimal changes:** preserve original Eclipse metadata, `sdkconfig`, `sdkconfig.defaults*`, CMake, application sources, and unrelated VS Code settings/profiles. Do not install tools or alter user-level VS Code settings.
- **Idempotent:** a repeat with unchanged sources should produce no extra debug configurations or unrelated edits. Update an existing matching migrated configuration instead of appending duplicates.
- **Honest status:** distinguish **configured**, **statically verified**, **build tested**, and **hardware tested**. Never claim hardware/debugging passed without running it.

## 1. Discover the selected project and Eclipse sources

Identify the ESP-IDF project directory (with root `CMakeLists.txt`) and the corresponding Eclipse workspace. Check these **specific** files first; do not recursively search the entire machine:

| Source | What to extract |
| --- | --- |
| Project `sdkconfig`, `dependencies.lock`, root `CMakeLists.txt`, optional `build/project_description.json` | Target, IDF version, actual CMake project name / ELF name |
| Project `.project`, `.cproject`, `.settings/` when relevant | Eclipse project identity and project-specific configuration |
| `<workspace>/.metadata/.plugins/org.eclipse.debug.core/.launches/*.launch` (**E**) | Launch configuration **matching this project**; XML attributes, custom build, flash and debugger settings |
| `<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.launchbar.core.prefs` (**B**) | Project-qualified active launch target, board and adapter selection |
| `<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.espressif.idf.debug.gdbjtag.openocd.prefs` (**D**) | Fallback Eclipse debugger defaults |
| `<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.espressif.idf.launch.serial.ui.prefs` (**S**) | Optional serial UI state |
| Existing `.vscode/settings.json`, `.vscode/launch.json` and applicable workspace settings | Preserve user configuration; identify effective overrides/conflicts |

- The workspace `.metadata` commonly lives **beside** the Eclipse project, not inside it. Check the project parent and nearby known workspace roots; if missing, migrate the information that is available and report the Eclipse-only items as pending. Do not block all migration to demand `.metadata`.
- Parse `.launch` as XML, including escaped newlines and XML entities. Match the launch's project/resource identity; do **not** select an arbitrary `*.launch` or stale launchbar entry.
- **Target precedence:** `sdkconfig` → `dependencies.lock` (cross-check) → project-qualified active B → E fallback. If these materially disagree, report the conflict rather than rewriting `sdkconfig`. Ignore stale CDT `target_os` values.
- **Board precedence:** project-qualified active B plus actual adapter details, then the verified installed OpenOCD scripts. Do not infer the adapter from the chip name alone.
- **Custom build/debug precedence:** matching E → relevant D fallback; explicitly configured project values win over defaults and prior agent-generated values.
- Inspect only the selected project. Do not treat other target-specific `sdkconfig.defaults.*` files as the active target.

## 2. Resolve the active ESP-IDF environment (non-interactive)

**`IDF_PATH` is the ESP-IDF framework source tree; OpenOCD is normally installed separately. Never search for an OpenOCD package inside `IDF_PATH` merely because that path was supplied.**

1. Reuse a compatible, **already selected** extension setup. Inspect project/workspace `idf.currentSetup`, available `idf.eimIdfJsonPath` / EIM `eim_idf.json`, and effective ESP-IDF environment variables. When no path is configured, check the normal EIM manifest locations (`C:\Espressif\tools\eim_idf.json` on Windows; `~/.espressif/tools/eim_idf.json` on macOS/Linux) before any wider search. A saved ESP-IDF Doctor report can provide `IDF_PATH`, `IDF_TOOLS_PATH` and `OPENOCD_SCRIPTS` directly. A generic AI-agent terminal may **not** inherit the VS Code extension's activated environment.
2. Cross-check the effective IDF version with the project; respect extension/user environment overrides and **report** stale or contradictory overrides instead of silently changing user-level settings. Write `idf.currentSetup` only when the verified installation is appropriate and a project-level selection is needed; do not invent a path.
3. For OpenOCD, prefer a valid `OPENOCD_SCRIPTS` from the selected setup. Otherwise find its `openocd-esp32` executable/package and scripts directory using the **same setup** and known installation layouts. Examples to check, as applicable:

   ```text
   <IDF_TOOLS_PATH>/openocd-esp32/<version>/openocd-esp32/share/openocd/scripts
   <IDF_TOOLS_PATH>/tools/openocd-esp32/<version>/openocd-esp32/share/openocd/scripts
   ```

   `IDF_TOOLS_PATH` may already name a `tools` directory; do not append another `tools` blindly. Choose the version associated with the selected setup, not an arbitrary installed version. If the environment is not activated but the active setup is known, inspect its installation metadata before searching elsewhere.
4. Match the Eclipse board **and adapter** to the necessary `.cfg` file(s) under the resolved scripts directory; use verified relative paths in the required order for `idf.openOcdConfigs`. Verify each file exists. Do not fabricate names like `board/<target>-builtin.cfg`. If no reliable match is possible, **omit only the unverified board setting**, continue and mark OpenOCD verification pending.
5. Use `${command:espIdf.getToolchainGdb}` for GDB; do not hardcode an architecture-specific executable. Reuse the active setup's Python, CMake and Ninja without configuring their absolute paths unnecessarily.
6. Resolve the explicit Eclipse build directory. If it is inside the project, a `${workspaceFolder}`-relative equivalent may improve portability. A build directory may **not exist yet** on a clean machine: a valid, resolvable destination with an accessible parent is sufficient; do not require a pre-existing ELF. Preserve an external absolute path only when meaningful on this machine. Otherwise record it as pending rather than inventing a replacement. Resolve the ELF from `build/project_description.json`, an existing verified ELF, or the actual CMake `project(...)` name (not necessarily the folder name). A VS Code-supported project-name substitution is preferable to guessing when build metadata is not present.

**Stop rule:** once the active setup, target, build path and (if available) OpenOCD `.cfg` are resolved, proceed to writing files. Do not launch repeated EIM/OpenOCD discovery rounds. If OpenOCD cannot be located, this is **not** a reason to delay unrelated mappings or ask for paths immediately.


## 3. C/C++ code intelligence configuration

Example of standard .vscode/c_cpp_properties.json file:

```json
{
  "configurations": [
    {
      "name": "ESP-IDF",
      "compilerPath": "",
      "compileCommands": "${config:idf.buildPath}/compile_commands.json",
      "intelliSenseMode": "gcc-x86",
      "includePath": [
        "${workspaceFolder}/**"
      ],
      "browse": {
        "path": [
          "${workspaceFolder}"
        ],
        "limitSymbolsToIncludedHeaders": true
      }
    }
  ],
  "version": 4
}
```

- Preserve existing `.vscode/c_cpp_properties.json`.
- If Microsoft C/C++ IntelliSense is used, generate or merge
  `c_cpp_properties.json` with a compileCommands path
  pointing to the actual ESP-IDF build directory.
- Leave compilerPath empty when relying on the compilation
  database, following the ESP-IDF extension convention.
- If clangd is used, configure its compilation database
  path instead; do not generate c_cpp_properties.json
  unnecessarily.
- Do not treat a missing c_cpp_properties.json as a
  Build, Flash, or Debug migration failure.
- Never run a full build solely to generate this file.

## 4. Create or merge `.vscode/settings.json`

Read existing VS Code JSON/JSONC without destroying unrelated keys or comments where practical. Apply **only verified mappings**:

| Eclipse source | VS Code destination | Rule |
| --- | --- | --- |
| `sdkconfig`: `CONFIG_IDF_TARGET` | No additional setting required | Keep the source target; never run `set-target` or rewrite `sdkconfig` during migration. |
| E: `com.espressif.idf.launch.serial.core.idfBuildFolderPath` | `idf.buildPath` | Preserve the explicit usable custom directory (it need not exist before the first build); otherwise omit and report an unresolvable/inaccessible custom destination. Without an explicit custom path, allow the extension default build directory. |
| E: `DFU`, `FLASH_OVER_JTAG` | `idf.flashType` | `DFU=true` → `DFU`; else `FLASH_OVER_JTAG=true` → `JTAG`; else `UART` **only when supported by the source**. Never copy a fixed example value. |
| B: active board + adapter | `idf.openOcdConfigs` | Use the verified relative OpenOCD `.cfg`; omit if not reliably resolved. |
| E/S: explicitly saved serial port | `idf.port`, optionally `idf.monitorPort` | Copy only a valid destination-machine port. Otherwise leave detection to the extension; report a stale port. |
| E: `...gdbServerGdbPortNumber` | `idf.openOcdLaunchArgs` **and** `launch.json` `target.port` | Preserve the explicit GDB server port; ensure OpenOCD and debugger agree. |
| E: `...gdbServerTclPortNumber` | `openocd.tcl.port` **and** OpenOCD args | Use a JSON **integer** and a matching Tcl server port. |
| E: `...gdbServerTelnetPortNumber` | `idf.openOcdLaunchArgs` | Preserve the explicit Telnet server port when relevant. |
| E/D: explicitly custom `...gdbServerExecutable` | `idf.customOpenOCDPath` | Only if the executable exists; ignore the standard Eclipse `${openocd_path}/${openocd_executable}` placeholder. |
| E: custom `ilg.gnumcueclipse.debug.gdbjtag.svdPath` | `idf.svdFilePath` | Only if the resolved path exists; never copy `${esp_svd_path}` literally. |

For the abbreviated debugger keys above, the usual prefix is `com.espressif.idf.debug.gdbjtag.openocd.`. Check actual XML key names rather than blindly expanding abbreviations.

### OpenOCD arguments and port invariants

- **Avoid redundant overrides.** If the standard board arguments/ports already produce the source behavior, leave `idf.openOcdLaunchArgs` unset.
- If custom ports or other required OpenOCD commands make `idf.openOcdLaunchArgs` necessary, **explicitly retain both default expansions first**:

  ```text
  "-d${config:idf.openOcdDebugLevel}",
  "${config:idf.openOcdConfigs,-f}"
  ```

  Then add required pairs such as `"-c", "gdb_port <GDB_PORT>"`, `"-c", "tcl_port <TCL_PORT>"`, `"-c", "telnet_port <TELNET_PORT>"`. Replace placeholders with source port numbers and use syntax accepted by the installed OpenOCD. Do not write the angle-bracket placeholders into the final files.
- With a custom Tcl port, set **both** OpenOCD's `tcl_port` and `openocd.tcl.port` to the **same numeric value**. Match GDB's `target.port` to OpenOCD's GDB port. Keep non-conflicting existing custom OpenOCD arguments; report conflicts instead of silently dropping them.
- `idf.openOcdLaunchArgs` replaces the extension's automatic board/debug arguments. Merely setting `idf.openOcdConfigs` alongside it is not enough; keep the substitutions above.

### Structural example (placeholders, not actual values)

```json
{
  "idf.buildPath": "<VERIFIED_BUILD_DIRECTORY>",
  "idf.flashType": "<UART_OR_JTAG_OR_DFU>",
  "idf.openOcdConfigs": ["board/<VERIFIED_BOARD>.cfg"]
}
```

Replace every placeholder from the **selected Eclipse project and active setup**. Omit any key whose mapping is inapplicable or unverifiable. Custom port overrides are added **only when needed**, following the rules above. This example is **not** a configuration to copy literally.

### Do not invent environment variables

Do **not** automatically populate `idf.customExtraVars` with `IDF_TARGET`, `IDF_PATH`, `ADF_PATH`, USB adapter location or a full toolchain PATH. The selected ESP-IDF setup already provides most of these. Only carry over an explicitly required, verified project-specific variable if it has a compatible effect; do not add a nonexistent `ADF_PATH`. Report conflicting inherited/user-level variables (especially `IDF_TARGET`) without modifying user-level settings. For USB-JTAG adapter serial/location, transfer only a verified, supported selector; otherwise report the unresolved hardware-specific mapping. Do not generate unrelated `clangd.*` settings unless explicitly requested or clearly sourced from Eclipse and validated.

## 5. Generate or merge `.vscode/launch.json`

For ESP-IDF Extension debugging, use **`type: "gdbtarget"` and `request: "attach"`**. Keep unrelated profiles; update the matching migrated profile instead of creating a duplicate.

Example of standard launch.json file for ESP-IDF v6.1 project:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "gdbtarget",
      "request": "attach",
      "name": "Eclipse CDT GDB Adapter"
    }
  ]
}
```

- Use `target.port` as a **string** when an explicit GDB port exists; omit it when no reliable override is necessary. Derive `initialBreakpoint` from Eclipse, not from example values.

- Ignore `set remotetimeout` or `set mem inaccessible-by-default off` parameters. 

| Eclipse `.launch` source | `gdbtarget` mapping / treatment |
| --- | --- |
| `org.eclipse.cdt.debug.gdbjtag.core.setStopAt` and `.stopAt` | `true` → `initialBreakpoint` with the exact symbol; `false` → `""` to disable the default `app_main` break. |
| `...doStartGdbServer` | Ignore |
| `...gdbServerGdbPortNumber`, consistent CDT `.portNumber` | `target.host` / `target.port`; agree with OpenOCD server configuration. |
| `...gdbClientOtherCommands` | Ignore |
| `...otherInitCommands` | Ignore |
| `...doFirstReset`, `.firstResetType`, `.doSecondReset`, `.secondResetType` | Ignore |
| `...doFlashBeforeStart` | Ignore |
| `...doContinue`, CDT `.setResume` | Ignore |

**GDB invariants (confirmed by hardware regression):**

- **Never generate `preConnectCommands`.** It is not accepted by the tested `gdbtarget` schema.
- **Never manually add `target remote`, `-target-select` or another remote-attach command** into `initCommands` or `target.connectCommands`: the ESP-IDF extension already handles its connection.
- The extension prepends generated GDB init/connect commands before user-defined `target.connectCommands`, and runs `initCommands` after connection. Preserve compatible custom commands without duplicating extension-managed connection/reset/flush operations.
- Validate generated fields against the installed debugger's available schema when accessible. If a key is unsupported, **remove it and report the lost or partial equivalence**; do not invent another key.

## 6. Report non-1:1 Eclipse settings; do not fabricate mappings

Report each unmapped custom item as **unsupported**, **ambiguous** or **pending runtime validation**; do not silently discard it:

- `SERIAL_MONITOR_ENCODING` (e.g. `ISO-8859-1`): no verified direct extension monitor-encoding setting.
- `OPEN_SERIAL_MONITOR`: debug-specific `idf.launchMonitorOnDebugSession` is **not** a universal after-flash equivalent.
- Semihosting and target-specific reset/continue behavior: require verified adapter/target semantics.
- `OPENOCD_ADAPTER_SERIAL`, `OPENOCD_USB_ADAPTER_LOCATION`: dependent on actual adapter and runtime; avoid copying stale port locations or inventing universal VS Code keys.
- Eclipse `gdbServerOther`, `jtagFlashArguments`, Eclipse variables, and custom launch scripts: not automatically identical to `idf.openOcdLaunchArgs` / `idf.jtagFlashCommandExtraArgs`.
- Eclipse-only CDT index, workbench and console preferences: skip unless an exact project-relevant equivalent is proven.

## 7. Apply once, validate once, report and stop

1. Inspect the destination files; preserve a recoverable copy or minimal diff before changing them. Merge only justified values. Support JSONC (comments/trailing commas) when reading existing VS Code files; avoid rewriting comments or unrelated configuration unnecessarily.
2. Write/merge `settings.json` and the **single** matching migrated attach profile. Do not rewrite source files or global settings. Never generate literal `<PLACEHOLDER>` text in final JSON.
3. Run **static checks**, without a full build: parse both files; verify property names/types (not just JSON syntax); compare all mapped source values to output; check effective target/IDF version, custom build directory and ELF resolution when available, board config existence when available, GDB/OpenOCD port consistency, Tcl client/server agreement, and OpenOCD override expansions. Correct a discovered mismatch and recheck the affected item **once**.
4. When the environment/ELF/board is missing, mark that particular validation **pending** instead of blocking all others. Existing ELF can establish path availability; it does **not** prove a new build passed. Do not run a full build, flash, or hardware debugging unless separately requested and approved where needed.
5. **Stop.** Report concisely: files changed, important mappings (including source-preserved custom ports), unsupported/partial mappings, configuration checks passed, build/hardware **not tested** or actual result, and unresolved external/global conflicts. Do not claim complete 1:1 equivalence when exceptions remain.

**Expected one-line request:** `Migrate this existing Eclipse Espressif-IDE project to the ESP-IDF VS Code Extension.`

**Reference documentation:** [ESP-IDF extension settings](https://docs.espressif.com/projects/vscode-esp-idf-extension/en/latest/settings.html), [debug configuration](https://docs.espressif.com/projects/vscode-esp-idf-extension/en/latest/debugproject.html), [installation/EIM](https://docs.espressif.com/projects/vscode-esp-idf-extension/en/latest/installation.html). Use these as reference when a specific mapping is uncertain; do not repeatedly query documentation during a routine migration.
