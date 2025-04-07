FAQ
====

How do I know the installed version of Java in my system?
----------------------------------------------------------
You can check using `java -version` command from the terminal.

How to check the Java version used by Eclipse?
----------------------------------------------
- `Help > About Eclipse > Installation Details > Configuration`
- Look for `-vm` argument.

How to increase the heap memory for Java while working Eclipse IDE?
--------------------------------------------------------------------
- Locate the eclipse.ini or espressif-ide.ini
- Increase the Xmx value under the `-vmargs` args section. For example, you can set to `-Xmx2048m`.

What are the operating systems the plugin supports?
----------------------------------------------------
- Windows
- macOSX
- Linux

How do I provide Eclipse environment and plugins information?
-------------------------------------------------------------
`Help > About Eclipse > Installation Details > Configuration > Copy to Clipboard`

How do I know the installed IDF Eclipse Plugins version?
---------------------------------------------------------
- You can check using the menu `Eclipse > About Eclipse > Installation Details > Installed Software`
- Search for `Espressif`.

How do I uninstall IDF Eclipse Plugins from the Eclipse?
---------------------------------------------------------
- `Eclipse > About Eclipse > Installation Details > Installed Software`
- Search for `Espressif`.
- Select the Espressif IDF Feature
- `Uninstall..`.

Unable to install IDF plugins in Eclipse?
-----------------------------------------
Please check the error log from the main menu, select `Window > Show View > Other`. Then select `General > Error Log`.

Espressif Menu options and Espressif IDF Project menu are not visible in my Eclipse CDT
---------------------------------------------------------------------------------------
- Make sure you have installed Java 8 and above and Eclipse in the C/C++ perspective.
- Reset the perspective using `Window > Perspective > Reset Perspective..`.

Do IDF Eclipse Plugins support CMake IDF project creation?
----------------------------------------------------------
Yes, you can create IDF CMake project using `File > New > Espressif IDF Project`.

Can I import my existing IDF project into Eclipse?
---------------------------------------------------
Yes, you can import using Import Menu. `Import... > Espressif > Existing IDF Project`.

Where can I find the IDF installed tools in my system?
------------------------------------------------------
Default directory is `$HOME/.espressif` for Linux/MacOS users or `%USER_PROFILE%.espressif` for Windows users.

Why am I getting timeout errors when Installing tools?
------------------------------------------------------
If you are getting errors when downloading or installing tools this can be due to some issue with the mirrors. You can try to set the mirrors in Eclipse `Preferences > Espressif` you will see two settings for Git and Pip Py Wheels please set these to proper mirror according to your region. Currently, these two mirrors are available.

### Mirror for GIT (IDF_GITHUB_ASSETS)
- dl.espressif.com/github_assets (default)
- dl.espressif.cn/github_assets

### Mirror for python wheels (PIP_EXTRA_INDEX_URL)
- https://dl.espressif.com/pypi (default)
- https://dl.espressif.cn/pypi

Deleted C/C++ build environment variables still appearing?
----------------------------------------------------------
- You need to uncheck the preference recorder. This can be performed by following. Eclipse `Preferences > Oomph > Setup Tasks > Preference Recorder`.
- Uncheck `Record into`.

How can I rollback to old ESP-IDF Eclipse plugin?
-------------------------------------------------
- Open Eclipse IDE and Uninstall the esp-idf plugin.
- Restart Eclipse IDE.
- Download the previous version of the ESP Eclipse Plugin from the release page
- Go to `Help > Install New Software`.
- Press the `Add` button, a window will open with the name of `Add Repository`.
- Press the `Archive` button and select the file downloaded.
- Proceed with the installation.
- Restart Eclipse.

Where can I find Compiler_commands.json file for the project?
--------------------------------------------------------------
`/projectName/build/compile_commands.json`

compile_commands.json containing the exact compiler calls for all translation units of the project in machine-readable form which is used by the Eclipse CDT indexing for parsing and resolving headers.

How do I access CDT Parser error log?
--------------------------------------
Please follow this menu. `Project > C/C++ Index > Create Parser Log`.

How do I access the error log?
------------------------------
To view the Eclipse error log: From the main menu, select `Window > Show View > Other`. Then select `General > Error Log`.

How do I report a deadlock or Eclipse hang?
-------------------------------------------
You can find the detailed instructions here: https://wiki.eclipse.org/How_to_report_a_deadlock.
- On the command line, use `jps -v` to find the PIDs of Java processes and `jstack <pid>` to show the stack trace of Java processes.

Here 32308 and 8824 are PIDs of Java processes. 8824 is jps itself and is of no interest for us. 32308 is an Eclipse process judging from the presence of `org.eclipse.equinox.launcher` in its command line. The `jstack` command saves the stack trace of the Eclipse process in a file `/tmp/jstack.txt`, attach the file to the bug report.

