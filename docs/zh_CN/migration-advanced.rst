进阶迁移场景
============

:link_to_translation:`en:[English]`

部分 Espressif-IDE 设置不在可移植的项目文件中。如果构建已经成功，但烧录、调试或构建目录仍与 Eclipse 流程不一致，请阅读本页。主要步骤见 :doc:`migration-walkthrough`。

自定义构建目录
--------------

Espressif-IDE 使用的自定义构建目录可能保存在 Eclipse 启动配置中，而不是可移植的项目文件中。

例如：

.. code-block:: xml

   <stringAttribute
       key="com.espressif.idf.launch.serial.core.idfBuildFolderPath"
       value="/path/to/custom-build"/>

VS Code 支持自定义构建目录，但目前不会自动导入该值。

如果项目依赖自定义构建路径：

1. 在 VS Code 中使用 `idf.buildPath <https://github.com/espressif/vscode-esp-idf-extension/blob/master/docs_espressif/en/settings.rst#build>`_ 设置对应的构建目录。

   例如：

   .. code-block:: json

      {
          "idf.buildPath": "${workspaceFolder}/custom-build"
      }

2. 必要时重新配置或清理已生成的状态。
3. 验证 Menuconfig。
4. 验证构建。
5. 如果工作流包含烧录，验证烧录。
6. 如果工作流包含调试，验证调试。

这不仅影响编译。外部脚本和 Clangd 也可能依赖当前构建目录中生成的 ``compile_commands.json`` 等文件。

JTAG / OpenOCD 设置
-------------------

JTAG 开发板和适配器设置属于 IDE 专用设置，可能需要在 VS Code 中重新选择。

如果迁移后 JTAG 烧录无法工作：

1. 确认 ESP-IDF 目标，并重新选择具体的开发板/设备。
2. 确认烧录方式已选择 JTAG。
3. 连接了多个设备时，确认所选适配器正确。

自定义 Eclipse 调试配置
-----------------------

Espressif-IDE 的调试设置不会自动转换为 VS Code 调试设置。

如果工作流使用了下列自定义值，请查看原来的 Eclipse ``.launch`` 配置：

- GDB、TCL 或 Telnet 端口
- ``stopAt=app_main``
- 半主机
- 调试前烧录
- 复位/启动行为
- 自定义 GDB 命令、参数或超时

请从 VS Code 的标准调试配置开始，只迁移或重建项目实际需要的自定义行为。

.. caution::

    Eclipse 工作区元数据可能包含过期或重复的值。不要把每个与目标相关的值都原样复制到 VS Code。出现冲突时，以实际的 ESP-IDF 项目配置为准。

C/C++ 代码导航与 IntelliSense
-----------------------------

Espressif-IDE 使用 clangd 进行 C/C++ 代码导航、诊断和头文件解析。

在 VS Code 中，ESP-IDF 默认使用 Microsoft C/C++ 扩展。

如果希望继续使用 clangd，请安装 clangd 扩展并运行：

**ESP-IDF: Configure project for ESP-Clang**

更多说明见 `ESP-IDF VS Code 扩展文档 <https://docs.espressif.com/projects/vscode-esp-idf-extension/zh_CN/latest/configureproject.html>`_。
