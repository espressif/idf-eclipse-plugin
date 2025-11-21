Wokwi 模拟器
===============

:link_to_translation:`en:[English]`

要在 IDE 中使用 Wokwi 模拟器，请按以下步骤操作：

1. 安装 ``wokwi-server``，具体步骤见 `wokwi-server 安装指南 <https://github.com/MabezDev/wokwi-server/>`_。
2. 前往 ``Preferences`` > ``C/C++`` > ``Build`` > ``Environment``。在 Eclipse CDT 构建环境变量中，将 ``WOKWI_SERVER_PATH`` 配置为 ``wokwi-server`` 可执行文件的路径。
3. 使用 ``Wokwi Simulator`` 创建新的 ``Run launch configuration``。
4. 选择一个项目，添加 Wokwi 项目的 ID。Wokwi 项目的 ID 可以在其 URL 中找到。例如，Wokwi 项目 `ESP32 Rust Blinky <https://wokwi.com/projects/345932416223806035>`_ 的项目 ID 为 ``345932416223806035``。
5. 点击 ``Finish`` 以保存配置更改。
6. 在 IDE 工具栏中，点击 ``Launch`` 按钮以启动 Wokwi 模拟器。
7. Wokwi 模拟器将会在外部浏览器中打开，串口监视器输出将显示在 Eclipse CDT 构建控制台中。
8. 如需结束 Wokwi 模拟器，请在工具栏中点击 ``Stop`` 按钮。