`sun.security.validator.ValidatorException: PKIX path building failed:` error
------------------------------------------------------------------------------
This would have been caused by the Java version or Java certificates. Please make sure you've installed `Java 11 and later` to fix this error.
Check below links:
- https://esp32.com/viewtopic.php?f=13&t=12327&start=10#p50137
- https://stackoverflow.com/questions/6908948/java-sun-security-provider-certpath-suncertpathbuilderexception-unable-to-find

Why Java 11 recommended for IDF Eclipse Plugin?
-----------------------------------------------
We recommend using Java 11 (that's the latest LTS version from Oracle) and above while working with IDF Eclipse Plugin considering Eclipse 2020-06 has a requirement for Java 11 to work with the CDT. Here are some important pointers from Eclipse.

### Installing CDT 9.11 on Eclipse 2020-06 and later requires a workaround when using Java 8
Check this - https://wiki.eclipse.org/CDT/User/NewIn911#Release

CDT 9.11 only requires Java 8 to run. However, a new feature in Eclipse 2020-06 and later means that the install wizard may prevent installation. The workaround is to disable "Verify provisioning operation is compatible with the currently running JRE" in Windows -> Preferences -> Install/Update. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=564407#c1.

### CDT 10.0 required Java 11 or later
Check this - https://wiki.eclipse.org/CDT/User/NewIn100

Starting with CDT 10.0, Java 11 or later is required to run CDT. This aligns with the requirements of Eclipse IDE which also requires Java 11 to run starting in 2020-09.

How to delete Launch Targets from the Eclipse
---------------------------------------------
There is no UI option to delete launch targets directly from Eclipse, however, this can be achieved by following the below instructions:
- Go to the Eclipse workspace directory. For example: In my case `/Users/myName/myTesteclipseWorkspace`.
- Navigate to `.metadata/.plugins/org.eclipse.core.runtime/.settings` folder in the workspace directory.
- Look for `org.eclipse.launchbar.core.prefs` file and open it in the editor.
- Search for the launch target name you want to delete and remove all those entries from the file.
- Save the file.
- Restart Eclipse.

How do I access project build log?
-----------------------------------
- To enable logging, navigate to `Preferences > Project > C/C++ > Build > Logging`.
- Check `Enable global build logging`.
- Build the project.
- Export `global-build.log`. This is the same build console log which appears in the CDT build console, but the build console usually has a limited buffer size, hence it won't display everything.

How do I enable verbose debug output to my project build?
----------------------------------------------------------
IDF Eclipse plugin uses CMake commands to build the project, so it's possible to pass CMake arguments from the build configuration wizard. To configure this:
- Click on the editor configuration wizard.
- Navigate to `Build Settings` tab.
- Add `--debug-output` or other

How do I build multiple configurations in Espressif-IDE?
----------------------------------------------------------
- Create a new project.
- Open the `Launch Configuration` dialog.
- Navigate to the `Build Settings` tab and enter `-B build_release` in the `Additional CMake Arguments` section. Here, `build_release` is the name of the build folder.
- Click the `OK` button to save the configuration.
- Reopen the `Launch Configuration` dialog.
- Click the `Duplicate` button (located at the bottom left corner).
- Navigate to the `Build Settings` tab and update the `Additional CMake Arguments` section to `-B build_dev`. Here, `build_dev` is the name of the build folder.
- Click the `OK` button to save the configuration.
- Click the Build icon from the toolbar (the leftmost icon) for the selected configuration. This will build the project and create a build folder for that configuration. Repeat the same process for the other configuration by selecting it from the dropdown.

Can I Use My Old C/C++ Editor Formatter File (.xml) as a .clang-format File?
----------------------------------------------------------------------------
No, you cannot directly use the old `.xml` (CDT formatter) file with ESP-IDF projects, as these now use the CDT LSP Editor, which relies on Clangd for code formatting. Clangd requires a `.clang-format` file, and there is no official tool to convert `.xml` formatter files to the Clang format.

However, Clang provides several default formatting styles (such as LLVM, Google, Mozilla, etc.) that you can use as a starting point. You can generate a default `.clang-format` file using the following command:

.. code-block:: none

  clang-format -style=llvm -dump-config > .clang-format

For new ESP-IDF projects, a `.clang-format` file is automatically created in the root directory with default settings. This file is picked up by Clangd automatically—no additional configuration is needed.

For existing projects, you can create this file manually by right-clicking the project and selecting:

  ESP-IDF > Create Clangd File

If you would like to replicate your old formatter settings, you can either:

- Manually map your `.xml` settings to Clang format using Clang’s formatting guide.
- Or Use an AI tool(e.g: ChatGPT) to assist in converting your old configuration to the new one and then manually adjust if there are any discrepancies.

More information on the Clang format can be found in the `Clang Format documentation <https://clang.llvm.org/docs/ClangFormat.html>`_ and formatting styles can be found in the `Clang Format Style Options <https://clang.llvm.org/docs/ClangFormatStyleOptions.html>`_.

