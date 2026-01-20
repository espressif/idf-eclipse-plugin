.. _dfuflashing:

Device Firmware Upgrade (DFU) through USB
==========================================

:link_to_translation:`zh_CN:[中文]`

Device Firmware Upgrade (DFU) is a mechanism for upgrading the firmware of devices through Universal Serial Bus (USB). There are a few requirements that need to be met:

-  DFU is supported by ESP32-S2 and ESP32-S3 chips.
-  Make necessary electrical connections manually. You can follow this `guide <https://blog.espressif.com/dfu-using-the-native-usb-on-esp32-s2-for-flashing-the-firmware-b2c4af3335f1>`__ for the ESP32-S2 board, and this `guide <https://docs.espressif.com/projects/esp-idf/en/latest/esp32s3/api-guides/dfu.html>`__ for the ESP32-S3 board. The necessary connections for the USB peripheral are shown in the following table.

   .. list-table::
      :header-rows: 1

      * - GPIO
        - USB
      * - 20
        - D+ (green)
      * - 19
        - D- (white)
      * - GND
        - GND (black)
      * - +5 V
        - +5 V (red)

After meeting the above requirements:

1. The chip needs to be in bootloader mode for detection as a DFU device and flashing. This can be achieved by pulling GPIO0 down (e.g., pressing the BOOT button), pulsing RESET down for a moment, and releasing GPIO0.
2. Install USB drivers (Windows only). The drivers can be installed by the `Zadig tool <https://zadig.akeo.ie/>`_.

   - Ensure that the device is in download mode before running the tool, and that the tool detects the device before installing the drivers.
   - The Zadig tool might detect several USB interfaces of the target. Install the WinUSB driver only for the interface without a driver installed (likely Interface 2), and avoid re-installing drivers for other interfaces.
   - Manual driver installation via Device Manager in Windows is not recommended, as it might cause flashing issues.

After meeting the above requirements, you can proceed to build and flash via DFU. To use DFU:

1. Edit the active launch configuration.
2. In the main tab, select the ``Flash over DFU`` option.
3. Select a suitable IDF target for DFU.
4. When using the build command, an extra file (``dfu.bin``) will be created, which can be used later for flashing.

.. image:: https://user-images.githubusercontent.com/24419842/226182180-286099d3-9c1c-4394-abb0-212d43054529.png
   :alt: DFU actions

Additional information, including common errors and known issues, is available `here <https://docs.espressif.com/projects/esp-idf/en/latest/esp32s3/api-guides/dfu.html#usb-drivers-windows-only>`_.
