迁移故障排查
============

:link_to_translation:`en:[English]`

如果 :doc:`migration-walkthrough` 中的某一步与 Espressif-IDE 中的行为不一致，请使用下表。IDE 构建错误提示仍见 :doc:`troubleshooting`。

.. list-table::
   :header-rows: 1
   :widths: 32 34 34

   * - 现象
     - 可能原因
     - 建议操作
   * - SDK 配置编辑器要求执行 ``fullclean``
     - 现有 CMake 缓存指向另一个 Python/环境
     - 运行 **ESP-IDF: Full Clean**，然后重新打开编辑器。
   * - JTAG 烧录报告 FTDI/OpenOCD 设备错误
     - 开发板/OpenOCD 设置未恢复
     - 重新选择目标以及具体的开发板/设备配置。
   * - 目标看起来正确，但 JTAG 仍然失败
     - 芯片目标与开发板/OpenOCD 状态是分开的
     - 再次明确选择开发板/设备。
   * - 调试提示缺少 ``.vscode/launch.json``
     - 现有项目没有 VS Code 调试配置
     - 运行 **ESP-IDF: Add VS Code Configuration Folder**。
   * - 默认调试可用，但行为与 Eclipse 不同
     - 自定义 Eclipse ``.launch`` 值未被转换
     - 检查并迁移或重建所需的自定义调试设置。
   * - VS Code 创建了 ``project/build``，而不是原来的构建路径
     - Eclipse 自定义构建路径未被导入
     - 手工配置自定义构建目录。见 :doc:`migration-advanced`。
   * - Doctor 报告与当前项目无关的配置错误
     - 用户级或全局扩展设置已过期
     - 确认所报告的设置是否与当前项目相关。
