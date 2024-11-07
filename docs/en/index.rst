Espressif-IDE
=========================
:link_to_translation:`zh_CN:[中文]`

Espressif-IDE is an Integrated Development Environment(IDE) based on Eclipse CDT for developing IoT Applications using the ESP-IDF framework. It's a standalone and customized IDE built specifically for ESP-IDF. Espressif-IDE comes with the IDF Eclipse plugins, essential Eclipse CDT plugins, and other third-party plugins from the Eclipse platform to support building ESP-IDF applications.

The plug-in runs on macOS, Windows and Linux platforms.

.. note:: 
    Espressif-IDE version 3.0 and later supports ESP-IDF version 5.x and above. For ESP-IDF version 4.x and earlier, please use Espressif-IDE version 2.12.1.

Features
----------------

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


For more about this project please see https://github.com/espressif/idf-eclipse-plugin

.. toctree::
    :maxdepth: 2

    Prerequisites <prerequisites>
    Installation <installation>
    Start a Project<startproject>
    Connect Your Device<connectdevice>
    Build the Project<buildproject>
    Configure Your Project<configureproject>
    Flash onto the Device<flashdevice>
    Monitor the Output<monitoroutput>
    Debug Your Project<debugproject>
    Additional IDE Features<additionalfeatures>
    FAQs<faqs>
    Downloads<downloads>
