.. _dfuflashing:

通过 USB 进行设备固件升级 (DFU)
==========================================

:link_to_translation:`en:[English]`

设备固件升级 (Device Firmware Upgrade, DFU) 是一种通过通用串行总线 (Universal Serial Bus, USB) 为设备升级固件的机制。需要满足以下要求：

-  ESP32-S2 和 ESP32-S3 芯片支持 DFU。
-  手动进行必要的电气连接。对于 ESP32-S2 开发版，请参考 `ESP32-S2 设备固件升级指南 <https://blog.espressif.com/dfu-using-the-native-usb-on-esp32-s2-for-flashing-the-firmware-b2c4af3335f1>`__；对于 ESP32-S3，参考 `ESP32-S3 设备固件升级指南 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32s2/api-guides/dfu.html>`__。USB 外设所需的连接可参见下表。

   .. list-table::
      :header-rows: 1

      * - GPIO
        - USB
      * - 20
        - D+（绿色）
      * - 19
        - D-（白色）
      * - GND
        - GND（黑色）
      * - +5 V
        - +5 V（红色）

满足上述要求后，继续以下步骤：

1. 芯片需要进入引导加载程序模式，才能被检测为 DFU 设备并进行烧录。可以通过将 GPIO0 拉低（例如，按下 BOOT 按钮），将 RESET 短暂拉低，然后释放 GPIO0 来实现。
2. 安装 USB 驱动（仅限 Windows 系统）。可以使用 `Zadig 工具 <https://zadig.akeo.ie/>`_ 安装驱动。

   - 在运行该工具前，确保设备处于下载模式，并且在安装驱动前，工具已检测到该设备。
   - Zadig 工具可能会检测到目标设备的多个 USB 接口。你只需要为还没有驱动程序的接口（通常是 Interface 2）安装 WinUSB 驱动，不要对其他已经安装了驱动的接口重复安装。
   - 不建议通过 Windows 的设备管理器手动安装驱动，因为这可能导致烧录问题。

满足上述要求后，即可通过 DFU 进行构建和烧录。参照下列步骤使用 DFU：

1. 编辑当前启用的启动配置。
2. 在主选项卡中，选择 ``Flash over DFU`` 选项。
3. 选择适用于 DFU 的 IDF 目标。
4. 使用构建命令时会生成一个额外的文件 (``dfu.bin``)，可用于后续烧录。

.. image:: https://user-images.githubusercontent.com/24419842/226182180-286099d3-9c1c-4394-abb0-212d43054529.png
   :alt: DFU 操作

包括常见错误和已知问题在内的更多信息，可参阅 `此处 <https://docs.espressif.com/projects/esp-idf/zh_CN/latest/esp32s3/api-guides/dfu.html#usb-windows>`_。
