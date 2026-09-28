Debug Your Project
==================

:link_to_translation:`zh_CN:[中文]`

.. |debug_icon| image:: ../../media/icons/debug.png
   :height: 16px
   :align: middle

Each ESP-IDF project has a single ``ESP-IDF Application`` launch configuration that is used for both running (building and flashing) and debugging. You do not need to create a separate debug configuration. The **Launch Mode** selected in the launch bar (the first dropdown) determines what the configuration does and which settings are shown when you edit it:

- ``Run`` mode: build, flash, and serial settings (``Build Settings``, ``Main``, ``Environment``, and ``Common`` tabs).
- ``Debug`` mode: OpenOCD and GDB settings (``Main``, ``Debugger``, ``Startup``, ``Source``, ``Common``, and ``SVD`` tabs).

.. note::

    If you're using Windows, you may need to install drivers using Zadig to run a debug session successfully. For detailed instructions, please refer to this `guide <https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/jtag-debugging/configure-ft2232h-jtag.html#configure-usb-drivers>`_.

In most cases, you only need to check that the board specified in the configuration matches the board you are using:

1. Select your project's configuration from the second dropdown in the launch bar.
2. Select ``Debug`` from the **Launch Mode** dropdown.
3. Click on the ``Edit`` (gear) icon next to the configuration.
4. In the ``Debugger`` tab, check that the ``Board`` and ``Config options`` match your board, then click ``OK``.
5. Click on the ``Debug`` icon |debug_icon| to start debugging.

.. image:: ../../media/unified_launch_config/switch_mode_and_edit.gif
   :alt: Switching the launch mode and editing the configuration

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/1fb0fb9b-a02a-4ed1-bdba-b4b4d36d100f
   :alt: Debugging process

.. important::

    Debug settings can only be edited while the **Launch Mode** is set to ``Debug``, and run settings (such as flash arguments or the build folder) can only be edited while it is set to ``Run``. If you do not see the tab you are looking for, switch the mode and open the configuration editor again.

To learn more about the debug settings, please refer to :ref:`ESP-IDF OpenOCD Debugging <OpenOCDDebugging>`.
