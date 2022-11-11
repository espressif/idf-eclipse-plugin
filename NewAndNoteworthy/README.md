Espressif-IDE Release Notes and New & Noteworthy

# ESP-IDF Eclipse Plugin v2.7.0

This is a minor release that offers Eclipse 2022-09 support, core dump debugging, automatic loading of SVD files for peripherals view, hints viewers for troubleshooting, ESP32-C2 and ESP32-H2 support in IDEs, index improvements, and many other bug fixes.

You should be able to install IEP 2.7.0 update site in Eclipse CDT 2022-06 and higher. If you're using Eclipse Embedded 2022-06 and higher will be an update site error, however, eclipse suggests an alternative solution that will still install the 2.7.0 plugin correctly and shouldn't impact anything else.

- IEP-648: Generated esp_idf_components folder moved to project/build/ide
- IEP-681: Show Tools installation error messages in red color 
- IEP-744: Fix for eclipse launch parameter for CI Issues 
- IEP-749: Hints viewer based on the hints.yml 
- IEP-72: Core Dump Debugging 
- esp-adf configuration in espressif-ide
- IEP-765:Configure esp-who in espressif-ide 
- IEP-351: Automatic loading of SVD for Peripherals view 
- IEP-768: Eclipse 2022-09 support 
- IEP-729: ESP32-C2 and ESP32-H2 support in IDEs 
- doc: How to choose to build with Clang or GCC toolchain 
- IEP-740: Chinese Translations of doc
- IEP-790: remove duplicate targets from target lists 
- docs: clangd configuration in eclipse CDT 
- IEP-805: fix for Unable to install 2.7.0 nightly on eclipse 2022-09 CDT due to missing dependencies 

### Important Note:
During previous releases, esp_idf_components used to be generated as part of the project root and that got moved to project/build/ide/esp_idf_components, hence project/esp_idf_components folder needs to be deleted manually before the build for the first time.

### How to get the latest version?
Installing IDF Plugins using the update site URL:
https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/

Installing IDF Plugins using the local archive file:
https://dl.espressif.com/dl/idf-eclipse-plugin/updates/com.espressif.idf.update-2.7.0.zip

### Espressif IDE downloads
macosx aarch64 - https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-aarch64-v2.7.0.dmg

macosx - https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-x86_64-v2.7.0.dmg

Windows - https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-2.7.0-win32.win32.x86_64.zip

Linux - https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-2.7.0-linux.gtk.x86_64.tar.gz 

### Windows Users
If you're starting new, always prefer installing Espressif-IDE through the Windows Installer package from here https://dl.espressif.com/dl/esp-idf/ this will install ESP-IDF v4.4.2 and required toolchains along with the IDE

However, in the above link, you will find Espressif-IDE 2.6.0 with ESP-IDF 4.4.2 so after installing this you need to update the IDE to 2.7.0 through the update site (will soon update the Windows installer as well!)
