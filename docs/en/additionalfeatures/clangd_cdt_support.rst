.. _clangd_cdt_support:

Espressif-IDE LSP Support for C/C++ Editor
==========================================

The Espressif-IDE 3.0.0 (and higher) now includes the `Eclipse CDT-LSP <https://github.com/eclipse-cdt/cdt-lsp/>`, enabling support for the latest C/C++ standards and providing an LSP-based C/C++ Editor. This editor, powered by the `LLVM <https://clangd.llvm.org/>` clangd C/C++ language server, offers advanced functionality for ESP-IDF developers.

In line with this enhancement, we've discontinued support for the standard CDT Editor/Indexer, as it only offers support for up to C++ 14. This has been replaced with a new LSP editor, especially considering that ESP-IDF now utilizes C++ 20 (with GCC 11.2) in v5.0.x, transitions to C++ 23 (with GCC 12.1) in v5.1, and to C++ 23 with GCC 13.1 in v5.2.

The LSP powered C/C++ editor greatly benefits ESP-IDF developers by aligning with the latest language standards and compiler versions, enhancing productivity, and improving code quality.

You can find more details on the LSP based C/C++ Editor features `here <https://github.com/eclipse-cdt/cdt-lsp/>`_.

Prerequisites
-------------
* You need to have Espressif-IDE 3.0.0 (and higher) to have access to the LSP powered C/C++ editor.
* If you are updating Eclipse CDT or Espressif-IDE via the update site, you need to select the ESP-IDF Eclipse Plugin and its dependencies, as shown below:

  .. image:: ../../../media/clangd/cdtlsp_updatesite.png

Clangd Configuration
--------------------

By default, the esp-clang toolchain is installed as a part of the ESP-IDF tools installation process, and clangd **Path** is configured in the preferences.

The **Drivers** path and **--compile-commands-dir** path will be configured based on the selected target (esp32, esp32c6 etc.) and the project you're building.

However, if there are any issues in configuration, this can be configured in the following way:

1. Go to `Window` > `Preferences` > `C/C++` > `Editor(LSP)`
2. Navigate to `clangd` node
3. Provide `Drivers` path as shown in the screenshot.
4. Set `--compile-commands-dir=/project/build` in the additional argument section.
5. Click on `Apply and Close`.

   .. image:: ../../../media/clangd/clangd_config.png

By default, when you create a new project, a `.clangd` configuration file is created with the following arguments.

However, if you are dealing with an existing project, please create a `.clangd` file at the root of the project and add the following content.

.. code-block:: yaml

    CompileFlags:
      CompilationDatabase: build
      Remove: [-m*, -f*]

How to fix Unknown argument error when navigating to the esp-idf components
----------------------------------------------------------------------------------------

If you are seeing the following error markers while navigating to the esp-idf components source code:

.. code-block:: none

    Multiple markers at this line
    - Unknown argument: '-fno-tree-switch-conversion' [drv_unknown_argument]
    - Unknown argument: '-fno-shrink-wrap' [drv_unknown_argument]
    - Unknown argument: '-fstrict-volatile-bitfields' [drv_unknown_argument]

Please follow the steps below to fix it:

1. Download the script from `here <https://github.com/espressif/idf-eclipse-plugin/tree/master/resources/resources/fix_compile_commands/fix_compile_commands.py>`_.

2. Invoke the script from the project post build step. Here is example for `CMakeLists.txt <https://github.com/espressif/idf-eclipse-plugin/blob/master/resources/resources/fix_compile_commands/CMakeLists.txt>`_:

   .. code-block:: cmake

      if(EXISTS "${CMAKE_SOURCE_DIR}/fix_compile_commands.py")
        add_custom_target(
            fix_clangd ALL
            COMMAND ${CMAKE_COMMAND} -E echo "Running fix_compile_commands.py..."
            COMMAND ${CMAKE_COMMAND} -E env python3 ${CMAKE_SOURCE_DIR}/fix_compile_commands.py
            COMMENT "Cleaning compile_commands.json for clangd"
            VERBATIM
        )
      endif()

3. Now run the build, the script will remove the -m* and -f* flags from the compile_commands.json file which are unknown to clangd.

4. Now, you can navigate to the esp-idf components source code without any errors.

Disable CDT Indexer
-------------------

With Espressif-IDE 3.0.0 (and higher), the CDT Indexer is disabled by default; instead, the LSP Indexer server will be used for code analysis.

If, for some reason, it is not disabled, please follow the steps below to disable it.

1. Go to `Window` > `Preferences` > `C/C++` > `Indexer`
2. Uncheck `Enable Indexer` option and then click on `Apply and Close`.

   .. image:: ../../../media/clangd/cdt_indexer_disable.png
