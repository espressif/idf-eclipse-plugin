# AGENTS.md — Espressif-IDE to ESP-IDF VS Code Migration

Use this file as guidance when helping a user migrate an existing ESP-IDF project from Espressif-IDE / the ESP-IDF Eclipse Plugin to the ESP-IDF VS Code Extension.

## Goal

Preserve the existing ESP-IDF project and reproduce the user's working development flow in VS Code with the minimum required changes.

Do not recreate the project from a template unless the user explicitly asks to do so.

## Migration request behavior

When the user asks to migrate an existing Espressif-IDE project to the
ESP-IDF VS Code Extension, perform the complete migration workflow
automatically.

Do not require the user to identify Eclipse metadata, custom build settings,
JTAG/OpenOCD settings, or debug configuration manually.

Inspect all relevant project files and, when accessible, the original Eclipse
workspace metadata.

Automatically migrate settings when the mapping to VS Code is unambiguous,
including:

- ESP-IDF target and project configuration
- custom build directory
- JTAG / OpenOCD board configuration
- adapter-specific settings
- VS Code debug configuration
- supported custom Eclipse `.launch` settings
- debug arguments and custom GDB settings

Do not only report what needs to be changed. Apply supported changes directly
when they can be migrated safely.

If required Eclipse workspace metadata is not accessible, do not guess.
Explain what information is missing and where it is normally located.

After migration, report only:

- what was migrated
- what files or settings were changed
- what could not be migrated automatically
- what still requires user attention

## General rules

- Treat the existing ESP-IDF project files as the primary source of truth.
- Do not overwrite `sdkconfig`, `sdkconfig.defaults*`, `CMakeLists.txt`, component manifests, or source files unless required and approved by the user.
- Prefer migration and validation over regeneration.
- Keep the original Eclipse workspace, including `<workspace>/.metadata/`, until Build, Flash, Monitor, and Debug are verified in VS Code.
- Do not blindly copy Eclipse workspace values when they conflict with the actual ESP-IDF project configuration.
- When a setting can be mapped unambiguously to a VS Code equivalent, migrate it automatically instead of only reporting it.
- When a setting cannot be mapped reliably, preserve the original value, explain the limitation, and ask the user to review it manually.
- Inspect both the ESP-IDF project and the original Eclipse workspace metadata when the workspace is available.

## Source-of-truth priority

When target-related values conflict, prefer:

1. ESP-IDF project configuration (`sdkconfig`)
2. `dependencies.lock`
3. Current Eclipse launchbar / device state
4. Eclipse `.launch` values only as a fallback

If conflicting values are detected, report the conflict before applying changes.

## Migration workflow

### 1. Open the existing project

Open the existing ESP-IDF project root directly in VS Code.

Do not create a new project or copy the source into a new template.

### 2. Verify ESP-IDF setup

Confirm that VS Code uses the ESP-IDF version expected by the project.

Prefer using the same ESP-IDF version first. Treat any ESP-IDF version upgrade as a separate task after migration is validated.

### 3. Verify target and board

Confirm the ESP-IDF target.

For JTAG users, explicitly select the correct board/device even when `IDF_TARGET` already appears correct. The chip target and OpenOCD board configuration may be initialized separately.

### 4. Verify flash method and port

Check:

- UART or JTAG flash method
- Serial port for UART / Monitor
- Correct board/OpenOCD configuration for JTAG

### 5. Handle build cache only if required

Try the project normally first.

If configuration or Menuconfig fails because the inherited build cache references a different Python executable, ESP-IDF environment, or absolute path, run:

`ESP-IDF: Full Clean`

Then retry configuration.

### 6. Build

Run:

`ESP-IDF: Build your project`

Verify that the existing project configuration still works.

### 7. Menuconfig

Run:

`ESP-IDF: SDK Configuration Editor (Menuconfig)`

Verify that important existing values are preserved.

### 8. Flash and Monitor

Run:

`ESP-IDF: Flash your project`

Then:

`ESP-IDF: Monitor your device`

Verify expected runtime output.

### 9. Debug

If `.vscode/launch.json` is missing and the user needs Debug, run:

`ESP-IDF: Add VS Code Configuration Folder`

Do not create `launch.json` manually unless a custom configuration requires it.

Verify that default Debug works first.

Important: this creates VS Code defaults. It does not automatically convert a custom Eclipse `.launch` configuration.

### 10. Migrate custom Eclipse debug settings

When the original Eclipse workspace is available, inspect:

`<workspace>/.metadata/.plugins/org.eclipse.debug.core/.launches/<project>.launch`

Start from the standard VS Code Debug configuration, then migrate supported custom Eclipse debug settings into the VS Code configuration automatically when the mapping is unambiguous.

Review and preserve, when applicable:

- GDB port
- TCL port
- Telnet port
- `stopAt`
- semihosting
- flash-before-debug behavior
- reset/start behavior
- custom GDB commands
- debug arguments
- custom timeouts

