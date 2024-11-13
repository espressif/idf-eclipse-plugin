Debug Your Project
===============================

.. |debug_icon| image:: ../../media/icons/debug.png
   :height: 16px
   :align: middle

In most cases, only two things are required to start debugging an ESP-IDF project:

1. Create a debug configuration
2. Check whether the board in the created configuration corresponds to the board in use.

.. note::  
    If you're using Windows, you may need to install drivers using Zadig to run a debug session successfully. For detailed instructions, please refer to this `guide <https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/jtag-debugging/configure-ft2232h-jtag.html#configure-usb-drivers>`_.

The fastest way to create a debug configuration is as follows:

1. Expand the configuration list in the launch bar and click on `New Launch Configuration...`.
2. Select `ESP-IDF GDB OpenOCD Debugging`, then double-click or click `Next`.
3. A preview of the new debug configuration will open, where you should check the board settings.

After creating the debug configuration, you can proceed with debugging the project:

1. Select the configuration you just created.
2. Choose debug mode.
3. Click on the "Debug" icon |debug_icon| to start debugging.


.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/1fb0fb9b-a02a-4ed1-bdba-b4b4d36d100f
   :alt: Debugging process

To learn more about the debug configuration, please refer to :ref:`ESP-IDF OpenOCD Debugging <OpenOCDDebugging>`.
