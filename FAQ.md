# FAQ

## Which version of Java should I use? 
Java 11 and above. We suggest to use the latest LTS version. Check the plugin [Prerequisites](https://github.com/espressif/idf-eclipse-plugin#Prerequisites). 
Also check [why we recommend Java 11](https://gitlab.espressif.cn:6688/idf/idf-eclipse-plugin/-/blob/master/FAQ.md#why-java-11-recommended-for-idf-eclipse-plugin) while using the IDF Eclipse plugin 
## Which version of Eclipse should I use?
Eclipse 2020-06 CDT and above (Eclipse IDE for C/C++ Developers). Check the plugin [Prerequisites](https://github.com/espressif/idf-eclipse-plugin#Prerequisites)
## How do I know the installed version of Java in my system?
You can check using `java -version` command from the terminal
##  How to check the Java version used by Eclipse?
- `Help > About Eclipse > Installation Details > Configuration`  
- Look for `-vm` argument
## What are the operating systems the plugin supports?
- Windows
- macOSX
- Linux
## How do I provide Eclipse envrionment and plugins information?
`Help > About Eclipse > Installation Details > Configuration > Copy to Clipboard` 
## How do I know the installed IDF Eclipse Plugins version?
- You can check using the menu `Eclipse > About Eclipse > Installation Details > Installed Software`  
- Search for `Espressif`
## How do I uninstall IDF Eclipse Plugins from the Eclipse?
- `Eclipse > About Eclipse > Installation Details > Installed Software`  
- Search for `Espressif`
- Select the Espressif IDF Feature  
- `Uninstall..`
## Unable to install IDF plugins in Eclipse?
Please check the error log from the main menu, select `Window > Show View > Other`. Then select `General > Error Log`
## Espressif Menu options and Espressif IDF Project menu are not visible in my Eclipse CDT
- Make sure you have installed Java 8 and above and Eclipse in the C/C++ perspective 
- Reset the perspective using `Window > Perspective > Reset Perspective..`
## Do IDF Eclipse Plugins support CMake IDF project creation?
Yes, you can create IDF CMake project using `File > New > Espressif IDF Project`
## Can I import my existing IDF project into Eclipse?
Yes, you can import using Import Menu. `Import... > Espressif > Existing IDF Project`
## Where can I find the IDF installed tools in my system?
Default directory is `$HOME/.espressif` for Linux/MacOS users or `%USER_PROFILE%.espressif` for Windows users
## Deleted C/C++ build envrionment variables still appearing?
- You need to uncheck the preference recorder. This can be performed by following. Eclipse `Preferences > Oomph > Setup Tasks > Preference Recorder`  
- Uncheck `Record into`
## "Unresolved inclusion" errors in the editor
Unresolved inclusion errors will be resolved only after the build. Make sure you select the `ESP` Launch Target before you trigger the build so that CDT will correctly identify the toolchain to build and index the sources. Check [Configure Launch Target](https://github.com/espressif/idf-eclipse-plugin#ConfigureLaunchTarget) for more details.

If you still see the errors, please try `Project > C/C++ Index > Rebuild` and see if it helps.
## "No Toolchain found for Target ESP32" error
You need to make sure you run the [Install Tools](https://github.com/espressif/idf-eclipse-plugin#installing-esp-idf-tools) from the Eclipse. This will take care of configuring the required paths in the CDT build environment along with installing the tools.

Check [Configuring core build toolchains](https://github.com/espressif/idf-eclipse-plugin#configuring-core-build-toolchains)
## "No esp launch target found. Please create/select the correct Launch Target" error
You see this error when you're trying a compile a project without selecting an `ESP` launch target. Check [this](https://github.com/espressif/idf-eclipse-plugin#configuring-launch-target)
## How can I rollback to old ESP-IDF Eclipse plugin?
- Open Eclipse IDE and Uninstall the esp-idf plugin
- Restart Eclipse IDE
- Download the previous version of the ESP Eclipse Plugin using this [link](https://github.com/espressif/idf-eclipse-plugin/releases)
- Goto `Help > Install New Software`
- Press the `Add` button, a window will open with the name of `Add Repository`
- Press the `Archive` button and select the file downloaded
- Proceed with the installation
- Restart Eclipse
## Where can I find Compiler_commands.json file for the project?
`/projectName/build/compile_commands.json`
## How do I access CDT Parser error log?
Please follow this menu. `Project > C/C++ Index >  Create Parser Log`
##  How do I access the error log?
To view the Eclipse error log: From the main menu, select `Window > Show View > Other`. Then select `General > Error Log`
Check more details [here](https://github.com/espressif/idf-eclipse-plugin#error-log)
## How do I report a deadlock or Eclipse hang?
You can find the detailed instructions here https://wiki.eclipse.org/How_to_report_a_deadlock
- On command line, use `jps -v` to find the PIDs of Java processes and jstack <pid> to show the stack trace of Java processes.
<pre>
<code>$ jps -v
32308 org.eclipse.equinox.launcher_1.5.0.v20180119-0753.jar -Dosgi.requiredJavaVersion=1.8 -Dosgi.instance.area.default=@user.home/eclipse-workspace -XX:+UseG1GC -XX:+UseStringDeduplication -Dosgi.requiredJavaVersion=1.8 -Dosgi.dataAreaRequiresExplicitInit=true -Xms256m -Xmx1024m -Dswt.dbus.init
8824 Jps -Dapplication.home=/usr/lib/jvm/java-8-oracle -Xms8m
$ jstack 32308 > /tmp/jstack.txt
</code></pre>
Here 32308 and 8824 are PIDs of Java processes. 8824 is jps itself and is of no interest for us. 32308 is and Eclipse process judging from presence of org.eclipse.equinox.launcher in its command line. Jstack command saves stack trace of Eclipse process in a file /tmp/jstack.txt, attach the file to bug report.
## `sun.security.validator.ValidatorException: PKIX path building failed:` error while installing the plugins through update site
This would have caused by the Java version or Java certificates. Please make sure you've installed `Java 11 and later` to fix this error. 
Check below links:
- https://esp32.com/viewtopic.php?f=13&t=12327&start=10#p50137 
- https://stackoverflow.com/questions/6908948/java-sun-security-provider-certpath-suncertpathbuilderexception-unable-to-find

## Why Java 11 recommended for IDF Eclipse Plugin?
We recommend using Java 11(that's the latest LTS version from Oracle) and above while working with IDF Eclipse Plugin considering Eclipse 2020-06 has a requirement for Java 11 to work with the CDT. Here are some important pointers from Eclipse.

### Installing CDT 9.11 on Eclipse 2020-06 and later requires a workaround when using Java 8
Check this - https://wiki.eclipse.org/CDT/User/NewIn911#Release

CDT 9.11 only requires Java 8 to run. However, a new feature in Eclipse 2020-06 and later means that the install wizard may prevent installation. The workaround is to disable "Verify provisioning operation is compatible with currently running JRE" in Windows -> Preferences -> Install/Update. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=564407#c1

### CDT 10.0 required Java 11 or later
Check this - https://wiki.eclipse.org/CDT/User/NewIn100

Starting with CDT 10.0 Java 11 or later is required to run CDT. This aligns with requirements of Eclipse IDE which also requires Java 11 to run starting in 2020-09.