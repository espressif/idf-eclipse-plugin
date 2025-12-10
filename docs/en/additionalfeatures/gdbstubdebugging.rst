.. _gdbstubdebugging:

GDBStub Debugging
=================

:link_to_translation:`zh_CN:[中文]`

You can now use the GDBStub debugging in the Eclipse plugin to diagnose and debug chip issues when the chip enters panic mode.

To enable GDBStub debugging for a project:

1.  Launch the ``sdkconfig`` file in the project root by double-clicking it. This will open the configuration editor.

    .. image:: ../../../media/GDBStubDebugging/sdkconfig_editor.png

2.  Expand the ``Component Config`` section and select ``ESP System Settings``. From the settings on the right for ``Panic handler behaviour`` select the ``GDBStub on panic`` option from the list.

    .. image:: ../../../media/GDBStubDebugging/sdkconfig_editor_panic_behavior.png

When you connect the serial monitor and a panic occurs in this example, the GDBStub debugger will be launched automatically.

To use the GDBStub debugging for a project:

1.  Create a template ``hello_world`` project and add the following lines in the ``main.c`` file:

    .. code-block:: c

        // This is a global variable
        COREDUMP_DRAM_ATTR uint8_t global_var;

2.  Now add these two lines just above the ``esp_restart()`` function:

    .. code-block:: c

        global_var = 25;
        assert(0);

The final file should look like the following:

.. image:: ../../../media/GDBStubDebugging/code_example.png

Build and flash the project, then launch the serial monitor. At line 45, a failing assert is triggered, which puts the chip into panic mode. When execution reaches this line, you will be prompted to switch to the Debug perspective, and the chip will be halted.

The chip is in panic mode, and execution cannot continue from this point. You must stop and restart the chip using IDF commands or by restarting the serial monitor.

.. image:: ../../../media/GDBStubDebugging/debug_panic_mode.png

You can view the registers, stack trace, and even the value of variables in the stack frame.

To exit the debug session, simply press the ``stop`` button.
