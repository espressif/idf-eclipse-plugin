# Espressif IDE

## Overview
Espressif IDE is an Integrated Development Environment(IDE) based on Eclipse CDT for developing IoT Applications using ESP-IDF framework. It provides better tooling capabilities, which enhances standard Eclipse CDT for developing and debugging ESP32 IoT applications. It offers advanced editing, compiling, flashing and debugging features with the addition of Installing the tools, SDK configuration and CMake editors.

![](images/espressifide_splash.bmp)

## Features:
- Easy-to-use IDE built on Eclipse CDT 
- Specifically built for ESP-IDF application development
- Auto-configuration of the build environment variables
- Integrated toolchain configuration
- New Project Wizards and getting started ESP-IDF examples
- Advanced editing, compiling, and editing with syntax coloring
- Pre-build function header and function definition navigation
- Integrated ESP-IDF and ESP-IDF Tools installation directly from IDE
- SDK Configuration editor for project-specific settings 
- Integrated CMake Editor plug-in for editing CMake files such as CMakeLists.txt
- CMake based build support
- Support for UART and JTAG flashing
- Customized ESP-IDF OpenOCD Debugging with pre-built configuration and settings
- GDB Hardware debugging
- Integrated ESP-IDF serial monitor
- ESP-IDF Terminal with the pre-configured ESP-IDF build environment
- Application size analysis editor for analyzing static memory footprint of your application
- Heap profiling for memory analysis and finding memory leaks
- Supports esp32,esp32s2, esp32s3 and esp32c3 chips
- English and Chinese language support IDE
- Extensible IDE for other third-party plugins
- Host operating systems: Windows, macOS, and Linux 

## Downloads
### Espressif-IDE v2.3.0-beta

| OS  | Download |
| ------------- | ------------- |
| Windows  | <a href ="https://dl.espressif.com/dl/espressif-ide//Espressif-IDE-2.3.0-beta-win32.win32.x86_64.zip">Espressif-IDE-2.3.0-beta-win32.win32.x86_64.zip</a>  |
| macosx | <a href ="https://dl.espressif.com/dl/espressif-ide//Espressif-IDE-2.3.0-beta-macosx.cocoa.x86_64.tar.gz">Espressif-IDE-2.3.0-beta-macosx.cocoa.x86_64.tar.gz</a>  |
| linux | <a href ="https://dl.espressif.com/dl/espressif-ide//Espressif-IDE-2.3.0-beta-linux.gtk.x86_64.tar.gz">Espressif-IDE-2.3.0-beta-linux.gtk.x86_64.tar.gz</a>  |

### macOS security notice
On macOS, if you download the archive with the browser, the strict security checks on recent macOS will prevent it to run, and complain that the program is damaged. That’s obviously not true, and the fix is simple, you need to remove the `com.apple.quarantine` extended attribute.
```
$ xattr -d com.apple.quarantine ~/Downloads/Espressif-IDE-2.3.0-beta-macosx.cocoa.x86_64.tar.gz
```
After un-archiving, if the application still complains, check/remove the attribute from the Espressif-IDE.app folder too:
```
$ xattr -dr com.apple.quarantine ~/Downloads/Espressif-IDE.app
```
