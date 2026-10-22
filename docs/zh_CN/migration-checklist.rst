迁移完成检查清单
================

:link_to_translation:`en:[English]`

在认为迁移完成之前，请确认：

- 项目能以预期的 ESP-IDF 版本和目标打开并构建。
- Menuconfig 可以打开，并且现有配置值仍然保留。
- 开发板/OpenOCD、烧录方式和串口与实际使用的流程一致。
- 如果工作流包含烧录和监视，这两项可以正常工作。
- 如果需要自定义构建目录，该目录已经恢复。
- 调试可以成功启动，并且所需的自定义 Eclipse 调试设置已经核对。

相关资源
--------

- :doc:`migrate-to-vscode`
- :doc:`support-policy`
- `ESP-IDF VS Code 扩展 <https://docs.espressif.com/projects/vscode-esp-idf-extension/zh_CN/latest/>`_
- `ESP-IDF 编程指南 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/>`_