Do not replace explicit custom Eclipse values with VS Code defaults when an equivalent VS Code setting exists.

If a setting has no direct VS Code equivalent, or if multiple conflicting Eclipse values exist:

1. Preserve the original value.
2. Report the conflict or unsupported setting.
3. Explain what could not be migrated automatically.
4. Ask the user before choosing an alternative.

## Custom build directory

A custom Espressif-IDE build directory may be stored in the Eclipse launch configuration:

`<workspace>/.metadata/.plugins/org.eclipse.debug.core/.launches/<project>.launch`

Relevant key:

`com.espressif.idf.launch.serial.core.idfBuildFolderPath`

If the Eclipse launch configuration contains a valid custom build directory, migrate it to the equivalent VS Code project setting:

`idf.buildPath`

Do not fall back to the default `build` directory when an explicit custom build directory was configured in Eclipse.

Do not assume the old generated build state itself should be reused. If the custom build directory contains incompatible CMake cache or environment-specific generated files, clean or reconfigure the generated state while preserving the configured build path.

## JTAG / OpenOCD metadata

Useful Eclipse workspace locations include:

`<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.launchbar.core.prefs`

`<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.espressif.idf.debug.gdbjtag.openocd.prefs`

Adapter-specific values may include:

- `OPENOCD_ADAPTER_SERIAL`
- `OPENOCD_USB_ADAPTER_LOCATION`

Also inspect the Eclipse workspace for board/OpenOCD configuration and other adapter-specific values.

When an equivalent ESP-IDF VS Code setting exists, migrate the value automatically if the mapping is unambiguous. Do not assume that a correct `IDF_TARGET` means the correct OpenOCD board configuration has already been restored.

If only the project folder is available and the original Eclipse workspace has been deleted, some workspace-level JTAG/debug settings may no longer be recoverable.

## IDE-specific settings migration

When the original Eclipse workspace is available, actively migrate supported IDE-specific settings instead of only reporting them.

Inspect the project together with relevant Eclipse workspace metadata under:

`<workspace>/.metadata/`

Important locations include:

- `<workspace>/.metadata/.plugins/org.eclipse.debug.core/.launches/<project>.launch`
- `<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/org.eclipse.launchbar.core.prefs`
- `<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.espressif.idf.debug.gdbjtag.openocd.prefs`
- `<workspace>/.metadata/.plugins/org.eclipse.core.runtime/.settings/com.espressif.idf.launch.serial.ui.prefs`

Apply settings automatically when the mapping to a VS Code equivalent is clear.

Do not blindly copy stale or duplicated Eclipse metadata. Resolve target-related conflicts using the source-of-truth priority defined above.

After applying the migration, provide a concise summary containing:

- settings migrated automatically
- files created or modified
- settings that require manual review
- conflicts or unsupported values that were intentionally left unchanged

## C/C++ code navigation

Espressif-IDE uses clangd for code navigation, diagnostics, and header resolution.

The default ESP-IDF VS Code setup uses the Microsoft C/C++ extension.

If the user wants to continue using clangd, instruct them to install the clangd extension and run:

`ESP-IDF: Configure project for ESP-Clang`

## Local custom components

No migration-specific changes are required for local custom components that are already configured in the ESP-IDF project.

If a component is referenced by an absolute path outside the project, verify that the path is still valid.

## Validation checklist

Do not consider migration complete until the applicable items below pass:

- Existing project opens directly in VS Code
- Correct ESP-IDF version is selected
- Correct target is selected
- Correct board/OpenOCD configuration is selected for JTAG
- Flash method is correct
- Serial port is correct when required
- Menuconfig opens
- Existing configuration values are preserved
- Build succeeds
- Managed components resolve
- Local/custom components resolve
- UART and/or JTAG Flash works
- Monitor shows expected runtime output
- Custom build path is restored if required
- Relevant JTAG/OpenOCD and adapter settings are restored when recoverable
- `.vscode/launch.json` exists for Debug users
- Default Debug starts successfully
- Supported custom Eclipse debug settings have been migrated
- Unsupported or conflicting debug settings have been reported for manual review

## Expected migration behavior

Usually reusable without migration-specific changes:

- source code
- CMake project files
- `sdkconfig`
- `sdkconfig.defaults*`
- component manifests
- dependency lock files
- managed components
- local components

Usually requires user attention:

- custom build directory
- JTAG/OpenOCD board configuration
- adapter-specific settings
- `.vscode/launch.json`
- custom Eclipse `.launch` values
- environment-specific build cache

## User communication

Keep instructions concise and action-oriented.

Prefer:

> The project itself is reusable. I will migrate the IDE-specific settings that can be mapped safely and report only what still needs manual review.

Avoid presenting every Eclipse metadata file as something the user must manually edit.

Only surface advanced workspace metadata when it is relevant to the user's actual migration problem.
