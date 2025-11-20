调试项目
========

:link_to_translation:`en:[English]`

.. |debug_icon| image:: ../../media/icons/debug.png
   :height: 16px
   :align: middle

在大多数情况下，开始调试 ESP-IDF 项目前只需要完成以下两步：

1. 创建调试配置。
2. 检查已创建配置中指定的开发板是否与实际使用的开发板一致。

.. note::

    如果使用 Windows 操作系统，可能需要通过 Zadig 安装驱动程序，才能成功运行调试会话。有关详细说明，请参考此 `指南 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/api-guides/jtag-debugging/configure-ft2232h-jtag.html#usb>`_。

创建调试配置的最快方式如下：

1. 在启动栏中展开配置列表并点击 ``New Launch Configuration...``。
2. 选择 ``ESP-IDF GDB OpenOCD Debugging``，然后双击该选项或点击 ``Next``。
3. 上述选项会打开新的调试配置预览界面，用于查看开发板设置。

创建调试配置后，即可继续调试该项目：

1. 选择刚才创建的配置。
2. 选择 ``Debug`` 模式。
3. 点击 ``Debug`` 图标 |debug_icon| 以开始调试。


.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/1fb0fb9b-a02a-4ed1-bdba-b4b4d36d100f
   :alt: 调试过程

要了解更多有关调试配置的内容，请参阅 :ref:`ESP-IDF OpenOCD 调试 <OpenOCDDebugging>`。
