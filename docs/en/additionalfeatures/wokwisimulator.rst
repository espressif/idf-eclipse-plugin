Wokwi Simulator
===============

:link_to_translation:`zh_CN:[中文]`

To use the Wokwi Simulator within the IDE, follow these steps:

1. Install ``wokwi-server`` as explained in the `wokwi-server installation guide <https://github.com/MabezDev/wokwi-server/>`_.
2. Go to ``Preferences`` > ``C/C++`` > ``Build`` > ``Environment``. In the Eclipse CDT build environment variables, configure ``WOKWI_SERVER_PATH`` with the path to the ``wokwi-server`` executable.
3. Create a new ``Run launch configuration`` with the ``Wokwi Simulator``.
4. Select a project and add the Wokwi project ID. The ID of a Wokwi project can be found in its URL. For example, the project ID of the Wokwi project `ESP32 Rust Blinky <https://wokwi.com/projects/345932416223806035>`_ is ``345932416223806035``.
5. Click ``Finish`` to save the configuration changes.
6. From the IDE Toolbar, click the ``Launch`` button to start the Wokwi simulator.
7. The Wokwi Simulator will open in an external browser, with serial monitor output displayed in the Eclipse CDT build console.
8. To terminate the Wokwi Simulator, click the ``Stop`` button in the toolbar.
