调试项目
========

:link_to_translation:`en:[English]`

.. |debug_icon| image:: ../../media/icons/debug.png
   :height: 16px
   :align: middle

每个 ESP-IDF 项目只有一个 ``ESP-IDF Application`` 启动配置，该配置同时用于运行（构建和烧录）和调试，无需单独创建调试配置。启动栏中第一个下拉菜单 **启动模式** 决定该配置的行为，以及编辑配置时显示的设置：

- ``Run`` 模式：构建、烧录和串口相关设置（``Build Settings``、``Main``、``Environment`` 和 ``Common`` 标签页）。
- ``Debug`` 模式：OpenOCD 和 GDB 相关设置（``Main``、``Debugger``、``Startup``、``Source``、``Common`` 和 ``SVD`` 标签页）。

.. note::

    如果使用 Windows 操作系统，可能需要通过 Zadig 安装驱动程序，才能成功运行调试会话。有关详细说明，请参考此 `指南 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/api-guides/jtag-debugging/configure-ft2232h-jtag.html#usb>`_。

在大多数情况下，只需检查配置中指定的开发板是否与实际使用的开发板一致：

1. 在启动栏的第二个下拉菜单中选择项目的配置。
2. 在 **启动模式** 下拉菜单中选择 ``Debug``。
3. 点击配置旁边的 ``Edit`` （齿轮）图标。
4. 在 ``Debugger`` 标签页中，检查 ``Board`` 和 ``Config options`` 是否与你的开发板一致，然后点击 ``OK``。
5. 点击 ``Debug`` 图标 |debug_icon| 以开始调试。

.. image:: ../../media/unified_launch_config/switch_mode_and_edit.gif
   :alt: 切换启动模式并编辑配置

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/1fb0fb9b-a02a-4ed1-bdba-b4b4d36d100f
   :alt: 调试过程

.. important::

    只有在 **启动模式** 设置为 ``Debug`` 时才能编辑调试设置；只有在设置为 ``Run`` 时才能编辑运行设置（如烧录参数或构建文件夹）。如果找不到所需的标签页，请切换模式后重新打开配置编辑器。

要了解更多有关调试设置的内容，请参阅 :ref:`ESP-IDF OpenOCD 调试 <OpenOCDDebugging>`。
