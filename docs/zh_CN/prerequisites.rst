准备工作
========

:link_to_translation:`en:[English]`

本文档概述了安装和运行 Espressif-IDE 所需的基本要求，从而实现使用 ESP-IDF 构建应用程序。

硬件
----

- 一台使用 Windows、Linux 或 macOS 操作系统的计算机
- 一块带有 USB 转串口接口/调试端口，或两者兼有的 ESP32 开发板
- 一根与开发板兼容的 USB 线缆（数据与供电）

.. Note::

    目前，部分开发板使用 USB Type-C 连接器。请确保准备的 USB 线可正确连接开发板。

软件
----

运行 Espressif-IDE 的最低要求如下：

- `Java 21 <https://www.oracle.com/cn/java/technologies/downloads/>`_ 及以上
- `Python 3.12 <https://www.python.org/downloads/>`_ 及以上
- `Git <https://git-scm.com/downloads>`_
- `ESP-IDF 依赖项 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/get-started/linux-macos-setup.html#get-started-prerequisites>`_，取决于所使用的操作系统

.. note::

    请确保已正确安装并配置 Java、Python 和 Git，且已在系统的 PATH 环境变量中可用。
