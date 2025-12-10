将二进制数据写入 flash
======================

:link_to_translation:`en:[English]`

可以通过 ``ESP-IDF: Write Binary Data to Flash`` 命令将二进制数据写入乐鑫的 flash 芯片。要访问此命令，请在项目资源管理器中右键单击该项目：

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/186c8498-d779-4771-af53-e5bf09e29502
   :alt: 将二进制数据写入 flash 的命令

点击此命令后，将打开 ``Write Binary Data to Flash`` 对话框，为二进制路径和偏移地址提供了可编辑的默认值。可以使用 ``ESP-IDF: Partition Table Editor`` 命令或手动打开 ``partitions.csv`` 文件查看分区表，确认正确的偏移地址。

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/46e24e89-a1ed-4169-8c92-1ba0b0089ea7
   :alt: 将二进制数据写入 flash 的对话框

点击 ``Flash`` 按钮将执行烧录命令，并在此对话框中显示结果。
