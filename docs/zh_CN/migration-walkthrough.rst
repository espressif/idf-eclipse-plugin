迁移步骤
========

:link_to_translation:`en:[English]`

在 ESP-IDF VS Code 扩展中打开现有 ESP-IDF 项目，并验证与 Espressif-IDE 相同的工作流程。哪些内容会自动复用、哪些仍需手工设置，见 :doc:`migrate-to-vscode`。

1. 打开现有项目
---------------

在 VS Code 中使用 **文件 → 打开文件夹...**，选择现有 ESP-IDF 项目的根目录。

.. image:: ../_static/migration/01_open_project.gif
   :alt: 打开现有项目

不需要新建 VS Code 项目，也不需要把源文件复制到新模板中。

打开文件夹后，确认项目仍包含常规 ESP-IDF 文件，例如：

.. code-block:: text

   project/
   ├── CMakeLists.txt
   ├── main/
   ├── sdkconfig
   ├── sdkconfig.defaults
   ├── dependencies.lock
   ├── managed_components/
   └── ...

2. 确认 ESP-IDF 环境
--------------------

确认 VS Code 使用的是项目所期望的 ESP-IDF 安装和版本。

如果所需的 ESP-IDF 版本已经安装，请在 ESP-IDF 扩展中选择该现有环境，而不是重新安装框架。

.. tip::

    先使用相同的 ESP-IDF 版本，迁移结果更容易验证。IDE 迁移完成后再单独升级项目。

3. 选择目标和开发板
-------------------

选择项目使用的 ESP-IDF 目标芯片；适用时，再选择具体的开发板/设备配置。

.. image:: ../_static/migration/02_set_target.gif
   :alt: 设置目标和开发板

.. important::

    使用 JTAG 时，即使芯片目标看起来已经正确，也请明确选择开发板/设备。正确的 ``IDF_TARGET`` 并不表示对应的 OpenOCD 开发板配置已经初始化。

例如，项目可能正确显示 ``esp32c6``，但 VS Code 仍保留着旧项目的 OpenOCD 配置。重新选择目标/开发板会刷新该项目的硬件配置。

4. 选择烧录方式
---------------

选择与现有流程一致的烧录方式。

.. image:: ../_static/migration/03_set_flash_method.gif
   :alt: 设置烧录方式

常见选择：

- **UART**：串口烧录
- **JTAG**：通过 OpenOCD/JTAG 烧录

如果迁移的是基于 JTAG 的流程，请先确认此项，再排查 OpenOCD 或调试问题。

5. 选择串口
-----------

进行 UART 烧录和串口监视时，选择设备端口。

.. image:: ../_static/migration/04_set_port.gif
   :alt: 设置串口

端口名称因平台而异，例如：

.. code-block:: text

   macOS:   /dev/tty.usbserial-...
   Linux:   /dev/ttyUSB0 或 /dev/ttyACM0
   Windows: COM3、COM4、...

.. _migration-full-clean:

6. 仅在需要时执行完全清理
-------------------------

不要无故清理项目。请先按现有项目正常使用。

如果 Menuconfig 或项目配置因过期或与环境绑定的构建缓存而失败，请运行：

**ESP-IDF: Full Clean**

.. image:: ../_static/migration/05_full_clean.gif
   :alt: 完全清理

.. note::

    恢复 Menuconfig 不一定需要完整构建项目。完全清理后重新打开 SDK 配置编辑器即可。

7. 构建项目
-----------

运行：

**ESP-IDF: Build your project**

.. image:: ../_static/migration/06_build_project.gif
   :alt: 构建项目

以下项目级状态无需按 IDE 做转换，应继续有效：

- 自定义 CMake 配置
- ``sdkconfig``
- ``sdkconfig.defaults*``
- 托管组件
- 本地/自定义组件
- 依赖锁定文件

如果构建成功，说明 ESP-IDF 项目本身已经正确迁移。

8. 打开 SDK 配置编辑器 / Menuconfig
-----------------------------------

运行：

**ESP-IDF: SDK Configuration Editor (Menuconfig)**

.. image:: ../_static/migration/07_menuconfig.gif
   :alt: SDK 配置编辑器

确认重要的现有配置仍然存在。

例如：

- 目标芯片相关配置
- Flash 大小
- 分区表选择
- 编译器优化
- 应用专用的 Kconfig 值

如果编辑器提示必须清理项目，原因是构建缓存不兼容，请返回 :ref:`migration-full-clean`。

9. 验证烧录
-----------

项目构建成功后，验证平时使用的硬件流程。

如果工作流包含烧录，请确认目标、烧录方式和端口/开发板正确，然后运行：

**ESP-IDF: Flash your project**

.. image:: ../_static/migration/08_flash_project.gif
   :alt: 烧录项目

UART 用户
~~~~~~~~~

配置正确的串口和目标后，UART 烧录通常即可工作。

JTAG 用户
~~~~~~~~~

如果 JTAG 烧录因 OpenOCD 适配器/设备错误而失败：

1. 重新选择目标以及具体的开发板/设备配置。
2. 确认烧录方式为 JTAG。
3. 连接了多个设备时，确认所选适配器正确。

10. 验证串口监视器
------------------

如果工作流包含串口监视，请运行：

**ESP-IDF: Monitor your device**

.. image:: ../_static/migration/09_monitor.gif
   :alt: 串口监视器

确认应用输出正常，并且运行的是预期固件。

这也适合验证自定义/本地组件：运行时输出可以确认这些组件已被编译、链接、烧录并执行。

11. 配置并验证调试
------------------

现有 Espressif-IDE 项目不一定包含 VS Code 调试配置。

如果缺少 ``.vscode/launch.json``，请运行：

**ESP-IDF: Add VS Code Configuration Folder**

然后从 VS Code 的 **运行和调试** 视图启动调试。

.. image:: ../_static/migration/10_debug_focus.gif
   :alt: 调试流程

生成 VS Code 配置文件夹后，默认调试即可使用。

.. warning::

    ``Add VS Code Configuration Folder`` 只会创建 VS Code 默认配置，**不会** 转换现有的 Eclipse ``.launch`` 文件。如果 Eclipse 调试配置包含自定义端口、半主机、复位行为、自定义 GDB 命令或其他非默认设置，请在默认调试可用后再手工核对。
