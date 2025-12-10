.. _serialMonitor:

监视输出
========

:link_to_translation:`en:[English]`

要在 Eclipse 中查看串口输出，需要将 ESP-IDF 串口监视器连接到串口。该监视器已与 `IDF 监视器 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/api-guides/tools/idf-monitor.html#idf>`_ 集成。

.. image:: ../../media/monitor.png
   :alt: 串口监视器

要在 IDE 中启动串口监视器，请按以下步骤操作：

1. 单击顶部工具栏中的 ``Open a Terminal`` 图标。
2. 在终端下拉菜单中选择 ``ESP-IDF Serial Monitor``。
3. 如果 IDE 未自动检测到你的开发版，则为其选择 ``Serial Port``。
4. 配置串口监视器的 ``Filter Options`` 以过滤输出。
5. 单击 ``OK`` 以启动监视器，监听 USB 端口。

.. image:: ../../media//10_serial_terminal.png


ESP-IDF 串口监视器设置
-----------------------

你可以自定义 ESP-IDF 串口监视器的默认字符数限制和行数。

1. 在 Eclipse 中，前往 ``Preferences`` > ``Espressif``。
2. 单击 ``ESP-IDF Serial Monitor Settings``。
3. 设置 ``Console Line Width`` 和 ``Limit Console Output``。
