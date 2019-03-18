# idf-eclipse-plugin

Eclipse IDE for Espressif IoT Development Framework(IDF)

## Setting up an Eclipse Development Environment:
1. Download and install `Java SE 11`(latest Java 11). Here is the download link https://www.oracle.com/technetwork/java/javase/downloads/index.html
2. Download and install `Eclipse RCP package` (latest Eclipse 2018-12) which you can find here https://www.eclipse.org/downloads/packages/
3. Install the `m2eclipse` feature into eclipse from the update site: https://www.eclipse.org/m2e/
4. Import the plugins, features, and test plugins into your workspace. `File > Import > Maven > Existing Maven Projects`. Select the directory this repo was cloned into.


## How to contribute:
1. Fork this repo 
2. Ensure you’ve installed Maven locally https://www.vogella.com/tutorials/ApacheMaven/article.html#maven_installation 
3. Make changes locally on your fork
4. Test with Maven Tycho using `$ mvn clean verify`
5. Submit a Merge Request(MR)


## Download and Installation:

1. Download `Eclipse CDT package` and install it. https://www.eclipse.org/downloads/packages/release/2018-12/r/eclipse-ide-cc-developers
2. Download the Espressif IDF eclipse `artifacts.zip` file from https://gitlab.espressif.cn:6688/idf/idf-eclipse-plugin/-/jobs/artifacts/master/download?job=build
3. Extract the above downloaded zip file
4. Launch Eclipse
5. Go to `Help` -> `Install New Software`
6. Click `Add…` button
7. Select `Achieve` from Add repository dialog and select the file ``com.espressif.idf.update-1.0.0-SNAPSHOT.zip`` from the exacted folder
8. Click `Add`
9. Select `Espressif IDF` from the list and proceed with the installation 
10. Restart the Eclipse

## Configuring Environment Variables:
* Click on the “Environment” properties page under “C/C++ Build”. Click “Add…” and enter name `BATCH_BUILD` and value 1.
* Click “Add…” again, and enter name `IDF_PATH`. The value should be the full path where ESP-IDF is installed. Windows users can copy the IDF_PATH from windows explorer.
* Edit the `PATH` environment variable. Keep the current value, and append the path to the Xtensa toolchain installed as part of IDF setup, if this is not already listed on the PATH. A typical path to the toolchain looks like `/home/user-name/esp/xtensa-esp32-elf/bin`. Note that you need to add a colon : before the appended path. Windows users will need to prepend `C:\msys32\mingw32\bin;C:\msys32\opt\xtensa-esp32-elf\bin;C:\msys32\usr\bin` to PATH environment variable (If you installed msys32 to a different directory then you’ll need to change these paths to match).
* On macOS, add a PYTHONPATH environment variable and set it to /Library/Frameworks/Python.framework/Versions/2.7/lib/python2.7/site-packages. This is so that the system Python, which has pyserial installed as part of the setup steps, overrides any built-in Eclipse Python.

**ADDITIONAL NOTE:** If either the IDF_PATH directory or the project directory is located outside C:\msys32\home directory, you will have to give custom build command in C/C++ Build properties as: python ${IDF_PATH}/tools/windows/eclipse_make.py (Please note that the build time may get significantly increased by this method.) 

## CMake IDF Project

