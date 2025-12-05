.. _clangd_cdt_support:

Espressif-IDE 对 C/C++ 编辑器的 LSP 支持
========================================

:link_to_translation:`en:[English]`

Espressif-IDE 3.0.0 及更高版本现已内置 `Eclipse CDT-LSP <https://github.com/eclipse-cdt/cdt-lsp/>`_，支持最新的 C/C++ 标准并提供基于 LSP 的 C/C++ 编辑器。该编辑器由 `LLVM <https://clangd.llvm.org/>`_ clangd C/C++ 语言服务器提供支持，为 ESP-IDF 开发者提供更强大的功能。

为配合此功能提升，已停止对标准 CDT 编辑器/索引器的支持，因为其最多支持 C++ 14。此编辑器已由全新的 LSP 编辑器代替，以适应 ESP-IDF 的语言升级：ESP-IDF 现已在 v5.0.x 中使用 C++ 20 (GCC 11.2)，在 v5.1 过渡到 C++ 23 (GCC 12.1)，并在 v5.2 使用 C++ 23 (GCC 13.1)。

基于 LSP 的 C/C++ 编辑器与最新的语言标准和编译器版本保持一致，可极大提升 ESP-IDF 的开发效率，改进代码质量。

关于基于 LSP 的 C/C++ 编辑器功能的更多信息，请参阅 `Eclipse cdt-lsp 仓库 <https://github.com/eclipse-cdt/cdt-lsp/>`_  文档。

准备工作
--------

* 需要 Espressif-IDE 3.0.0 及更高版本才能使用基于 LSP 的 C/C++ 编辑器。
* 如果通过更新站点升级 Eclipse CDT 或 Espressif-IDE，需要选择 ESP-IDF Eclipse 插件及其依赖项，如下图所示。

  .. image:: ../../../media/clangd/cdtlsp_updatesite.png

Clangd 配置
-----------

默认情况下，esp-clang 工具链会在 ESP-IDF 工具的安装过程中自动安装，并且会在偏好设置中配置好 clangd 的 ``Path``。

``Drivers`` 路径和 ``--compile-commands-dir`` 路径会根据所选目标（esp32、esp32c6 等）以及当前正在构建的项目自动配置。

如果自动配置有问题，可按以下方式手动配置：

1.  前往 ``Window`` > ``Preferences`` > ``C/C++`` > ``Editor(LSP)``。
2.  定位到 ``clangd`` 节点。
3.  按截图所示填写 ``Drivers`` 路径。
4.  在附加参数区域设置 ``--compile-commands-dir=/project/build``。
5.  点击 ``Apply and Close``。

    .. image:: ../../../media/clangd/clangd_config.png

创建新项目时，会默认生成一个 ``.clangd`` 配置文件。

如果从旧版工作区迁移已有的项目，请在项目根目录处手动创建 ``.clangd`` 文件并添加如下内容：

.. code-block:: yaml

    CompileFlags:
        CompilationDatabase: build
        Remove: [-m*, -f*]

如何解决在浏览 ESP-IDF 组件时出现的 "Unknown Argument" 错误
-----------------------------------------------------------

如果在浏览 ESP-IDF 组件源码时看到如下错误标记：

.. code-block:: none

    Multiple markers at this line
    - Unknown argument: '-fno-tree-switch-conversion' [drv_unknown_argument]
    - Unknown argument: '-fno-shrink-wrap' [drv_unknown_argument]
    - Unknown argument: '-fstrict-volatile-bitfields' [drv_unknown_argument]

请按以下步骤修复：

1.  下载脚本 `fix_compile_commands.py <https://github.com/espressif/idf-eclipse-plugin/tree/master/resources/fix_compile_commands/fix_compile_commands.py>`_。

2. 在项目的 post build 阶段中调用该脚本。以下为 `CMakeLists.txt <https://github.com/espressif/idf-eclipse-plugin/blob/master/resources/fix_compile_commands/CMakeLists.txt>`_ 示例：

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

3.  运行构建，该脚本会从 ``compile_commands.json`` 文件中移除 clangd 无法识别的 ``-m*`` 和 ``-f*`` 标志。

4.  现在浏览 ESP-IDF 组件源码将不会出现报错。

禁用 CDT 索引器
---------------

在 Espressif-IDE 3.0.0 及更高版本中，默认禁用 CDT 索引器，代码分析由 LSP 索引器服务器执行。

如果 CDT 索引器因为某些原因未被自动禁用，可参照下列步骤手动将其禁用。

1.  前往 ``Window`` > ``Preferences`` > ``C/C++`` > ``Indexer``。
2.  取消勾选 ``Enable Indexer`` 选项，然后点击 ``Apply and Close``。

    .. image:: ../../../media/clangd/cdt_indexer_disable.png
