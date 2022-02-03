# Espressif IDE

## Overview
Espressif IDE is an Integrated Development Environment(IDE) based on Eclipse CDT for developing IoT Applications using the ESP-IDF framework. It's a standalone and customized IDE built specifically for ESP-IDF. Espressif IDE comes with the IDF Eclipse plugins, essential Eclipse CDT plugins, and other third-party plugins from the Eclipse platform to support building ESP-IDF applications. 

![](images/espressifide_splash.bmp)

## Features:
- Easy-to-use IDE built on Eclipse CDT environment
- Specifically built for ESP-IDF application development
- Auto-configuration of the build environment variables
- Integrated toolchain configuration
- New Project Wizards and getting started ESP-IDF examples
- Advanced editing, compiling, and editing with syntax coloring features
- Pre-built function header and function definition navigation
- ESP-IDF and ESP-IDF Tools installation and configuration directly from IDE
- SDK Configuration editor for project-specific settings 
- Integrated CMake Editor plug-in for editing CMake files such as CMakeLists.txt
- CMake based build support
- Support for UART and JTAG flashing
- Customized ESP-IDF OpenOCD Debugging with pre-built configuration and settings
- GDB Hardware debugging
- Integrated ESP-IDF serial monitor
- ESP-IDF Terminal with the pre-configured ESP-IDF build environment
- Application size analysis editor for analyzing static memory footprint of your application
- Supports Heap profiling for memory analysis and finding memory leaks
- Supports GDB Stub debugging and Application level tracing
- Supports esp32,esp32s2, esp32s3 and esp32c3 chips
- English and Chinese language support for IDE
- Extensible IDE for other third-party plugins from Eclipse eco-system
- Host operating systems supported: Windows, macOS, and Linux 

## Downloads
### Espressif-IDE v2.4.0

| OS  | Download |
| ------------- | ------------- |
| Windows  | <a href ="https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-2.4.0-win32.win32.x86_64.zip">Espressif-IDE-2.4.0-win32.win32.x86_64.zip</a>  |
| macosx | <a href ="https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-2.4.0-macosx.cocoa.x86_64.tar.gz">Espressif-IDE-2.4.0-macosx.cocoa.x86_64.tar.gz</a>  |
| linux | <a href ="https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-2.4.0-linux.gtk.x86_64.tar.gz">Espressif-IDE-2.4.0-linux.gtk.x86_64.tar.gz</a>  |

### macOS security notice
On macOS, if you download the archive with the browser, the strict security checks on recent macOS will prevent it to run, and complain that the program is damaged. That’s obviously not true, and the fix is simple, you need to remove the `com.apple.quarantine` extended attribute.
```
$ xattr -d com.apple.quarantine ~/Downloads/Espressif-IDE-2.4.0-macosx.cocoa.x86_64.tar.gz
```
After un-archiving, if the application still complains, check/remove the attribute from the Espressif-IDE.app folder too:
```
$ xattr -dr com.apple.quarantine ~/Downloads/Espressif-IDE.app
```
### Espressif-IDE v2.4.0 Installer for Windows
Espressif-IDE Installer (All-in-one) is an offline installer and it comes with all the required dependencies to work with the ESP-IDF Application development.

Installer includes:
- Embedded Python
- Cross-compilers
- OpenOCD
- CMake and Ninja build tools
- ESP-IDF
- Espressif-IDE (includes essential Eclipse CDT Packages, IDF Eclipse Plugins and other third-party plugins required)
- Amazon Corretto OpenJDK

Espressif-IDE Installer for Windows is available <a heref="https://dl.espressif.com/dl/esp-idf/">here</a>
