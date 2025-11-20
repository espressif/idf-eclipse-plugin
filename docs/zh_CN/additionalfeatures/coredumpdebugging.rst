.. _coredumpdebugging:

核心转储调试
============

:link_to_translation:`en:[English]`

IDF-Eclipse 插件允许在芯片发生崩溃且已完成相关配置时调试核心转储。目前，仅支持通过 UART 捕获和调试核心转储。

可参照以下步骤为项目启用核心转储调试：

1. 先在 ``sdkconfig`` 中启用该功能。在项目根目录中双击 ``sdkconfig``，打开配置编辑器。
2. 在左侧设置中点击 ``Core Dump``，并将 ``Data Destination`` 设置为 ``UART``。

.. image:: ../../../media/CoreDumpDebugging/sdkconfig_editor.png

上述步骤将启用核心转储调试。为项目连接串口监视器时，如发生崩溃，则将加载转储并在 Eclipse 中打开调试透视图，以便查看核心转储包含的所有信息。

你可以查看寄存器、栈回溯，甚至查看栈帧中的变量值。

若想退出调试会话，只需点击 ``stop`` 按钮。
