NVS 表格编辑器
==============

:link_to_translation:`en:[English]`

NVS 表格编辑器可根据 CSV 文件中的键值对创建二进制文件。生成的二进制文件与 ESP-IDF `非易失性存储库 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/api-reference/storage/nvs_flash.html>`_ 中定义的 NVS 架构兼容。CSV 格式应如下所示：

.. code-block:: text

    key,type,encoding,value      <-- 列标题，必须是第一行
    namespace_name,namespace,,   <-- 第一条目类型必须为 "namespace"
    key1,data,u8,1
    key2,file,string,/path/to/file

.. note:: 

    此功能基于 ESP-IDF `NVS 分区生成程序 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32/api-reference/storage/nvs_partition_gen.html>`_。

操作步骤
--------

1.  在资源管理器中右键单击某个项目。
2.  点击 ``ESP-IDF: NVS Table Editor`` 菜单选项。

    .. image:: https://user-images.githubusercontent.com/24419842/216114697-9f231211-f5dd-431b-9432-93ecc656cfec.png
       :alt: NVS 表格编辑器菜单选项

3.  按需修改 CSV 数据。
4.  点击 ``Save`` 按钮保存更改。若配置正确，则将在对话框顶部看到一条信息提示：

    .. image:: https://user-images.githubusercontent.com/24419842/216115906-9bb4fe55-293b-4c6b-8d22-0aa3520581ab.png
       :alt: 在 NVS 表格编辑器中的保存确认

5.  生成分区二进制文件。可选择 ``Encrypt`` 以加密该二进制文件。如需使用自定义密钥，可禁用 ``Generate Key`` 选项。文件生成后，对话框顶部会显示生成结果的信息提示。如果提示内容过长无法完全显示，可将鼠标悬停其上以查看全文：

    .. image:: https://user-images.githubusercontent.com/24419842/216117261-9bee798a-3a9e-4be5-9466-fc9d3847834b.png
       :alt: 在 NVS 表格编辑器中的二进制文件生成结果

    .. note:: 

        如果出现错误，将会高亮显示。将鼠标悬停在错误图标上可以查看错误详情，同时在对话框顶部也会显示错误信息。
