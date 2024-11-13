Espressif-IDE
=========================
:link_to_translation:`zh_CN:[中文]`

Espressif-IDE is an Integrated Development Environment (IDE) based on `Eclipse CDT <https://github.com/eclipse-cdt/>`_ for developing IoT Applications using the `ESP-IDF <https://github.com/espressif/esp-idf>`_. It's a standalone and customized IDE built specifically for ESP-IDF. Espressif-IDE comes with the IDF Eclipse plugins, essential Eclipse CDT plugins, and other third-party plugins from the Eclipse platform to support building ESP-IDF applications.

The plug-in runs on `macOS`, `Windows` and `Linux` platforms.

.. note:: 
    Espressif-IDE version 3.0 and later supports ESP-IDF version 5.x and above. For ESP-IDF version 4.x and earlier, please use Espressif-IDE version `2.12.1 <https://github.com/espressif/idf-eclipse-plugin/releases/tag/v2.12.1>`_.

.. image:: ../../media/espressif-ide.png
    :alt: Espressif-IDE
    :align: center
    
Features
----------------

- Auto-configuration of the build environment variables
- Integrated toolchain configuration
- New Project Wizards and getting started with ESP-IDF examples
- LSP-based editor for advanced editing, compiling, and syntax coloring features
- Pre-built function header and function definition navigation
- ESP-IDF and ESP-IDF tools installation and configuration directly from the IDE
- SDK configuration editor for project-specific settings
- Integrated CMake editor plug-in for editing CMake files such as CMakeLists.txt
- CMake-based build support
- Support for UART and JTAG flashing
- Customized ESP-IDF OpenOCD debugging with pre-built configuration and settings
- Integrated ESP-IDF serial monitor
- ESP-IDF terminal with the pre-configured ESP-IDF build environment
- Application size analysis editor for analyzing the static memory footprint of your application
- Supports heap profiling for memory analysis and finding memory leaks
- Supports GDB stub debugging and application-level tracing
- English and Chinese language support for the IDE

For more about this project please see https://github.com/espressif/idf-eclipse-plugin

.. toctree::
    :maxdepth: 1

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
    Troubleshooting<troubleshooting>
    FAQs<faqs>
    Downloads<downloads>
