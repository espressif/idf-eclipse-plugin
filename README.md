# idf-eclipse-plugin

Eclipse Plugins for Espressif IoT Development Framework(IDF)

# Table Of Contents
[ Prerequisites ](#Prerequisites) <br>
[ Getting Started ](#GettingStarted) <br>
[ Installation of Tools ](#InstallTools) <br>
[ Configuring Environment Variables ](#configureEnvironmentVariables)<br>
[ Create a new Project using default template ](#NewProjectUsingDefault)<br>
[ Create a new project using idf examples/templates ](#NewProjectUsingTemplates)<br>
[ Import an existing IDF Project ](#ImportProject)<br>
[ Configuring Core Build Toolchains ](#ConfigureToolchains)<br>
[ Configuring CMake Toolchain ](#ConfigureCMakeToolchain)<br>
[ Configuring Launch Target ](#ConfigureLaunchTarget)<br>
[ Compiling the Project ](#BuildApplication)<br>
[ Viewing Serial Output ](#ConfigureLaunchTerminal)<br>
[ Flashing the Project ](#FlashApplication)<br>
[ Configuring the Project ](#projectconfigure)<br>
[ Debugging the Project ](#debugging)<br>
[ How to raise bugs ](#howToRaiseBugs)<br>
[ FAQ ](#faq)<br>


<a name="Prerequisites"></a>
# Prerequisites
* **Java 8 and Higher** : Download and install Java SE from <a href= "https://www.oracle.com/technetwork/java/javase/downloads/index.html">here</a>
* **Eclipse 2018-12 CDT Package** : Download and install Eclipse CDT package from <a href= "https://www.eclipse.org/downloads/packages/release/2018-12/r/eclipse-ide-cc-developers">here </a>
*  **ESP-IDF** : Follow the download instructions from <a href ="https://docs.espressif.com/projects/esp-idf/en/latest/get-started/index.html#step-2-get-esp-idf">here</a>

<a name="GettingStarted"></a>
# Getting started with the Eclipse IDF Plugins

# Install Plugins using Zip
* Download the Espressif IDF eclipse `artifacts.zip` file from https://gitlab.espressif.cn:6688/idf/idf-eclipse-plugin/-/jobs/artifacts/master/download?job=build
* Extract the downloaded zip file
* Launch Eclipse
* Go to `Help` -> `Install New Software`
* Click `Add…` button
* Select `Archive` from Add repository dialog and select the file ``com.espressif.idf.update-1.0.0-SNAPSHOT.zip`` from the extracted folder
* Click `Add`
* Select `Espressif IDF` from the list and proceed with the installation 
* Restart the Eclipse

![](docs/images/1_idffeature_install.png)

# Install Plugins using Update site url

* You can install the IDF Eclipse plugins into an existing Eclipse CDT installation using the update site url. You first need to add the release repository url as follows:
* Go to `Help` -> `Install New Software`
* Click `Add…`
* Enter `Location` of the repository https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/
* Enter `Name` as `Espressif IDF Plugins for Eclipse`
* Click `Ok`
* Select `Espressif IDF` from the list and proceed with the installation 


<a name="InstallTools"></a>
# Installation of IDF Tools:
* Navigate to `Help` > `Espressif IDF Tools Manager` > `Install Tools`
* Provide the ESP-IDF directory path to get started with the installation. Check the Console for the installation details. Installation might take a while if you're doing it for the first time since it has to download and install xtensa-esp32-elf, esp32ulp-elf, cmake, openocd-esp32 and ninja tools.

**Note:** Make sure you run this step even if you've already installed the required tools, since it sets the IDF_PATH and PATH based on the install_tools.py export command.

![](docs/images/install_tools.png)

<a name="configureEnvironmentVariables"></a>
# Configuring Environment Variables:
* Click on the `Environment` preference page under `C/C++ Build`. 
* Click “Add…” and enter name `BATCH_BUILD` and value `1`.
* Click “Add…” again, and enter name `IDF_PATH`. The value should be the full path where ESP-IDF is installed. Windows users can copy the IDF_PATH from windows explorer.
* Edit the `PATH` environment variable. Keep the current value, and append the path to the Xtensa toolchain installed as part of IDF setup, if this is not already listed on the PATH. A typical path to the toolchain looks like `/home/user-name/esp/xtensa-esp32-elf/bin`. Note that you need to add a colon : before the appended path. Windows users will need to prepend `C:\msys32\mingw32\bin;C:\msys32\opt\xtensa-esp32-elf\bin;C:\msys32\usr\bin` to PATH environment variable (If you installed msys32 to a different directory then you’ll need to change these paths to match).

![](docs/images/2_environment_pref.png)

<a name="NewProjectUsingDefault"></a>
# CMake IDF Project

## To create a new Project using default esp-idf-template:
* Make sure you are in C/C++ perspective.
* Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`)
* Provide the project Name
* Click `Finish`

![](docs/images/3_new_project_default.png)

<a name="NewProjectUsingTemplates"></a>
## To create a new project using idf examples/templates:
* Make sure you're in C/C++ perspective.
* Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`)
* Provide the Project Name
* Click `Next`
* Check `Create a project using one of the templates`
* Select the required template from the tree
* Click `Finish`

![](docs/images/4_new_project_templates.png)

<a name="ImportProject"></a>
##  Import an existing IDF Project
* Make sure you're in `C/C++ Perspective`.
* Right click on the Project Explorer
* Select `Import..` Menu
* Select `Existing IDF Project` from `Espressif` import wizard menu list
* Click `Next`
* Click on `Browse...` to choose an existing project location directory
* Provide project name if you wish you have a different name
* Click `Finish` to import the selected project into eclipse workspace as a CMake project

![](docs/images/5_import_project.png)

<a name="BuildIDFProject"></a>
# Building the IDF projects

We need to tell Eclipse CDT what is the CMake toolchain and toolchain file which need to be used to build the project. However, this will be auto-detected if you've installed the tools using the "Help > Espressif IDF Tools Manager > Install Tools" option from the eclipse. Make sure to restart the eclipse after installing the tools.

If you see toolchain is not auto-detected you can always configure using the below instructions.

<a name="ConfigureToolchains"></a>
## Configuring Core Build Toolchains

* Open Eclipse Preferences
* Navigate to `C/C++  -> “Core Build Toolchains` preference page
* Click on `Add..` from the User defined Toolchians tables
* Select `GCC` as a toolchain type
* Click on `Next>`
* Provide the GCC Toolchain Settings:

**Compiler:** /Users/kondal/esp/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc,
**Operating System:** esp32,
**CPU Architecture:** xtensa

![](docs/images/6_core_build_toolchains.png)

<a name="ConfigureCMakeToolchain"></a>
## Configuring CMake Toolchain
We now need to tell CDT which toolchain to use when building the project. This will pass the required arguments to CMake when generating the Ninja files.

* Navigate to “C/C++  -> “CMake” preference page
* Click `Add..` and this will launch the New CMake Toolchain configuration dialog
* Browse CMake toolchain `Path`. Example: `/Users/kondal/esp/esp-idf/tools/cmake/toolchain-esp32.cmake`
* Select GCC Xtensa Toolchain compiler from the drop-down list. Example: `esp32 xtensa /Users/kondal/esp/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc`

**NOTE:**  Eclipse CDT has a bug in saving the toolchain preferences, hence it's recommended to restart the Eclipse before we move further configuring the launch target.

![](docs/images/7_cmake_toolchain.png)

<a name="ConfigureLaunchTarget"></a>
## Configuring Launch target
Next, we need to tell CDT to use the toolchain for our project. This is accomplished through the Launch Bar, the new widget set you see on the far left of the toolbar. And this will be shown only when you have a project in the project explorer.

* Click on the third dropdown 
* Select `New Launch Target`
* Select `ESP32 Target`
* Provide properties for the target where you would like to launch the application. Enter a name for the target, “esp32” as the operating system, “xtensa” as the CPU architecture, and select the serial port your ESP32 device is connected to on your machine. The OS and architecture need to match the settings for the toolchain. You can see those settings in the Preferences by selecting C/C++ and Core Build Toolchains.

![](docs/images/8_launch_target.png)

<a name="BuildApplication"></a>
## Compiling the Project
* Select a project from the Project Explorer
* Select `Run` from the first drop-down, which is called `Launch Mode`
* Select your application from the second drop-down, which is called `Launch Configuration`
* Select target from the third drop-down, which is called `Launch Target`
* Now click on the `Build` button widget which you see on the far left of the toolbar

![](docs/images/9_cmake_build.png)

<a name="ConfigureLaunchTerminal"></a>
## Viewing Serial Output
To see what program do we need to configure eclipse terminal to connect the serial port.

* Click on the "Open a Terminal" icon from the toolbar
* Choose `Serial Terminal` from the terminal drop-down
* Select `Serial Port` for your board. Example: /dev/cu.SLAB_USBtoUART
* And, configure the remaining settings and click on Ok to launch the eclipse terminal and which will listen the USB port

**NOTE:** This won't display anything immediately until the application is flashed on to the board. 

![](docs/images/10_serial_terminal.png)

<a name="FlashApplication"></a>
## Flashing the Project
ESP-IDF has a tool called "idf.py" which is a wrapper around make flash command with some handy operations. Follow the below instructions to hook the idf.py to the launch configuration

* Click on the `Launch Configuration` edit button
* Switch to the `Main` tab
* Specify the `Location` where this application has to run on. Since idf.py is a python file, will configure the python system path. Example:`${system_path:python}`
* Specify `Working directory` of the application. Example: `${workspace_loc:/hello_world}`
* In additional arguments, provide a flashing command which will run in the specified working directory
* Flash command looks like this: `/Users/kondal/esp/esp-idf/tools/idf.py -p /dev/cu.SLAB_USBtoUART flash`
* Click OK to save the settings
* Click on the `Launch` icon to flash the application to the selected board 

![](docs/images/11_launch_configuration.png)

![](docs/images/12_flashing.png)

<a name="projectconfigure"></a>
# Configuring the Project
IDF plugin will allow you to configure `sdkconfig` without leaving the Eclipse environment.

## SDK Configuration editor
Project configuration is held in a single file called `sdkconfig` in the root directory of the project. This configuration file is modified using "SDK Configuration Editor" to customise the configuration of the project.

To launch the SDK Configuration editor:
* Navigate to `sdkconfig` file
* Double click on the file to launch the SDK configuration editor
* Use `Ctrl+S` or  `Command+S` based on the OS environment to save the changes. You can also use Eclipse `Save` button from the toolbar
* To revert the sdkconfig editor changes, either you can close the editor without saving them. Or you can right click on the `sdkconfig` file and select `Load sdkconfig` menu option to revert the changes from the editor.

![](docs/images/13_sdkconfig_editor.png)

## CMake Editor
CMake Editor Plug-in is integrated with IDF plugins for editing CMake files such as CMakeLists.txt. It provides syntax coloring, CMake command content assist, and code templates.

![](docs/images/cmake_editor_ca.png)

<a name="debugging"></a>
# Debugging the Project
Please refer to <a href ="https://docs.espressif.com/projects/esp-idf/en/latest/api-guides/jtag-debugging/index.html" > JTAG Debugging guide</a>

<a name="howToRaiseBugs"></a>
## How to raise bugs
Raise issues directly under espressif JIRA here https://jira.espressif.com:8443/projects/IEP/issues/ with the project name `IDF Eclipse Plugin(IEP)`

<a name="faq"></a>
## FAQ

* Which version of Java should I use? 
> Java 8 and higher
* Which version of Eclipse should I use?
> Eclipse 2019-12 CDT and higher
* How do I know the installed version of Java in my system?
> You can check using "java -version" command from the terminal
* Espressif Menu options and Espressif IDF Project menu is not visible in my eclipse
> Make sure you have installed Java 8 and higher and you're in the C/C++ perspective
* How do I access the error log?
> To view the Eclipse error log: From the main menu, select Window > Show View > Other. Then select General > Error Log.
* How do I know the installed Eclipse IDF plugin version?
> You can check using Eclipse menu > About Eclipse > Installation Details > Installed Software > Search for "Espressif"
* How do I uninstall IDF plugins from the eclipse?
> Eclipse menu > About Eclipse > Installation Details > Installed Software > Search for "espressif" > Select the espressif IDF plugins > Uninstall..
* Unable to install idf plugins in eclipse?
> Please check the error log from the main menu, select Window > Show View > Other. Then select General > Error Log. 
* Do IDF plugins support CMake project creation?
> Yes, you can create idf CMake project using File > New > Espressif IDF Project
* Can I import my existing IDF project into eclipse?
> Yes, you can import using Import Menu. Import... > Espressif > Existing IDF Project
* Deleted C/C++ build envrionment variables still appearing?
> Uncheck Eclipse Oomph Preference Recorder. Which can be performed by following. Eclipse Preferences >Oomph > Setup Tasks > Preference Recorder > Uncheck "Record into". Find more info here https://jira.espressif.com:8443/browse/IEP-12