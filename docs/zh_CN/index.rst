Espressif-IDE
=============

:link_to_translation:`en:[English]`

Espressif-IDE 是基于 `Eclipse CDT <https://github.com/eclipse-cdt/>`_ 的集成开发环境 (IDE)，用于开发 `ESP-IDF <https://github.com/espressif/esp-idf>`_ 物联网应用。该独立 IDE 专为 ESP-IDF 打造，内置了 ESP-IDF 的 Eclipse 插件、Eclipse CDT 的基本插件，以及其他辅助开发的第三方插件，可支持 ESP-IDF 应用的构建。

该插件可在 **macOS**、**Windows** 和 **Linux** 平台上运行。

.. note::

    Espressif-IDE 3.0 及以上版本支持 ESP-IDF 5.x 及以上版本。对于 ESP-IDF 4.x 及更早版本，请使用 Espressif-IDE `2.12.1 版本 <https://github.com/espressif/idf-eclipse-plugin/releases/tag/v2.12.1>`_。

.. image:: ../../media/espressif-ide.png
    :alt: Espressif-IDE
    :align: center
    
功能
----

- 自动配置构建环境的变量
- 配置集成的工具链
- 新建项目向导与 ESP-IDF 入门示例
- 基于 LSP 的编辑器，提供高级编辑、编译及语法高亮功能
- 自动生成函数声明与快速跳转到函数定义
- 可在 IDE 中直接安装和配置 ESP-IDF 及 ESP-IDF 工具
- 用于项目专属设置的 SDK 配置编辑器
- 集成的 CMake 编辑器插件，用于编辑 CMake 文件，例如 ``CMakeLists.txt``
- 基于 CMake 的构建支持
- 支持通过 UART 与 JTAG 烧录
- ESP-IDF 专用 OpenOCD 调试功能，且内置预设配置
- 集成的 ESP-IDF 串口监视器
- 已预先配置好 ESP-IDF 构建环境的 ESP-IDF 专用终端
- 应用大小分析编辑器，用于分析应用的静态内存占用情况
- 支持堆分析，用于内存使用情况分析和内存泄漏检测
- 支持 GDB stub 调试与应用级跟踪
- IDE 支持中英双语界面

更多有关该项目的信息请参阅 https://github.com/espressif/idf-eclipse-plugin。

目录
----

.. toctree::
    :maxdepth: 1

    准备工作 <prerequisites>
    安装 <installation>
    启动项目 <startproject>
    连接设备 <connectdevice>
    构建项目 <buildproject>
    配置项目 <configureproject>
    烧录设备 <flashdevice>
    监视输出 <monitoroutput>
    调试项目 <debugproject>
    其他 IDE 功能 <additionalfeatures>
    故障排查 <troubleshooting>
    常见问题 <faqs>
    下载 <downloads>
