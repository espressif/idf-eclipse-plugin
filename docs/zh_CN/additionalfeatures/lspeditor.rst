LSP C/C++ 编辑器
================

:link_to_translation:`en:[English]`

从 Espressif IDE 3.0.0 开始，LSP 编辑器成为默认的代码编辑器，其行为与之前的默认编辑器存在显著差异，主要区别如下所示。

格式化
------

如需自定义格式化，请打开项目中的 ``.clang-format`` 文件。默认情况下，文件包含以下内容：

.. code-block:: none

    BasedOnStyle: LLVM
    UseTab: Always
    IndentWidth: 4
    TabWidth: 4
    PackConstructorInitializers: NextLineOnly
    BreakConstructorInitializers: AfterColon
    IndentAccessModifiers: false
    AccessModifierOffset: -4

也可以使用 ``DisableFormat: true`` 选项对特定文件夹禁用格式化。例如，如果希望在如下结构的项目中为 ``managed_components`` 文件夹禁用格式化：

.. code-block:: none

    project
     ├── managed_components
     │       └── .clang-format
     ├── main
     └── .clang-format

在 ``managed_components`` 文件夹中的 ``.clang-format`` 文件中添加 ``DisableFormat: true`` 选项。该选项会指示 ClangFormat 完全忽略此 ``.clang-format`` 文件及其在 ``managed_components`` 目录下定义的所有格式化规则。

有关可用风格选项的更多信息，请参阅 `可配置的格式化风格选项 <https://clang.llvm.org/docs/ClangFormatStyleOptions.html#configurable-format-style-options>`_。

搜索
----

在基于 LSP 的 C/C++ 编辑器中，右键菜单里的 ``Search Text`` 选项目前不可用。不过，你可以暂时通过工具栏菜单中的 ``Search`` > ``Text`` > ``Workspace`` 来实现相同的功能。

内联提示
--------

LSP 编辑器默认启用内联提示。如果你不想使用此功能，可通过编辑 ``.clangd`` 文件将其禁用：

.. code-block:: none

    CompileFlags:
      CompilationDatabase: build
      Remove: 
        - -m*
        - -f*

    InlayHints:
      Enabled: No

搜索 ESP-IDF 组件
-----------------

如需浏览 ESP-IDF 组件，请执行以下步骤：

1. 创建一个新项目。
2. 将 ESP-IDF 组件文件夹作为虚拟文件夹添加到新建项目中。
3. 使用快捷键 **Ctrl + Shift + T** 或 **Ctrl + Shift + R**。
4. 现在可以浏览 ESP-IDF 组件文件。
5. 若要搜索特定函数或关键字，请使用工具栏中的 ``Search`` 菜单。

创建虚拟文件夹
~~~~~~~~~~~~~~

1. 前往 ``New`` > ``Folder``。
2. 点击 ``Advanced``。
3. 选择 ``Link to alternate Location (Linked Folder)``。
4. 点击 ``Browse`` 并选择 ``ESP-IDF components`` 文件夹。

建议始终创建一个新项目，而不是修改现有项目，从而避免索引器在 ``components`` 文件夹中生成不必要的 Git 文件和错误标记。由于两个项目都位于同一个工作区中，你仍然可以在整个工作区范围内进行搜索。

如果在 LSP 编辑器中使用 Clangd 配置时遇到任何问题，请参阅 :ref:`Clangd 配置 <clangd_cdt_support>` 指南。

参考文档
--------

.. toctree::
    :maxdepth: 1

    Clangd CDT 支持与配置 <clangd_cdt_support>
    配置 Clang 工具链 <clangtoolchain>
