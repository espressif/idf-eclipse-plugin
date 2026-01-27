<h1 align="center">Espressif-IDE</h1>
<p align="center">
  <strong>The official Eclipse-based IDE for ESP-IDF development</strong>
</p>

<p align="center">
  <a href="https://github.com/espressif/idf-eclipse-plugin/releases/latest"><img src="https://img.shields.io/github/release/espressif/idf-eclipse-plugin.svg" alt="GitHub Release"></a>
  <a href="https://github.com/espressif/idf-eclipse-plugin/blob/master/LICENSE.md"><img src="https://img.shields.io/badge/License-EPL%202.0-blue.svg" alt="License"></a>
  <a href="https://github.com/espressif/idf-eclipse-plugin/issues"><img src="https://img.shields.io/github/issues/espressif/idf-eclipse-plugin.svg" alt="GitHub Issues"></a>
  <a href="https://github.com/espressif/idf-eclipse-plugin/actions"><img src="https://img.shields.io/github/actions/workflow/status/espressif/idf-eclipse-plugin/ci.yml?branch=master" alt="Build Status"></a>
</p>

<p align="center">
  <img src="docs_readme/images/macos-logo.png" alt="macOS">
  <img src="docs_readme/images/windows-logo.png" alt="Windows">
  <img src="docs_readme/images/linux-logo.png" alt="Linux">
</p>

<p align="center">
  <a href="https://docs.espressif.com/projects/espressif-ide/en/latest/">Documentation</a> &middot;
  <a href="https://github.com/espressif/idf-eclipse-plugin/releases/latest">Download</a> &middot;
  <a href="https://github.com/espressif/idf-eclipse-plugin/issues">Report Bug</a> &middot;
  <a href="./README_CN.md">中文</a>
</p>

---

Espressif-IDE is a standalone, customized IDE built on Eclipse CDT for developing IoT applications with [ESP-IDF](https://github.com/espressif/esp-idf). It bundles the IDF Eclipse plugins, Eclipse CDT, and essential third-party plugins into a single ready-to-use environment for the entire ESP32 family of chips.
## Key Features

### Write

| Feature | Description |
|---------|-------------|
| **Project Wizards** | Create new projects from scratch or from 100+ ESP-IDF example templates |
| **Clangd-Powered Editor** | LSP-based C/C++ editor with code completion, navigation, and real-time diagnostics |
| **CMake Editor** | Syntax highlighting and content assist for `CMakeLists.txt` files |
| **SDK Configuration (menuconfig)** | Visual editor for Kconfig options -- no terminal needed |

### Build

| Feature | Description |
|---------|-------------|
| **CMake Build System** | Full integration with ESP-IDF's CMake-based build |
| **Toolchain Management** | Auto-configured ESP GCC and Clang toolchains |
| **ESP-IDF Manager** | Install, manage, and switch between multiple ESP-IDF versions from inside the IDE |
| **Size Analysis** | Visualize your firmware's static memory footprint (RAM/Flash) |

### Flash

| Feature | Description |
|---------|-------------|
| **UART Flashing** | One-click flash over serial using `idf.py flash` |
| **JTAG Flashing** | Flash via JTAG debug probes |
| **DFU Flashing** | Flash over USB using Device Firmware Update protocol |
| **Partition Table Editor** | Visual editor for custom partition layouts |
| **NVS Editor** | Create and edit Non-Volatile Storage partition data |

### Debug

| Feature | Description |
|---------|-------------|
| **OpenOCD Debugging** | Fully integrated JTAG debugging with pre-built launch configurations |
| **GDB Stub Debugging** | Debug panics and exceptions over serial |
| **Core Dump Analysis** | Post-mortem debugging from crash dumps |
| **Heap Tracing** | Profile memory allocations and detect leaks at runtime |
| **App-Level Tracing** | Collect real-time trace data from running firmware |

### Monitor & Simulate

| Feature | Description |
|---------|-------------|
| **Serial Monitor** | Integrated terminal for viewing device output |
| **ESP-IDF Terminal** | Pre-configured shell with all build environment variables set |
| **Wokwi Simulator** | Test your firmware on a virtual ESP32 -- no hardware required |

## Quick Start

### Option 1: Download Espressif-IDE (Recommended)

Download the latest standalone installer from the [releases page](https://github.com/espressif/idf-eclipse-plugin/releases/latest) for your platform.

### Option 2: Install the Plugin into an Existing Eclipse

Install from the Eclipse Marketplace or the update site. See the [installation guide](https://docs.espressif.com/projects/espressif-ide/en/latest/) for details.

### First Steps

1. Launch Espressif-IDE
2. Install ESP-IDF and tools through the built-in ESP-IDF Manager
3. Create a new project using **File > New > Espressif IDF Project**
4. Select a target chip and serial port
5. Build, flash, and monitor

Full walkthrough in the [official documentation](https://docs.espressif.com/projects/espressif-ide/en/latest/).

## Building from Source

**Prerequisites:** Java 17+ and Maven 3.9+

```bash
git clone https://github.com/espressif/idf-eclipse-plugin.git
cd idf-eclipse-plugin
mvn clean verify -Djarsigner.skip=true
```

The p2 update site artifact is generated at `releng/com.espressif.idf.update/target/`. Install it using the [local archive instructions](https://docs.espressif.com/projects/espressif-ide/en/latest/marketplaceupdate.html?#installing-idf-eclipse-plugin-from-local-archive).

## Getting the Latest Development Build

1. Go to the [latest commits on master](https://github.com/espressif/idf-eclipse-plugin/commits/master)
2. Click the green checkmark on the latest commit
3. Click **Details** > **Summary**
4. Download the `com.espressif.idf.update` artifact from the **Artifacts** section
5. Install using the [local archive instructions](https://docs.espressif.com/projects/espressif-ide/en/latest/marketplaceupdate.html?#installing-idf-eclipse-plugin-from-local-archive)

## Contributing

We welcome contributions! See [CONTRIBUTING.md](CONTRIBUTING.md) for development environment setup, coding guidelines, and how to submit pull requests.

## Resources

- [Official Documentation](https://docs.espressif.com/projects/espressif-ide/en/latest/)
- [ESP-IDF Programming Guide](https://docs.espressif.com/projects/esp-idf/en/latest/)
- [Espressif on GitHub](https://github.com/espressif)
- [ESP32 Forum](https://esp32.com/)

## Reporting Issues

Found a bug or have a feature request? Please open an issue [here](https://github.com/espressif/idf-eclipse-plugin/issues) with your environment details and relevant logs.

## License

This project is licensed under the [Eclipse Public License v2.0](LICENSE.md).
