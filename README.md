[![GitHub release](https://img.shields.io/github/release/espressif/idf-eclipse-plugin.svg)](https://github.com/espressif/idf-eclipse-plugin/releases/latest)

[中文](./README_CN.md)

# ESP-IDF Eclipse Plugin

ESP-IDF Eclipse Plugin brings developers an easy-to-use Eclipse-based development environment for developing ESP32-based IoT applications.
It provides better tooling capabilities, which simplifies and enhances standard Eclipse CDT for developing and debugging ESP32 IoT applications. It offers advanced editing, compiling, flashing and debugging features with the addition of Installing the tools, SDK configuration and CMake editors.

The plug-in runs on `macOS`, `Windows` and `Linux` platforms.

![](docs/images/macos-logo.png)
![](docs/images/windows-logo.png)
![](docs/images/linux-logo.png)


> **Note:** It supports ESP-IDF CMake based projects (4.x and above).

To get a quick understanding of ESP-IDF and Eclipse plugin features, check our session which was presented in <a href= "https://youtu.be/CbPX3q7LeBc">EclipseCon 2020</a>.

# Table Of Contents
<details open>
  <summary>Get Started</summary>

* [ Installation](#Installation) <br>
* [ Creating a new Project ](#NewProjectUsingDefault)<br>
* [ Configuring Launch Target ](#ConfigureLaunchTarget)<br>
* [ Compiling the Project ](#BuildApplication)<br>
* [ Flashing the Project ](#FlashApplication)<br>
* [ Viewing Serial Output ](#ConfigureLaunchTerminal)<br>
* [ Debugging the Project ](#debugging)<br>
* [ Troubleshooting Guide](#troubleshooting)<br>
* <a href ="https://github.com/espressif/idf-eclipse-plugin/blob/master/FAQ.md#FAQ">FAQ</a>
</details>

<details open>
  <summary>Other IDE Features</summary>

* [ Installing ESP-IDF and Tools via Tools Installation Wizard ](#InstallToolsWizard) <br>
* [ Configuring the Project using sdkconfig Editor](#projectconfigure)<br>
* [ CMake Editor](#cmakeproject)<br>
* [ ESP-IDF Application Size Analysis Editor](#sizeanalysiseditor)<br>
* [ Installing ESP-IDF Components](#espidfcomponents)<br>
* [ ESP-IDF Terminal](#idfterminal)<br>
* [ Configuring Build Environment Variables ](#configureEnvironmentVariables)<br>
* [ Configuring Core Build Toolchain ](#ConfigureToolchains)<br>
* [ Configuring CMake Toolchain ](#ConfigureCMakeToolchain)<br>
* [ Selecting Clang Toolchain](#SelectDifferentToolchain)<br>
* [ Configuring the flash arguments ](#customizeLaunchConfig)<br>
* [ Installing IDF Eclipse Plugin from Eclipse Market Place](#installPluginsFromMarketPlace) <br>
* [ Installing IDF Eclipse Plugin using local archive ](#installPluginsUsingLocalFile) <br>
* [ Upgrading IDF Eclipse Plugin ](#upgradePlugins)<br>
* [ Importing an existing IDF Project ](#ImportProject)<br>
* [ Importing an existing Debug launch configuration ](#importDebugLaunchConfig)<br>
* [ Device Firmware Upgrade (DFU) through USB ](#deviceFirmwareUpgrade)<br>
* [ GDBStub Debugging ](#gdbStubDebugging)<br>
* [ Core Dump Debugging ](#coreDumpDebugging)<br>
* [ Application Level Tracing ](#appLvlTracing)<br>
* [ ESP-IDF master update](#updateEspIdfMaster)<br>
* [ Partition Table Editor UI for ESP-IDF](#partitionTableEditor)<br>
* [ NVS Table Editor](#nvsTableEditor)<br>
* [ Write Binary Data to Flash](#writeFlashBinary)<br>
* [ Changing Language ](#changeLanguage)<br>
* [ Wokwi Simulator](#wokwisimulator)<br>
</details>

<a name="Installation"></a>
# Installing Prerequisites

The minimum requirements for running the IDF Eclipse plug-ins are below.

* **Java 17 and above** : Download and install Java SE from <a href= "https://www.oracle.com/technetwork/java/javase/downloads/index.html">here</a>.
* **Python 3.6 and above** : Download and install Python from <a href="https://www.python.org/downloads/">here</a>.
* **Eclipse IDE for C/C++ Developers 2023-12** : Download and install Eclipse CDT package from <a href= "https://www.eclipse.org/downloads/packages/release/2023-12/r/eclipse-ide-cc-developers">here </a>.
*  **Git** : Get the latest git from <a href ="https://git-scm.com/downloads">here</a>.
*  **ESP-IDF 4.0 and above** : Clone the ESP-IDF repo from <a href ="https://github.com/espressif/esp-idf/releases">here</a>.

> **Note:** Make sure Java, Python and Git are available on the system environment PATH.

## Recommened approach
Instead of installing Eclipse CDT and adding the IEP Update site, we recommend installing the IDE through the mechanism below.
### Windows OS
* Download the [Espressif-IDE with ESP-IDF Offline Windows Installer](https://dl.espressif.com/dl/esp-idf/) and follow the instructions provided [here](https://github.com/espressif/idf-eclipse-plugin/blob/master/docs/Espressif-IDE-Windows-Installer.md). This guide will walk you through the installation process for Java, Git, CMake, ESP-IDF, IDF Tools, Device Drivers, and Espressif-IDE to help you get started.

### macOS and Linux
* To begin, download the [Espressif-IDE](https://github.com/espressif/idf-eclipse-plugin/blob/master/docs/Espressif-IDE.md#downloads) for your respective operating system. Following this, launch the IDE and proceed to install the necessary ESP-IDF and related tools. Please note that prior to launching the IDE, you must ensure that Java, Python, and Git are installed as prerequisites and available in the system path.

<a name="GettingStarted"></a>

# Installing IDF Plugin using update site URL

You can install the IDF Eclipse plugin into an existing Eclipse CDT/Espressif-IDE using the update site URL. First, add the release repository URL as follows:

1. Go to `Help` > `Install New Software`.
1. Click `Add…`, and in the pop-up window:
	* Enter `Name` as `Espressif IDF Plugin for Eclipse`
	* Enter `Location` of the repository:
		* Stable releases(recommended): https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/
		* Beta versions: https://dl.espressif.com/dl/idf-eclipse-plugin/updates/beta/
		* Nightly build: https://dl.espressif.com/dl/idf-eclipse-plugin/updates/nightly/
	* Click `Add`.
1. Select `Espressif IDF` from the list and proceed with the installation.

> **Note:** While the screenshots are captured on macOS, the installation instructions are applicable to Windows, Linux, and macOS.

![](docs/images/idf_update_site_install.png)

<a name="InstallTools"></a>
# Installing ESP-IDF

To install ESP-IDF directly from the Eclipse:

1. Go to `Espressif` > `Download and Configure ESP-IDF`.
1. From the `Download ESP-IDF` section, choose ESP-IDF version and directory to download.
1. Click on `Finish`.

To configure an existing ESP-IDF:

1. Go to `Espressif` > `Download and Configure ESP-IDF`.
1. Check `Use an existing ESP-IDF directory from the file system`.
1. Choose an existing ESP-IDF directory from the file system.
1. Click on `Finish`.

This will download a specified ESP-IDF version and configure `IDF_PATH` in the Eclipse CDT build environment variables.

![](docs/images/espidf_download.png)

# Installing ESP-IDF Tools

ESP-IDF requires the installation of several prerequisite tools to build firmware for the ESP32. These tools include Python, Git, cross-compilers, the menuconfig tool, CMake, and Ninja build tools.

Follow the below instructions to get started.

1. Navigate to `Espressif` > `ESP-IDF Tools Manager` > `Install Tools`.
1. Provide the `ESP-IDF Directory` path.
1. Provide `Git` and `Python` executable locations if they are not auto-detected.
1. Click on `Install Tools` to proceed with the installation process. Check the Console for the installation details.
1. Installation might take a while if you're doing it for the first time since it has to download and install `xtensa-esp32-elf`, `esp32ulp-elf`, Cmake, `openocd-esp32` and Ninja tools.

> **Note:** Ensure that you perform this step even if you have already installed the necessary tools. This step sets the IDF_PATH, PATH, OPENOCD_SCRIPTS, and IDF_PYTHON_ENV_PATH in the Eclipse CDT build environment based on the idf_tools.py export command.

![](docs/images/install_tools.png)

ESP-IDF Directory selection dialog:

![](docs/images/esp_idf_dir.png)

<a name="NewProjectUsingDefault"></a>
# Create a new Project

1. Make sure you are in `C/C++ Perspective`.
1. Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective...`).
1. Provide the `Project name` (The ESP-IDF build system does not support spaces in the project path).
1. Click `Finish`.

To create a project using existing ESP-IDF templates, please refer to [this](#NewProjectUsingTemplates).

> **Note:** You will see a lot of unresolved inclusion errors in the editor and those will be resolved only after the build.

![](docs/images/3_new_project_default.png)

<a name="ConfigureLaunchTarget"></a>
# Configuring build target

Next, we need to tell CDT to use the toolchain for our project so that all the headers will be indexed and resolved. This is accomplished through the Launch Bar, the new widget set you see on the far left of the toolbar. This will be shown only when you have a project in the `Project Explorer`.

1. Click on the third dropdown window from the top bar.
1. Select `New Launch Target`.
1. Select `ESP Target`.
1. Provide properties for the target where you would like to launch the application. Enter a `Name` for the target and select the `Serial Port` your ESP device is connected to on your machine.

![](docs/images/8_launch_target.png)

<a name="BuildApplication"></a>
# Compiling the Project

1. Select a project from the `Project Explorer`.
1. Select `Run` from the first drop-down, which is called `Launch Mode`.
1. Select your application from the second drop-down, which is called `Launch Configuration`(Auto-detected).
1. Select a target from the third drop-down, which is called `Launch Target`.
1. Now click on the `Build` button widget which you see on the far left of the toolbar.

![](docs/images/9_cmake_build.png)

<a name="FlashApplication"></a>
# Flashing the Project

ESP-IDF has a tool called `idf.py` which is a wrapper around `make flash` command with some handy operations. Flash operation can be initiated with just a click of a launch button (second button from the left on the top bar) and it's auto-configured to flash the application with the default flash command i.e, `idf.py -p PORT flash`.

To provide the customized flash arguments, please follow [this](#customizeLaunchConfig) link for further instructions.

To configure flashing via JTAG, please refer to this <a href="https://github.com/espressif/idf-eclipse-plugin/tree/master/docs/JTAG%20Flashing.md"> JTAG Flashing guide</a>.

<a name="ConfigureLaunchTerminal"></a>
# Viewing Serial Output

To see the serial output in Eclipse, we need to configure the `ESP-IDF Serial Monitor` to connect to the serial port. This is integrated with the `IDF Monitor`. Please check more details <a href="https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/tools/idf-monitor.html#idf-monitor">here</a>.

1. Click on the `Open a Terminal` icon from the toolbar.
1. Choose `ESP-IDF Serial Monitor` from the terminal drop-down.
1. Select `Serial Port` for your board if it's not detected.
1. Configure serial monitor filter options for output filtering.
1. Click on `OK` to launch the terminal, which will listen to the USB port.

![](docs/images/10_serial_terminal.png)

### ESP-IDF Serial Monitor Settings

ESP-IDF Serial Monitor will allow you to configure the default settings of the serial monitor character limit and number of lines.

1. Navigate to `Espressif` from the Eclipse Preferences.
1. Click on `ESP-IDF Serial Monitor Settings`.
1. Provide `Console Line Width` and `Limit Console Output`.

<a name="debugging"></a>
# Debugging the Project

## GDB OpenOCD Debugging

Please refer to <a href="https://github.com/espressif/idf-eclipse-plugin/tree/master/docs/OpenOCD%20Debugging.md">GDB OpenOCD Debugging</a>.

# Other IDE Features

<a name="NewProjectUsingTemplates"></a>
## Create a new project using ESP-IDF Templates

1. Make sure you're in `C/C++ Perspective`.
1. Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`).
1. Provide the `Project name`.
1. Click `Next`.
1. Check `Create a project using one of the templates`.
1. Select the required template from the tree.
1. Click `Finish`.

> **Note:** You will see a lot of unresolved inclusion errors in the editor and those will be resolved only after the build.

![](docs/images/4_new_project_templates.png)

<a name="InstallToolsWizard"></a>
# Tools Installation Wizard

You can use the Tools Installation Wizard to manage the tools installation via a wizard. The advantage of this method over the existing installation is that you can easily manage the whole flow via the wizard and install the tools in ESP-IDF framework that you only need.<br/>

To get started:

1. Navigate to `Espressif` > `ESP-IDF Tools Manager` > `Tools Installation Wizard (Preview)`.
![](docs/images/ToolsManager/install_tools_manager.png)

1. The wizard will start and you can select the location for the Git and Python, if they are already present on the system PATH or registry the tools will be populated. After selection, you can click `Next`.
![](docs/images/ToolsManager/git_python_selection.png)

1. Next page will let you select the folder for existing ESP-IDF or you can also select from the drop down list to download the available versions. You can also select master from the list to clone the master for ESP-IDF from GitHub.
![](docs/images/ToolsManager/select_or_download_new_esp_idf.png)

1. After you select `Next` you will see the list of all the available tools in the selected ESP-IDF version, this page lets you select only the recommended tools or you can select the tools you want to. You can also filter out the tools via the filter text box or based on the target. The wizard page is the last page and will Install and Download if necessary all the selected tools required. After you have installed all the tools you can finish the wizard and start creating projects.
![](docs/images/ToolsManager/manage_tools_installation.png)

<a name="projectconfigure"></a>
# SDK Configuration editor

Project configuration is held in a single file called `sdkconfig` in the root directory of the project. This configuration file can be modified using `SDK Configuration Editor`

To launch the SDK Configuration editor:

1. Navigate to `sdkconfig` file.
1. Double-click on the file to launch the SDK configuration editor.
1. Use `Ctrl+S` or  `Command+S` based on the OS environment to save the changes. You can also use Eclipse `Save` button from the toolbar.
1. To revert the sdkconfig editor changes, you can either close the editor without saving them or you can right-click on the `sdkconfig` file and select `Load sdkconfig` menu option to revert the changes from the editor.

![](docs/images/13_sdkconfig_editor.png)

<a name="cmakeproject"></a>
# CMake Editor

CMake Editor Plug-in is integrated with IDF Plugin for editing CMake files such as CMakeLists.txt. It provides syntax coloring, CMake command content assist, and code templates.

![](docs/images/cmake_editor_ca.png)

CMake editor preferences can be controlled using `Eclipse` > `Preferences` > `CMakeEd`.

![](docs/images/cmake_editor_preferences.png)

<a name="sizeanalysiseditor"></a>
# ESP-IDF Application Size Analysis

The Application Size Analysis editor provides a way to analyze the static memory footprint of your application. It has two sections:
- The **Overview** section provides a summary of the application's memory usage;
- The **Details** section will have in-depth details about components and per-symbol level memory information.

**Details** table viewer also provides you with searching and sorting capabilities on various columns.

To launch the Application Size Analysis editor:

1. Right-click on the project.
1. Select `ESP-IDF: Application Size Analysis` menu option to launch the editor.

**Application Size Analysis - Overview**

![](docs/images/sizeanalysis_overview.png)

**Application Size Analysis - Details**

![](docs/images/sizeanalysis_details.png)

<a name="idfterminal"></a>
# ESP-IDF Terminal

This would launch a local terminal with all the environment variables which are set under `Preferences` > `C/C++` > `Build` > `Environment`. The default working directory would be either the currently selected project or IDF_PATH if there is no project selected.

The terminal PATH is also configured with `esptool`, `espcoredump`, `partition_table`, and `app_update` component paths so that it will be handy to access them directly from the ESP-IDF terminal.

To launch the ESP-IDF Terminal:

* Click on the `Open a Terminal` icon from the toolbar.
* Choose `ESP-IDF Terminal` from the terminal drop-down and click `OK` to launch a terminal.

![](docs/images/idf_terminal.png)

<a name="espidfcomponents"></a>
# Installing ESP-IDF Components

You can install the ESP-IDF Components directly into your project from the available components online. Follow the steps below:

* Right-click on the project from `Project Explorer` in which you want to add the component to and Select `Install ESP-IDF Components`.

  ![](docs/images/ESP-IDF_Components/install_components.png)

  A new window will open up showing all the available component to be installed.

* From the window, you can click on `Install` button to add that component to the project. To get to the readme file for that component you can click on `More Info` which will open the browser link to the readme file of that component.
  ![](docs/images/ESP-IDF_Components/components_window.png)

Already added components are also shown but the `Install` button changes text to `Already Added` and is disabled.

<a name="configureEnvironmentVariables"></a>
# Configuring Environment Variables

Eclipse configures automatically the required environment variables in the `Preferences` > `C/C++ Build` > `Environment` section if IDF Tools are installed using `Espressif` > `ESP-IDF Tools Manager` > `Install Tools` menu option. Required environment variables:

* `IDF_PATH`
* `PATH`
* `OPENOCD_SCRIPTS`
* `IDF_PYTHON_ENV_PATH`

If the required environment variables are not configured for any reason, please follow the step-by-step instructions below.

1. Click on the `Environment` preference page under `C/C++ Build`.
1. Click `Add…` again, and enter name `IDF_PATH`. The value should be the full path where ESP-IDF is installed.
1. Similarly, we should configure `OPENOCD_SCRIPTS`, `IDF_PYTHON_ENV_PATH` and `PATH` environment variables.

This is how they should look:

##### IDF_PATH #####
`/Users/user-name/esp/esp-idf`

##### OPENOCD_SCRIPTS #####
`/Users/user-name/.espressif/tools/openocd-esp32/v0.10.0-esp32-20190313/openocd-esp32/share/openocd/scripts`

##### IDF_PYTHON_ENV_PATH #####
`/Users/user-name/.espressif/python_env/idf4.0_py3.7_env`

##### PATH #####
`/Users/user-name/.espressif/tools/xtensa-esp32-elf/esp32-2019r1-8.2.0/xtensa-esp32-elf/bin:/Users/user-name/.espressif/tools/esp32ulp-elf/2.28.51.20170517/esp32ulp-elf-binutils/bin:/Users/user-name/.espressif/tools/cmake/3.13.4/CMake.app/Contents/bin:/Users/user-name/.espressif/tools/openocd-esp32/v0.10.0-esp32-20190313/openocd-esp32/bin:/Users/user-name/.espressif/tools/ninja/1.9.0/:/Users/user-name/.espressif/python_env/idf4.0_py3.7_env/bin:/Users/user-name/esp/esp-idf/tools:$PATH`

![](docs/images/2_environment_pref.png)

# Configuring Toolchains

We need to tell Eclipse CDT what core build toolchain and CMake toolchain to use to build the project. However, this will be auto-detected if you've installed the tools using the `Espressif` > `ESP-IDF Tools Manager` > `Install Tools` option from Eclipse.

If these toolchains are not detected for any reason, please follow the step-by-step instructions below to add a new toolchain.

<a name="ConfigureToolchains"></a>
# Configuring Core Build Toolchains

1. Open Eclipse Preferences.
1. Navigate to `C/C++` > `Core Build Toolchains` preference page.
1. Click on `Add..` from the user-defined toolchains tables.
1. Select `GCC` as a toolchain type.
1. Click on `Next`.
1. Provide the GCC toolchain settings:

	* **Compiler:** /Users/user-name/esp/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc,
	* **Operating System:** esp32,
	* **CPU Architecture:** xtensa

![](docs/images/6_core_build_toolchains.png)

<a name="ConfigureCMakeToolchain"></a>
# Configuring CMake Toolchain

We now need to tell CDT which toolchain to use when building the project. This will pass the required arguments to CMake when generating the Ninja files.

1. Navigate to `C/C++` > `CMake` preference page.
1. Click `Add...` and this will launch the New CMake toolchain configuration dialog.
1. Browse CMake toolchain `Path`. Example: `/Users/user-name/esp/esp-idf/tools/cmake/toolchain-esp32.cmake`.
1. Select GCC Xtensa toolchain compiler from the drop-down list. Example: `esp32 xtensa /Users/user-name/esp/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc`.

> **Note:**  Eclipse CDT has a bug in saving the toolchain preferences, hence it's recommended to restart Eclipse before we move further configuring the launch target.

![](docs/images/7_cmake_toolchain.png)

<a name="SelectDifferentToolchain"></a>
# Selecting Clang Toolchain

With ESP-IDF Eclipse Plugin v2.7.0 and higher you can build your project with the Clang toolchain

1. After updating/installing the ESP-IDF Eclipse plugin to v2.7.0 or higher, you need to run `Espressif` > `ESP-IDF Tools Manager` > `Install Tools` to update the toolchain list and environment variables, that are necessary for Clang toolchain.
1. After creating a new project, edit the project configuration
![image](https://user-images.githubusercontent.com/24419842/194882285-9faadb5d-0fe2-4012-bb6e-bc23dedbdbd2.png)
1. Go to `Build Settings` tab and select clang toolchain there:
![image](https://user-images.githubusercontent.com/24419842/194882462-3c0fd660-b223-4caf-964d-58224d91b518.png)

> **Note:** Clang toolchain now is an experimental feature and you may face some build issues due to the incompatibility of esp-idf. Below is a description of how to fix the most common build issue on the current ESP-IDF master (ESP-IDF v5.1-dev-992-gaf28c1fa21-dirty). To work around clang build errors please refer to [this](https://github.com/espressif/idf-eclipse-plugin/blob/master/WORKAROUNDS.md#clang-toolchain-buid-errors).

<a name="customizeLaunchConfig"></a>
# Launch Configuration

To provide the customized launch configuration and flash arguments, please follow the step-by-step instructions below.

1. Click on the `Launch Configuration` edit button.
1. Switch to the `Main` tab.
1. Specify the `Location` where this application has to run. Since `idf.py` is a python file, will configure the python system path. Example:`${system_path:python}`.
1. Specify `Working directory` of the application. Example: `${workspace_loc:/hello_world}`.
1. In additional arguments, provide a flashing command which will run in the specified working directory.
1. Flash command looks like this: `/Users/user-name/esp/esp-idf/tools/idf.py -p /dev/cu.SLAB_USBtoUART flash`.
1. Click OK to save the settings.
1. Click on the `Launch` icon to flash the application to the selected board.

![](docs/images/11_launch_configuration.png)

![](docs/images/12_flashing.png)

<a name="changeLanguage"></a>
# Changing Language

To change the plugin language a menu is provided to show the list of available languages for the plugin.

1. Click on the `Espressif` menu from the menu bar.
1. Select the `Change Language` from the drop-down menu.
1. From the sub menu select the language you want.
1. Eclipse will restart with selected language.

![](docs/images/change_language.png)

Remember this will only change the language of the Eclipse if the required language bundles for the selected language are installed or else only the plugin interfaces will be changed.

<a name="troubleshooting"></a>
# Troubleshooting

## Suggestions for Solving Errors from ESP-IDF by Hints Viewer

If you run into a problem during a build, chances are that there is a hint for this error in the ESP-IDF hint database, which is stored in `tools/idf_py_actions/hints.yml` of ESP-IDF. The ESP-IDF Eclipse plugin provides a hint viewer where you can type an error message and find a hint for it.
Prerequisites for it is to have `hints.yml`, which is available from ESP-IDF v5.0 and higher. If you are using lower versions of ESP-IDF, you can still use the hints viewer. To do it, you have to manually download the hints.yml file from [here](https://github.com/espressif/esp-idf/blob/master/tools/idf_py_actions/hints.yml) and put it to your `esp-idf/tools/idf_py_actions/` path. To download a file from GitHub, right-click the `Raw` button and then `Save as...`.

To open the hints viewer go to `Windows` -> `Show View` -> `Other...` -> `Espressif` -> `Hints`. You will see the following view:
![image](https://user-images.githubusercontent.com/24419842/189666994-78cc8b24-b934-426f-9df5-79af28c50c55.png)

Now you can type or copy paste some error from the build log, for example:
`ccache error: Failed to create temporary file for esp-idf/libsodium/CMakeFiles/..../....: No such file or directory`

![image](https://user-images.githubusercontent.com/24419842/189672552-994624f3-c0c5-48e6-aa2c-61e4ed8915e5.png)

Double-clicking on the row will give you a hint message, so you can clearly see it if it doesn't fit on your screen in the table view.

![image](https://user-images.githubusercontent.com/24419842/189673174-8ce40cda-6933-4dc4-a555-5d2ca617256e.png)

## Error Log

The Error Log view captures all the warnings and errors logged by plug-ins. The underlying log file is a .log file stored in the .metadata subdirectory of the workspace.

The Error Log view is available in `Window` > `Show View` > `Error Log`.

To export the current log view content into a file, press the Export Log toolbar button or select `Export Log...` from the context menu. Then, enter a file name.

Always provide an error log when reporting an issue.

![](docs/images/export_log.png)

## Console View Log

The Console View provides all the warnings and errors related to the current running process or build. To access the console view.

From the menu bar, `Window` > `Show View` > `Console`.

![](docs/images/CDT_Build_Console.png)

## CDT Global Build Log

Go to `Preferences > C/C++ > Build > Logging`

## Espressif IDF Tools Console

The Espressif IDF Tools Console is part of the Console view, this will be opened only during the installation of IDF tools from the Eclipse.

If you encounter any issue while installing the IDF tools using `Espressif` > `ESP-IDF Tools Manager` > `Install tools`, please check the Espressif IDF Tools Console to see the errors reported.

If this is not active, it can be switched by clicking on the `Display Selected Console` icon from the console view.

![](docs/images/IDF_tools_console.png)

## Heap Tracing

Please refer to <a href="https://github.com/espressif/idf-eclipse-plugin/tree/master/docs/HeapTracing.md">this</a> doc.

<a name="installPluginsFromMarketPlace"></a>
# Installing IDF Eclipse Plugin from Eclipse Market Place

Please follow the steps below to install IDF Eclipse Plugin from the Eclipse Market Place.

1. In Eclipse, choose `Help` > `Eclipse Market Place...`.
1. Enter `ESP-IDF Eclipse Plugin` in the search box to find the plugin.
1. Click on `Install` to follow the installation instructions.
1. Restart the Eclipse.

![](docs/images/market_place.png)

<a name="installPluginsUsingLocalFile"></a>
# Installing IDF Eclipse Plugin from Local Archive

1. Download the latest update site archive for IDF Eclipse Plugin here - https://github.com/espressif/idf-eclipse-plugin/releases.
1. In Eclipse, choose `Help` > `Install New Software`.
1. Click `Add…` button.
1. Select `Archive` from Add repository dialog and select the file `com.espressif.idf.update-vxxxxxxx.zip`.
1. Click `Add`.
1. Select `Espressif IDF` from the list and proceed with the installation.
1. Restart the Eclipse.

![](docs/images/1_idffeature_install.png)

<a name="upgradePlugins"></a>
# How do I upgrade my existing IDF Eclipse Plugin?

If you are installing IDF Eclipse Plugin into your Eclipse for the first time, you first need to add the new release's repository as follows:

1. `Window` > `Preferences` > `Install/Update` > `Available Software Sites`.
1. Click `Add`.
1. Enter the URL of the new repository https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/.
1. Click `Ok`.

If you've already installed IDF Eclipse Plugin using `update site URL`, you can get the latest changes by following the steps below:

1. `Help` > `Check for Updates`.
1. If updates are found, select `Espressif IDF Plugins for Eclipse` and deselect all other items.
1. Click `Next` to proceed with the installation.

![](docs/images/Update_plugins.png)

<a name="ImportProject"></a>
# Importing an existing IDF Project

1. Make sure you're in `C/C++ Perspective`.
1. Right-click in the `Project Explorer`.
1. Select `Import..` Menu.
1. Select `Existing IDF Project` from `Espressif` import wizard menu list.
1. Click `Next`.
1. Click on `Browse...` to choose an existing project location directory.
1. Provide `Project name` if you wish you have a different name.
1. Click `Finish` to import the selected project into Eclipse workspace as a CMake project.

![](docs/images/5_import_project.png)

<a name="importDebugLaunchConfig"></a>
# Importing an existing Debug launch configuration

To import an existing launch configuration into Eclipse:

1. Select `Import...` from the `File` menu.
1. In the Import dialog box, expand the `Run/Debug` group and select `Launch Configurations`.
1. Click on `Next`.
1. Click on `Browse...` to select the required location in the local file system.
1. Select the folder containing the launch files and then click `OK`.
1. Select the checkboxes for the required folder and launch file.
1. If you are replacing an existing configuration with the same name then select `Overwrite existing launch configurations without warning`.
1. Click on `Finish`.

<a name="gdbStubDebugging"></a>
# GDBStub Debugging

You can now use the GDBStub debugging inside our Eclipse plugin to help you diagnose and debug issues on chips via Eclipse when it is in panic mode.

To enable GDBStub debugging for a project:

1. Launch the `sdkconfig` in project root by double-clicking on it which will open the configuration editor.
![](docs/images/GDBStubDebugging/sdkconfig_editor.png)

1. Expand the `Component Config` section and select `ESP System Settings`. From the settings on the right for `Panic Handler behaviour` select the `GDBStub on Panic option` from the list.
![](docs/images/GDBStubDebugging/sdkconfig_editor_panic_behavior.png)

Now you will be taken to the GDBStub debugger automatically when you connect the serial monitor and there is a panic for this example.

To use the GDBStub debugging for a project:

1. Create a template `hello_world` project and add the following lines in the main c file.

	```
	This is a global variable<br/>
	COREDUMP_DRAM_ATTR uint8_t global_var;
	```

1. Now add these two lines just above the `esp_restart()` function

	```
	global_var = 25;
	assert(0);
	```

The final file should be something like this:
![](docs/images/GDBStubDebugging/code_example.png)

Build and flash the project and launch the serial monitor. On line number 45, we are signaling for a failing assert which will put the chip in panic mode and when that line reaches, you will be prompted to switch the perspective to debug mode and the chip will be halted.

Remember that this is a panic mode and you cannot continue the execution from here, you will have to stop and restart the chip through IDF commands or simply restart the serial monitor.

![](docs/images/GDBStubDebugging/debug_panic_mode.png)

You can view the registers stack trace and even view the value of variables in the stack frame.

To exit the debug session simply press `stop` button.

<a name="coreDumpDebugging"></a>

# Core Dump Debugging

The IDF-Eclipse plugin allows you to debug the core dump if any crash occurs on the chip and the configurations are set. Currently only the UART core dump capture and debugging is supported.

To enable core dump debugging for a project:

1. You need to enable it first in `sdkconfig`. Launch the `sdkconfig` in the project root by double-clicking on it which will open the configuration editor

1. Click on the `Core Dump` from the settings on the left and select `Data Destination` as `UART`.
![](docs/images/CoreDumpDebugging/sdkconfig_editor.png)

This will enable the core dump debugging and whenever you connect a serial monitor for that project if any crash occurs it will load the dump and open a debug perspective in Eclipse to let you diagnose the dump where you can view all the information in the core dump.

You can view the registers stack trace and even view the value of variables in stack frame.

To exit the debug session: simply press stop button.

<a name="deviceFirmwareUpgrade"></a>
# Device Firmware Upgrade (DFU) through USB

Device Firmware Upgrade (DFU) is a mechanism for upgrading the firmware of devices through Universal Serial Bus (USB). There are a few requirements that need to be met:

- DFU is supported by ESP32-S2 and ESP32-S3 chips.
- You will need to do some electrical connection work (Here is a [guide](https://blog.espressif.com/dfu-using-the-native-usb-on-esp32-s2-for-flashing-the-firmware-b2c4af3335f1) for the ESP32-S2 board). The necessary connections for the USB peripheral are shown in the following table.

| GPIO | USB         |
| -----| ------------|
| 20   |  D+ (green) |
| 19   |  D- (white) |
| GND  |  GND (black)|
| +5V  |  +5V (red)  |

After meeting the above requirements:

1. The chip needs to be in bootloader mode for the detection as a DFU device and flashing. This can be achieved by pulling GPIO0 down (e.g. pressing the BOOT button), pulsing RESET down for a moment and releasing GPIO0.
1. Install USB drivers (Windows only). The drivers can be installed by the [Zadig tool](https://zadig.akeo.ie/>).
	- Please make sure that the device is in download mode before running the tool and that it detects the device before installing the drivers.
	- The Zadig tool might detect several USB interfaces of the target. Please install the WinUSB driver for only that interface for which there is no driver installed (probably it is Interface 2) and don't re-install the driver for the other interface.
	- The manual installation of the driver in Device Manager of Windows is not recommended because the flashing might not work properly.

After meeting the above requirements, you are free to build and flash via DFU. How to use DFU:

1. Edit the active launch configuration.
1. In the main tab, select the `Flash over DFU` option.
1. Select a suitable IDF target for DFU
1. Now, if you use the build command, an extra file (dfu.bin) will be created, which can be used later for flashing.

![DFU actions](https://user-images.githubusercontent.com/24419842/226182180-286099d3-9c1c-4394-abb0-212d43054529.png)

Additional information, including common errors and known issues, is mentioned in this [guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32s3/api-guides/dfu.html#usb-drivers-windows-only).

<a name="appLvlTracing"></a>
# Application Level Tracing

ESP-IDF provides a useful feature for program behavior analysis called [Application Level Tracing](https://docs.espressif.com/projects/esp-idf/en/latest/esp32c3/api-guides/app_trace.html?). IDF-Eclipse plugin has UI, that allows the use of start, and stop tracing commands and process received data. To familiarize yourself with this library, you can use the [app_trace_to_host](https://github.com/espressif/esp-idf/tree/release/v5.0/examples/system/app_trace_to_host) project or the [app_trace_basic](https://github.com/espressif/esp-idf/tree/release/v5.1/examples/system/app_trace_basic) project if you are using esp-idf 5.1 and higher. These projects can be created from the plugin itself:
![](docs/images/AppLvlTracing_1.png)

Before you start using application-level tracing, it is important to create a debug configuration for the project where you must select the board you are using in order to successfully start the OpenOCD server.

![](docs/images/AppLvlTracing_3.png)

After debug configuration is created, right-click on the project in the `Project Explorer` and click on `ESP-IDF:Application Level Tracing`:

![](docs/images/AppLvlTracing_2.png)

It can take a while to open the application level tracing dialog because the OpenOCD server starts first, so you don't need to start it externally. At the very top of the application-level trace dialog, there are auto-configured fields that you can change for the trace start command.

Start command:

* Syntax: `start <outfile> [poll_period [trace_size [stop_tmo [wait4halt [skip_size]]]]`
* Argument:
	* `outfile`: Path to file to save data from both CPUs. This argument should have the following format: ``file://path/to/file``.
	* `poll_period`: Data polling period (in ms) for available trace data. If greater than 0 then command runs in non-blocking mode. By default, 1 ms.
	* `trace_size`: Maximum size of data to collect (in bytes). Tracing is stopped after specified amount of data is received. By default -1 (trace size stop trigger is disabled).
	* `stop_tmo`: Idle timeout (in sec). Tracing is stopped if there is no data for a specified period of time. By default -1 (disable this stop trigger). Optionally set it to a value longer than the longest pause between tracing commands from the target.
	* `wait4halt`: If 0 start tracing immediately, otherwise command waits for the target to be halted (after reset, by breakpoint etc.) and then automatically resumes it and starts tracing. By default, 0.
	* `skip_size`: Number of bytes to skip at the start. By default, 0.

Additional information can be found [here](https://docs.espressif.com/projects/esp-idf/en/latest/esp32c3/api-guides/app_trace.html?).

![](docs/images/AppLvlTracing_4.png)

The next two fields `Trace Processing Script` and `Start Parsing Command` are used to parse the output file.

* `Trace Processing Script` is used to provide the path to the parsing script, by default it is logtrace_proc.py from esp-idf.
* `Start Parsing Command` allows you to check the resulting parsing command and edit it if it's necessary. By default, this field is automatically configured to match `$IDF_PATH/tools/esp_app_trace/logtrace_proc.py/path/to/trace/file/path/to/program/elf/file`.

Note the `Start parse` button is disabled until a dump file is available. To generate it, click the `Start` button at the bottom of the dialog box. After you click, the button changes to Stop so that you can stop tracking.

When the output file is generated, you can click on `Start parse` button, and you will see the parsed script output in the Eclipse console:

![](docs/images/AppLvlTracing_5.png)

<a name ="updateEspIdfMaster"></a>
# ESP-IDF Master Update

If you are using the master version of ESP-IDF and want to update it, you can do so in the plugin by going to `Espressif -> ESP-IDF Tool Manager` and clicking the `Update ESP-IDF master` command there.

![image](https://user-images.githubusercontent.com/24419842/182107159-16723759-65e0-4c34-9440-ebe2f536e62a.png)

> **Note:** This command is visible only if you are on the master branch in ESP-IDF.

<a name ="partitionTableEditor"></a>
# Partition Table Editor UI for ESP-IDF

`ESP-IDF: Partition Table Editor` command allows to edit your [partition table](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html) in a more convenient way, where you can see the supported types and subtypes and monitor the correctness of the entered data.

> Note: This command is available in the IDF-Eclipse plugin 2.8.0 and higher.

Steps:

1. Go to `Project Explorer`, open any IDF Project where you want to have a customized partition table.
1. In `Project Explorer`, right-click on the project and click on `ESP-IDF: Partition Table Editor` command:

	![partition_table_editor_3](https://user-images.githubusercontent.com/24419842/216105408-ca2e73ce-5df3-4bdd-ac61-b7265deb9b44.png)

	When opening the partition table editor for the selected project, you will see the standard editable content. Errors (if any) will be highlighted. You can hover your mouse over it to get a hint of what it is about:

	![partition_table_editor_4](https://user-images.githubusercontent.com/24419842/216106804-703b2eb4-b141-48de-8559-0599f072219f.png)

1. Click "Save" or "Save and Quit" to save your changes.

To use a customized partition table:

1. Go to `sdkconfig` and set `Custom partition table CSV` like below:

	![partition_table_editor](https://user-images.githubusercontent.com/24419842/216104107-2844068b-8412-468b-931f-b4778af4417c.png)

<a name ="nvsTableEditor"></a>
# NVS Table Editor

`NVS Table Editor` helps to create a binary file based on key-value pairs provided in a CSV file. The resulting binary file is compatible with NVS architecture defined in [ESP_IDF Non Volatile Storage](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/storage/nvs_flash.html). The expected CSV format is:

```
	key,type,encoding,value     <-- column header (must be the first line)
	namespace_name,namespace,,  <-- First entry must be of type "namespace"
	key1,data,u8,1
	key2,file,string,/path/to/file
```
> Note: This is based on ESP-IDF [NVS Partition Generator Utility](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/storage/nvs_partition_gen.html).

Steps:

1. Right-click on project in the `Project Explorer`
1. Select the `ESP-IDF: NVS Table Editor` command:

	![NVS Table Editor](https://user-images.githubusercontent.com/24419842/216114697-9f231211-f5dd-431b-9432-93ecc656cfec.png)

	> Note: This command is available in the IDF-Eclipse plugin 2.8.0 and higher

1. Make desired changes to CSV data
1. Save changes by clicking the `Save` button. If everything is ok, you will see an information message at the top of the dialog:

	![NVS_TABLE_EDITOR_2png](https://user-images.githubusercontent.com/24419842/216115906-9bb4fe55-293b-4c6b-8d22-0aa3520581ab.png)

1. Generate the partition binary (Choose `encrypt` to encrypt the binary and disable the generate key option to use your own key if desired). You will see an information message at the top of the dialog about the result of generated binaries. You can hover your mouse over it to read the whole message if it's too long:

	![NVS_Table_Editor_4](https://user-images.githubusercontent.com/24419842/216117261-9bee798a-3a9e-4be5-9466-fc9d3847834b.png)

	> Note: If there are any errors, you will see them highlight, hover on the error icon to read more about the error. Also, you will see an error message at the top of the dialog if saving the CSV file is not successful:

	![NVS_Table_editor_5](https://user-images.githubusercontent.com/24419842/216118486-69f819fa-7a95-49ae-805e-473cd2c424e8.png)

After all these steps, you should see `nvs.csv` and `nvs.bin` files in the project directory.

<a name ="writeFlashBinary"></a> 
# Write Binary to Flash

Binary data can be written to the ESP’s flash chip via `ESP-IDF: Write Binary Data to Flash` command accessible by right click on the project in the project explorer:

<img width="344" alt="Screenshot 2023-10-16 at 10 51 52" src="https://github.com/espressif/idf-eclipse-plugin/assets/24419842/186c8498-d779-4771-af53-e5bf09e29502">

After clicking this command, the `Write Binary Data to Flash` dialog box will open. Editable default values ​​are provided for binary path and offset. The correct offset can be checked by looking at the partition table via `ESP-IDF: Partition Table Editor` or manually by opening the partitions.csv file

<img width="591" alt="Screenshot 2023-10-16 at 10 51 27" src="https://github.com/espressif/idf-eclipse-plugin/assets/24419842/46e24e89-a1ed-4169-8c92-1ba0b0089ea7">

After clicking on the `Flash` button the result of the flash command will be printed inside of this dialog.

# How to build locally

1. Install prerequisites Java 11+ and Maven.
2. Run the below commands to clone and build.

	```
	git clone https://github.com/espressif/idf-eclipse-plugin.git
	cd idf-eclipse-plugin
	mvn clean verify -Djarsigner.skip=true
	```

This will generate p2 update site artifact:

* Name: `com.espressif.idf.update-*`
* Location: `releng/com.espressif.idf.update/target`

This artifact can be installed using the mechanism mentioned <a href="https://github.com/espressif/idf-eclipse-plugin#installPluginsUsingLocalFile">here</a>

# How do I get the latest development build

1. Go to the last commit of the master branch <a href="https://github.com/espressif/idf-eclipse-plugin/commits/master">here</a>.
1. Click on a :white_check_mark: green tick mark.
1. Click on `Details`.
1. Click on `Summary` on the left.
1. Scroll down to see the `Artifacts` section.
1. Download `com.espressif.idf.update` p2 update site archive and install as per the instructions mentioned <a
href="https://github.com/espressif/idf-eclipse-plugin#installPluginsUsingLocalFile">here</a>.

# Custom IDE Configuration
## Custom build directory

IDE allows configuring a custom build directory to the project:

1. Select a project and click on a launch configuration `Edit` button from the top toolbar and this will the launch `Edit Configuration` window.
2. Navigate to the `Build Settings` tab.
3. In the `Additional CMake Arguments` section, provide a custom build directory with arguments `-B <custom build path>` with an absolute path. Customized build directory path could be within the project or a path from the file system. For example: `-B /Users/myUser/esp/generated`.
4. Click on `Ok` and build the project.

Note this configuration changes where all the project build artifacts will be generated.

![](docs/images/custombuilddir.png)

<a name ="wokwisimulator"></a>
# Wokwi Simulator

1. Install `wokwi-server` as mentioned [here](https://github.com/MabezDev/wokwi-server/)
1. In the Eclipse CDT build environment variables, configure `WOKWI_SERVER_PATH` with the wokwi-server executable path (`Preferences` > `C/C++` > `Build` > `Environment`).
1. Create a new `Run launch configuration` with the `Wokwi Simulator`.
1. Choose a project and add the project ID of the Wokwi project. The ID of a Wokwi project can be found in the URL. E.g., the URL of project ESP32 Rust Blinky is [https://wokwi.com/projects/345932416223806035](https://wokwi.com/projects/345932416223806035) and the project ID is 345932416223806035.
1. Click `Finish` to save the changes.
1. From the IDE Toolbar, click on the `Launch` button to launch the Wokwi simulator.
1. Wokwi Simulator will be launched in the external browser. The serial monitor output is also displayed in the Eclipse CDT build console.
1. To kill a Wokwi simulator, click on the `Stop` button from the toolbar.

# ESP-IDF Eclipse Plugin Compatibility Matrix

| IEP | Eclipse | Java | Installer | Description |
| ------ | ------ | ------ |------ | ------ |
| IEP 2.11.0 | Eclipse 2023-03, Eclipse 2023-06, Eclipse 2023-09 |Java 17 and above | |
| IEP 2.10.0 | Eclipse 2022-09,2022-12,2023-03 |Java 17 and above | [espressif-ide-setup-2.10.0-with-esp-idf-5.0.1.exe](https://github.com/espressif/idf-installer/releases/download/untagged-52aeb689780472c126c1/espressif-ide-setup-2.10.0-with-esp-idf-5.0.1.exe)|
| IEP 2.9.1 | Eclipse 2022-09 and Eclipse  2022-12 |Java 17 and above | [espressif-ide-setup-2.9.0-with-esp-idf-5.0.1.exe](https://github.com/espressif/idf-installer/releases/download/espressif-ide-2.9.0-esp-idf-5.0.1/espressif-ide-setup-2.9.0-with-esp-idf-5.0.1.exe) | For Windows Users, it's recommended to use the Windows Offline Installer and upgrade to the latest IEP v2.9.1 plugin|
| IEP 2.9.0 | Eclipse 2022-09 |Java 17 and above | [espressif-ide-setup-2.9.0-with-esp-idf-5.0.1.exe](https://github.com/espressif/idf-installer/releases/download/espressif-ide-2.9.0-esp-idf-5.0.1/espressif-ide-setup-2.9.0-with-esp-idf-5.0.1.exe) | For Windows, it's recommended to use the Windows Offline Installer|


<a name="Support"></a>
# How to raise bugs

Please raise the issues [here](https://github.com/espressif/idf-eclipse-plugin/issues) with the complete environment details and log.
