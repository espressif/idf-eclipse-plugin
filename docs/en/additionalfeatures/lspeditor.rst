LSP C/C++ Editor
================

:link_to_translation:`zh_CN:[中文]`

Starting with Espressif IDE 3.0.0, the LSP Editor becomes the default code editor and behaves differently from the previous default editor. The most notable differences are listed below.

Formatting
----------

To customize formatting, open the ``.clang-format`` file located in your project. By default, the file contains the following content:

.. code-block:: none

    BasedOnStyle: LLVM
    UseTab: Always
    IndentWidth: 4
    TabWidth: 4
    PackConstructorInitializers: NextLineOnly
    BreakConstructorInitializers: AfterColon
    IndentAccessModifiers: false
    AccessModifierOffset: -4

You can also disable formatting for specific folders by using the ``DisableFormat: true`` option. For example, if you want to disable formatting for the ``managed_components`` folder in a project structured like this:

.. code-block:: none

    project
     ├── managed_components
     │       └── .clang-format
     ├── main
     └── .clang-format

Add the ``DisableFormat: true`` option to the ``.clang-format`` file in the ``managed_components`` folder. This flag tells ClangFormat to completely ignore this specific ``.clang-format`` file and its formatting rules within the ``managed_components`` directory.

For more information about available style options, refer to `Configurable Format Style Options <https://clang.llvm.org/docs/ClangFormatStyleOptions.html#configurable-format-style-options>`_.

Search
------

The ``Search Text`` option in the right-click menu is currently unavailable in the LSP-based C/C++ Editor. However, as a workaround, you can use ``Search`` > ``Text`` > ``Workspace`` from the toolbar menu.

Inlay Hints
-----------

The LSP Editor has inlay hints enabled by default. If you prefer not to use them, you can disable this feature by editing the ``.clangd`` file:

.. code-block:: none

    CompileFlags:
      CompilationDatabase: build
      Remove: 
        - -m*
        - -f*

    InlayHints:
      Enabled: No

Searching ESP-IDF Components
----------------------------

To browse ESP-IDF components, follow these steps:

1. Create a new project.
2. Add the ESP-IDF components folder as a virtual folder to the newly created project.
3. Press **Ctrl + Shift + T** or **Ctrl + Shift + R**.
4. You should now be able to browse the ESP-IDF component files.
5. To search for a specific function or keyword, use the ``Search`` menu in the toolbar.

Creating a Virtual Folder
~~~~~~~~~~~~~~~~~~~~~~~~~

1. Navigate to ``New`` > ``Folder``.
2. Click on ``Advanced``.
3. Select ``Link to alternate Location (Linked Folder)``.
4. Click ``Browse`` and select the ``ESP-IDF components`` folder.

It is recommended to always create a new project instead of modifying your current one to avoid unnecessary Git files and error markers created by the indexer for the ``components`` folder. Since both projects reside in the same workspace, you will still be able to search across your entire workspace.

If you have any issues with the Clangd Configuration with the LSP Editor, please refer to the :ref:`Clangd Configuration <clangd_cdt_support>` guide.

References
----------

.. toctree::
    :maxdepth: 1

    Clangd CDT Support and Configuration <clangd_cdt_support>
    Clang Toolchain Configuration <clangtoolchain>
