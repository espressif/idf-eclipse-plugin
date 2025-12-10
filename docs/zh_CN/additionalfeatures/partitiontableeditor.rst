分区表编辑器
============

:link_to_translation:`zh_CN:[中文]`

可以使用分区表编辑器命令更快捷地编辑 `分区表 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/api-guides/partition-tables.html>`_。你可以在编辑器中查看受支持的类型和子类型，并检查输入数据的正确性。

步骤
----

1.  进入 ``Project Explorer``，打开任意希望创建自定义分区表的 IDF 工程。
2.  在 ``Project Explorer`` 中，右键单击该项目并选择 ``ESP-IDF: Partition Table Editor``：

    .. image:: https://user-images.githubusercontent.com/24419842/216105408-ca2e73ce-5df3-4bdd-ac61-b7265deb9b44.png
       :alt: 分区表编辑器菜单选项

    为所选项目打开分区表编辑器时，能看到标准可编辑的内容。若存在错误，则会高亮显示。你可以将鼠标悬停在错误之上以查看相关提示：

    .. image:: https://user-images.githubusercontent.com/24419842/216106804-703b2eb4-b141-48de-8559f072219f.png
       :alt: 分区表编辑器中的错误提示

3. 单击 ``Save`` 或 ``Save and Quit`` 以保存更改。


使用自定义分区表
----------------

进入 ``SDK Configuration``，并按下图所示设置 ``Custom partition table CSV``：

.. image:: https://user-images.githubusercontent.com/24419842/216104107-2844068b-8412-468b-931f-b4778af4417c.png
   :alt: 在 sdkconfig 中设置自定义分区表
