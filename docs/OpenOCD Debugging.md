## ESP-IDF GDB OpenOCD Debugging
In ESP-IDF GDB OpenOCD Debugging, both GDB server and GDB client both will be initiated by Eclipse, so no need to run anything from the command line.

Before you get started, please make sure you've already installed `Embedded C/C++ OpenOCD Debugging` plugin while updating the IDF Eclipse Plugin.
Also make sure your [JTAG interface is configured and connected](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/jtag-debugging/index.html#configure-and-connect-jtag-interface).
![](images/OpenOCDDebug_1.png)

# Create a new debug configuration
Please follow the below steps to create a new debug configuration.
* Right-click on the project
* `Debug As > Debug Configurations...` This will launch a debug configuration window
* On the left Panel, choose `ESP-IDF GDB OpenOCD Debugging`
* Right Click and create `New Configuration` This will create a new debug configuration for your project

Please navigate through each tab and configure project specific settings. 
> **NOTE:**  Most of the settings are auto-configured by the plugin.

![](images/OpenOCDDebug_4.png)


The other way to create a debug configuration is from the launch configuration bar:
* Expand list with launch/debug configurations 
* Click on `New Launch Configuration...`
* Select `ESP-IDF GDB OpenOCD Debugging` and double click on it or on `Next >` button
* In the `Debugger` tab, check if the `Config options` is right for your board.
* Click on `Finish` 

![](images/OpenOCDDebug_9.png)
## Main Tab 
![](images/OpenOCDDebug_5.png)

## Debugger Tab
In the `Debugger` tab, all parameters are automatically configured to start debugging, you just need to check if the `Config options` line is appropriate for your board. It automatically adjusts based on `Flash voltage` and `Board` options. If you expand the list of boards, only those that match the selected `Target` will appear. So, for example, if the selected target is `esp32`, you will not see `ESP32-S2-KALUGA-1` in the list, to see it there, you need to change the target to `esp32s2` first. The second option in the debugger tab, which depends on the target, is the `gdb executable`, which is also automatically and dynamically configured based on the target you choose.


![](images/OpenOCDDebug_6.png)

> **NOTE:**  Update the OpenOCD Config options based on the esp board you've choosen. Please check this here https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/jtag-debugging/tips-and-quirks.html#id1

## Startup Tab
By default, the binaries will be uploaded to your board before joining a debug session, so there is no need to flash them to the target separately. If for some reason you don't want to do that, you can uncheck the `Flash every time with application binaries` option.

![](images/OpenOCDDebug_7.png)


# Preferences for OpenOCD Configuration
OpenOCD path is auto-configured based on the `OPENOCD_SCRIPTS` path defined in the CDT Build environment variables.

![](images/OpenOCDDebug_2.png)

Any issues on OpenOCD debugging please refer to https://github.com/espressif/openocd-esp32/wiki/Troubleshooting-FAQ 
