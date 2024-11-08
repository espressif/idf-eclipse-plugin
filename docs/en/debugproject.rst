Debug Your Project
===============================
In most cases, only two things are required to start debugging an ESP-IDF project:

1. Create a debug configuration
2. Check whether the board in the created configuration corresponds to the board in use.

.. note::  
    If you're using Windows, you may need to install drivers using Zadig to run a debug session successfully. For detailed instructions, please refer to this `guide <https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/jtag-debugging/configure-ft2232h-jtag.html#configure-usb-drivers>`_.

The fastest way to create a debug configuration is to expand the configuration list in the launch bar and click `New Launch Configuration...`. Then select `ESP-IDF GDB OpenOCD Debugging` -> Double Click or `Next`. After that, the preview for the new debug configuration will open where it's necessary to check the board.

After creating the debug configuration, you can go ahead and debug the project. Select the configuration you just created, select debug mode, and click on the "Debug" icon ![](docs_readme/images/icons/debug.png) to start debugging:

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/1fb0fb9b-a02a-4ed1-bdba-b4b4d36d100f
   :alt: Debugging process

To learn more about the debug configuration, please refer to :ref:`ESP-IDF OpenOCD Debugging <OpenOCDDebugging>`.