# To create a new Project using default esp-idf-template:
1. Make sure you're in C/C++ perspective.
2. Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`)
3. Provide the project Name
4. Click `Finish`


# To create a new project using idf examples/templates:
1. Make sure you're in C/C++ perspective.
2. Go to `File` > `New` > `Espressif IDF Project` (If you don't see this, please reset the perspective from `Window` > `Perspective` > `Reset Perspective..`)
3. Provide the project Name
4. Click `Next`
5. Check `Create a project using one of the templates`
6. Select the required template from the tree
7. Click `Finish`

#  Import an existing IDF Project
1. Make sure you're in `C/C++ Perspective`.
2. Right click on the Project Explorer
3. Select `Import..` Menu
4. Select `Existing IDF Project` from `Espressif` import wizard menu list
5. Click `Next`
6. Click on `Browse...` to choose an existing project location directory
7. Provide project name if you wish you have a different name
8. Click `Finish` to import the selected project into eclipse workspace as a CMake project


## Building the IDF projects

## Configuring Core Build Toolchains

* Open Eclipse Preferences
* Navigate to “C/C++  -> “Core Build Toolchains” preference page
* Click on `Add..` from the User defined Toolchians tables
* Select `GCC` as a toolchain type
* Click on `Next>`
* Provide the GCC Toolchain Settings:

**Compiler:** /Users/kondal/esp/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc
**Operating System:** esp32
**CPU Architecture:** xtensa

## Configuring CMake Toolchain
We now need to tell CDT which toolchain to use when building the project. This will pass the required arguments to CMake when generating the Ninja files.

In the Preferences, select C/C++ and then CMake to see the list of toolchain files CDT will use with CMake. Click Add. Each CMake toolchain file is associated with a toolchain. Since we have the xtensa toolchain in our PATH, CDT will auto discover it and it will appear in the dropdown. Also enter the location of the esp-idf toolchain file which is in the esp-idf repo under tools/cmake/toolchain-esp32.cmake.

* Navigate to “C/C++  -> “CMake” preference page
* Click `Add..` and this will launch the New CMake Toolchain configuration dialog
* Browse CMake toolchain `Path`. Example: `/Users/kondal/esp/esp-idf/tools/cmake/toolchain-esp32.cmake`
* Select GCC Xtensa Toolchain compiler from the drop-down list. example: `esp32 xtensa /Users/kondal/esp/xtensa-esp32-elf/bin/xtensa-esp32-elf-gcc`

**ADDITIONAL NOTE:**  Eclipse need to be restared before we move further configuring the launch target for your application.

## Configuring Launch target
Next we need to tell CDT to use the toolchain for our project. This is accomplished through the Launch Bar, the new widget set you see on the far left of the toolbar. And this will be shown only when you've a project in the project explorer.

* Click on the third dropdown 
* Select `New Launch Target`
* Select `Serial Flash Target`
* Provide properties for the target where you would like to launch the application. Enter a name for the target, “esp32” as the operating system, “xtensa” as the CPU architecture, and select the serial port your ESP32 device is connected to on your machine. The OS and architecture need to match the settings for the toolchain. You can see those settings in the Preferences by selecting C/C++ and Core Build Toolchains. For GCC toolchains, CDT autodetects those settings by asking GCC for it’s target triple.

## Building the Application
* Select a project from the Project Explorer
* Select `Run` from the first drop-down, which is called `Launch Mode`
* Select your applicaton from the second drop-down, which is called `Launch Configuration`
* Select target from the third drop-down, which is called `Launch Target`
* Now click on the `Build` button widget which you see on the far left of the toolbar

**ADDITIONAL NOTE:**  When everything configured correctly as specified above - you will see that `cmake.run.esp32.xtensa` folder is created under the project build folder, this is where the application build articats will be generated. 

## Configuring the Launch Terminal
To see what program does we need to configure eclipse terminal to connect the serial port.

* Click on the "Open a Terminal" icon from the toolbar
* Choose `Serial Terminal` from the terminal drop-down
* Select `Serial Port` for your board. Example: /dev/cu.SLAB_USBtoUART
* And, configure the remaining settings and click on Ok to launch the eclipse terminal and which will listen the USB port

**ADDITIONAL NOTE:** This won't display anything immediately until the application is flashed to the board. 

## Flashing the Application
ESP-IDF has a tool called "idf.py" which is a wrapper around make flash command with some handy operations. Follow the below instructions to hook the idf.py to the launch configuration

* Click on the `Launch Configuration` edit button
* Switch to the `Main` tab
* Specify the `Location` where this application has to run on. Since idf.py is a python file, will configure the python system path. Example:{system_path:python}
* Specify `Working directory` of the application. Example: `${workspace_loc:/hello_world}`
* In additional arguments, provide a flashing command which will run in the specified working directory
* Flash command looks like this: `/Users/kondal/esp/esp-idf/tools/idf.py -p /dev/cu.SLAB_USBtoUART flash`
* Click OK to save the settings
* Click on the `Launch` icon to flash the application to the selected board 

**ADDITIONAL NOTE:** Launch configuration has issues in accessing eclipse C/C++ environment variables, hence we need to define PATH again in the Lauch configuration `Environment` tab to get the access to the CMake command.





## How to raise bugs
Raise issues directly under espressif JIRA here https://jira.espressif.com:8443/projects/IEP/issues/ with the project name `IDF Eclipse Plugin(IEP)`

