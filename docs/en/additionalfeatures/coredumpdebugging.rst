.. _coredumpdebugging:

Core Dump Debugging
====================

The IDF-Eclipse plugin allows you to debug the core dump if any crash occurs on the chip and the configurations are set. Currently, only the UART core dump capture and debugging is supported.

To enable core dump debugging for a project:

1. You need to enable it first in `sdkconfig`. Launch the `sdkconfig` in the project root by double-clicking on it which will open the configuration editor.

2. Click on the `Core Dump` from the settings on the left and select `Data Destination` as `UART`.

.. image:: ../../../media/CoreDumpDebugging/sdkconfig_editor.png

This will enable the core dump debugging, and whenever you connect a serial monitor for that project, if any crash occurs, it will load the dump and open a debug perspective in Eclipse to let you diagnose the dump where you can view all the information in the core dump.

You can view the registers stack trace and even view the value of variables in the stack frame.

To exit the debug session: simply press the `stop` button.
