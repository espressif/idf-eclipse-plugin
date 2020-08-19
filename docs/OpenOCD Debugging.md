## ESP-IDF GDB OpenOCD Debugging
In ESP-IDF GDB OpenOCD Debugging, both GDB server and GDB client both will be initiated by Eclipse, so no need to run anything from the command line.

Before you get started, please make sure you've already installed `Embedded C/C++ OpenOCD Debugging` plugin while updating the IDF Eclipse Plugin.

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

![](images/OpenOCDDebug_5.png)

![](images/OpenOCDDebug_6.png)

> **NOTE:**  Update the OpenOCD Config options based on the esp board you've choosen. Please check this here https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/jtag-debugging/tips-and-quirks.html#id1

![](images/OpenOCDDebug_7.png)


# Preferences for OpenOCD Configuration
OpenOCD path is auto-configured based on the `OPENOCD_SCRIPTS` path defined in the CDT Build environment variables.

![](images/OpenOCDDebug_2.png)