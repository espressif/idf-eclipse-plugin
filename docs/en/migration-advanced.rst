Advanced migration cases
========================

:link_to_translation:`zh_CN:[中文]`

Some Espressif-IDE settings live outside the portable project files. Review this page when Build succeeds but Flash, Debug, or the build directory still does not match the Eclipse workflow. The main steps are in :doc:`migration-walkthrough`.

Custom build directory
----------------------

A custom build directory used by Espressif-IDE may be stored in the Eclipse launch configuration rather than in the portable project files.

For example:

.. code-block:: xml

   <stringAttribute
       key="com.espressif.idf.launch.serial.core.idfBuildFolderPath"
       value="/path/to/custom-build"/>

VS Code supports a custom build directory, but this value is not currently imported automatically.

If your project depends on a custom build path:

1. Configure the equivalent build directory in VS Code using the `idf.buildPath <https://github.com/espressif/vscode-esp-idf-extension/blob/master/docs_espressif/en/settings.rst#build>`_ setting.

   For example:

   .. code-block:: json

      {
          "idf.buildPath": "${workspaceFolder}/custom-build"
      }

2. Reconfigure or clean generated state if necessary.
3. Verify Menuconfig.
4. Verify Build.
5. Verify Flash if it is part of your workflow.
6. Verify Debug if it is part of your workflow.

This matters not only for compilation. External scripts and Clangd may also depend on files such as ``compile_commands.json`` generated inside the active build directory.

JTAG / OpenOCD settings
-----------------------

JTAG board and adapter settings are IDE-specific and may need to be selected again in VS Code.

If JTAG Flash does not work after migration:

1. Verify the ESP-IDF target and re-select the exact board/device.
2. Confirm JTAG is selected as the flash method.
3. Verify the intended adapter when multiple devices are connected.

Custom Eclipse debug configuration
----------------------------------

Espressif-IDE debug settings are not automatically converted into VS Code debug settings.

Review the old Eclipse ``.launch`` configuration if your workflow uses custom values such as:

- GDB, TCL, or Telnet ports
- ``stopAt=app_main``
- semihosting
- flash-before-debug behavior
- reset/start behavior
- custom GDB commands, arguments, or timeouts

Start from the standard VS Code Debug configuration, then migrate or recreate only the custom behavior your project actually requires.

.. caution::

    Eclipse workspace metadata can contain stale or duplicated values. Do not blindly copy every target-related value into VS Code. Prefer the actual ESP-IDF project configuration when resolving conflicts.

C/C++ code navigation and IntelliSense
--------------------------------------

Espressif-IDE uses clangd for C/C++ code navigation, diagnostics, and header resolution.

In VS Code, the default ESP-IDF setup uses the Microsoft C/C++ extension.

If you prefer to continue using clangd, install the clangd extension and run:

**ESP-IDF: Configure project for ESP-Clang**

See the `ESP-IDF VS Code Extension documentation <https://docs.espressif.com/projects/vscode-esp-idf-extension/en/latest/configureproject.html>`_ for more details.
