# FAQ

## Which version of Java should I use? 
Java 8 and above
## Which version of Eclipse should I use?
Eclipse 2018-12 CDT and above (Eclipse IDE for C/C++ Developers)
## How do I know the installed version of Java in my system?
You can check using "java -version" command from the terminal
##  How to check the Java version used by Eclipse?
Help > About Eclipse >Installation Details > Configuration > Look for "-vm" argument
## Espressif Menu options and Espressif IDF Project menu is not visible in my Eclipse
Make sure you have installed Java 8 and higher and you're in the C/C++ perspective
##  How do I access the error log?
To view the Eclipse error log: From the main menu, select Window > Show View > Other. Then select General > Error Log.
## How do I know the installed IDF Eclipse Plugins version?
You can check using Eclipse menu > About Eclipse > Installation Details > Installed Software > Search for "Espressif"
## How do I uninstall IDF Eclipse Plugins from the Eclipse?
Eclipse > About Eclipse > Installation Details > Installed Software > Search for "espressif" > Select the espressif IDF plugins > Uninstall..
## Unable to install IDF plugins in Eclipse?
Please check the error log from the main menu, select Window > Show View > Other. Then select General > Error Log. 
## Do IDF Eclipse Plugins support CMake IDF project creation?
Yes, you can create IDF CMake project using File > New > Espressif IDF Project
## Can I import my existing IDF project into Eclipse?
Yes, you can import using Import Menu. Import... > Espressif > Existing IDF Project
## Deleted C/C++ build envrionment variables still appearing?
Uncheck Eclipse Oomph Preference Recorder. Which can be performed by following. Eclipse Preferences >Oomph > Setup Tasks > Preference Recorder > Uncheck "Record into".
## "Unresolved inclusion" errors in the editor
You need to select the esp "Launch Target" before you trigger the build so that CDT will correctly identify the toolchain to build and index the sources. Check "Configure Launch Target" for more details.
## "No Toolchain found for Target ESP32" error
You need to make sure you run this step https://github.com/espressif/idf-eclipse-plugin#installing-esp-idf-tools. This will take care of configuring the required paths in the CDT build environment along with installing the tools.

Check this https://github.com/espressif/idf-eclipse-plugin#configuring-core-build-toolchains
## "No esp launch target found. Please create/select the correct Launch Target"
You see this error when you're trying a compile a project without selecting an esp launch target. Check this https://github.com/espressif/idf-eclipse-plugin#configuring-launch-target
## How can I rollback to old esp-idf eclipse plugin?
- Open Eclipse IDE and Uninstall the esp-idf plugin
- Restart Eclipse IDE
- Download the previous version of the ESP Eclipse Plugin using this [link](https://github.com/espressif/idf-eclipse-plugin/releases)
- Goto Help->Install New Software
- Press the "Add" button, a window will open with the name of "Add Repository"
- Press the "Archive" button and select the file downloaded
- Proceed with the installation
- Restart Eclipse