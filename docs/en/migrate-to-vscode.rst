Migrating from Espressif-IDE to the ESP-IDF VS Code Extension
=============================================================

:link_to_translation:`zh_CN:[中文]`

A practical migration guide for existing Espressif-IDE / ESP-IDF Eclipse Plugin users.

Espressif-IDE 4.5.0 is the final feature release of Espressif-IDE and the ESP-IDF Eclipse plugin. Existing users can continue to maintain current projects, but new projects are encouraged to use the ESP-IDF VS Code Extension.

This guide shows how to open an existing ESP-IDF project in VS Code and reproduce the workflows you already know from Espressif-IDE: configure, build, flash, monitor, and debug.

What to expect
--------------

You do **not** need to recreate your ESP-IDF project.

Most project-level ESP-IDF configuration is portable and can be reused directly in VS Code. The parts that may need manual attention are mainly IDE-specific settings stored by Eclipse outside the project itself.

Usually reusable without changes
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

- Source code and ``CMakeLists.txt``
- ``sdkconfig`` and ``sdkconfig.defaults*``
- ``dependencies.lock``
- ``idf_component.yml``
- Managed components
- Local/custom components

May require manual setup
~~~~~~~~~~~~~~~~~~~~~~~~

- Custom build directory
- JTAG / OpenOCD board configuration
- Adapter-specific settings
- VS Code debug configuration (``.vscode/launch.json``)
- Custom debug values from Eclipse ``.launch`` files
- Environment-specific build cache

.. important::

    Keep the original Eclipse workspace, including its ``.metadata`` directory, until Build, Flash, Monitor, and Debug have all been verified in VS Code. Some Eclipse-specific settings are stored in the workspace rather than in the project directory.

Eclipse to VS Code: quick reference
-----------------------------------

.. list-table::
   :header-rows: 1
   :widths: 18 41 41

   * - Task
     - Espressif-IDE / Eclipse Plugin
     - ESP-IDF VS Code Extension
   * - Open project
     - Import/open project in an Eclipse workspace
     - **File → Open Folder...**
   * - Select target
     - Project/toolbar target configuration
     - ESP-IDF target/device selection from the status bar or Command Palette
   * - Select flash method
     - Eclipse launch/flash configuration
     - Select **UART** or **JTAG** in the ESP-IDF status bar/settings
   * - Select port
     - Serial configuration
     - Select the device port from the ESP-IDF status bar
   * - Full Clean
     - Clean/reconfigure project
     - **ESP-IDF: Full Clean**
   * - Build
     - Build toolbar / CDT Build
     - **ESP-IDF: Build your project**
   * - Menuconfig
     - ESP-IDF SDK Configuration Editor
     - **ESP-IDF: SDK Configuration Editor (Menuconfig)**
   * - Flash
     - Serial/JTAG flash action
     - **ESP-IDF: Flash your project**
   * - Monitor
     - Serial Monitor
     - **ESP-IDF: Monitor your device**
   * - Debug
     - Eclipse ``.launch`` configuration
     - Generate ``.vscode`` config if needed, then start Debug

Familiar controls, different locations
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Espressif-IDE provides common project actions through the Eclipse toolbar:

.. image:: ../_static/migration/eclipse_toolbar.png
   :alt: Espressif-IDE toolbar

In VS Code, equivalent actions are available through the **ESP-IDF status bar**, **Command Palette**, and **Run and Debug** view:

.. image:: ../_static/migration/vscode_status_bar.png
   :alt: VS Code ESP-IDF status bar

Optional: AI-assisted migration
-------------------------------

If your AI coding agent supports ``AGENTS.md``, download the :download:`migration AGENTS.md <migration/AGENTS.md>` and place it in the root of your ESP-IDF project.

It provides migration-specific instructions for inspecting the existing project and Eclipse workspace metadata, then migrating supported build, JTAG/OpenOCD, and debug settings to their VS Code equivalents.

You can start with a simple prompt:

.. code-block:: text

   Migrate this existing Espressif-IDE project to the ESP-IDF VS Code Extension.

The agent should apply settings automatically when the mapping is unambiguous and report only what still requires manual review. Actions that modify a connected device, such as flashing firmware, should require user confirmation.

.. toctree::
    :maxdepth: 1

    Migration walkthrough <migration-walkthrough>
    Advanced migration cases <migration-advanced>
    Migration Troubleshooting <migration-troubleshooting>
    Final migration checklist <migration-checklist>
