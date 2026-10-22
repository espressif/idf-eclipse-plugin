从 Espressif-IDE 迁移到 ESP-IDF VS Code 扩展
============================================

:link_to_translation:`en:[English]`

面向现有 Espressif-IDE / ESP-IDF Eclipse 插件用户的迁移指南。

Espressif-IDE 4.5.0 是 Espressif-IDE 与 ESP-IDF Eclipse 插件的最终功能版本。现有用户可以继续维护当前项目，新项目建议使用 ESP-IDF VS Code 扩展。

本指南说明如何在 VS Code 中打开现有 ESP-IDF 项目，并复现 Espressif-IDE 中已熟悉的配置、构建、烧录、监视和调试流程。

预期结果
--------

**不需要** 重新创建 ESP-IDF 项目。

项目级 ESP-IDF 配置大多可以直接在 VS Code 中复用。需要手工处理的部分，主要是 Eclipse 保存在项目目录之外的 IDE 设置。

通常无需修改即可复用
~~~~~~~~~~~~~~~~~~~~

- 源代码和 ``CMakeLists.txt``
- ``sdkconfig`` 与 ``sdkconfig.defaults*``
- ``dependencies.lock``
- ``idf_component.yml``
- 托管组件
- 本地/自定义组件

可能需要手工设置
~~~~~~~~~~~~~~~~

- 自定义构建目录
- JTAG / OpenOCD 开发板配置
- 适配器相关设置
- VS Code 调试配置（``.vscode/launch.json``）
- Eclipse ``.launch`` 文件中的自定义调试值
- 与环境相关的构建缓存

.. important::

    在 VS Code 中验证构建、烧录、监视和调试全部通过之前，请保留原来的 Eclipse 工作区，包括其中的 ``.metadata`` 目录。部分 Eclipse 设置保存在工作区中，而不是项目目录中。

Eclipse 与 VS Code 对照
-----------------------

.. list-table::
   :header-rows: 1
   :widths: 18 41 41

   * - 任务
     - Espressif-IDE / Eclipse 插件
     - ESP-IDF VS Code 扩展
   * - 打开项目
     - 在 Eclipse 工作区中导入/打开项目
     - **文件 → 打开文件夹...**
   * - 选择目标
     - 项目/工具栏中的目标配置
     - 从状态栏或命令面板选择 ESP-IDF 目标/设备
   * - 选择烧录方式
     - Eclipse 启动/烧录配置
     - 在 ESP-IDF 状态栏或设置中选择 **UART** 或 **JTAG**
   * - 选择端口
     - 串口配置
     - 从 ESP-IDF 状态栏选择设备端口
   * - 完全清理
     - 清理/重新配置项目
     - **ESP-IDF: Full Clean**
   * - 构建
     - 构建工具栏 / CDT 构建
     - **ESP-IDF: Build your project**
   * - Menuconfig
     - ESP-IDF SDK 配置编辑器
     - **ESP-IDF: SDK Configuration Editor (Menuconfig)**
   * - 烧录
     - 串口/JTAG 烧录操作
     - **ESP-IDF: Flash your project**
   * - 监视
     - 串口监视器
     - **ESP-IDF: Monitor your device**
   * - 调试
     - Eclipse ``.launch`` 配置
     - 按需生成 ``.vscode`` 配置，然后开始调试

熟悉的操作，不同的位置
~~~~~~~~~~~~~~~~~~~~~~

Espressif-IDE 通过 Eclipse 工具栏提供常用项目操作：

.. image:: ../_static/migration/eclipse_toolbar.png
   :alt: Espressif-IDE 工具栏

在 VS Code 中，对应操作位于 **ESP-IDF 状态栏**、**命令面板** 和 **运行和调试** 视图：

.. image:: ../_static/migration/vscode_status_bar.png
   :alt: VS Code ESP-IDF 状态栏

可选：借助 AI 迁移
------------------

如果使用的 AI 编程代理支持 ``AGENTS.md``，请下载 :download:`迁移用 AGENTS.md <migration/AGENTS.md>`，并放到 ESP-IDF 项目根目录。

该文件说明如何检查现有项目和 Eclipse 工作区元数据，并把支持的构建、JTAG/OpenOCD 和调试设置迁移到 VS Code 中的对应项。

可以从下面这条提示开始：

.. code-block:: text

   Migrate this existing Espressif-IDE project to the ESP-IDF VS Code Extension.

映射明确时，代理应自动应用设置，并只报告仍需人工确认的部分。会改动已连接设备的操作（例如烧录固件）应先征得用户确认。

.. toctree::
    :maxdepth: 1

    迁移步骤 <migration-walkthrough>
    进阶迁移场景 <migration-advanced>
    迁移故障排查 <migration-troubleshooting>
    迁移完成检查清单 <migration-checklist>
