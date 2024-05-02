# Install script for directory: D:/work/esp-idf-v5.1.1

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "C:/Program Files (x86)/bootloader")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Is this installation the result of a crosscompile?
if(NOT DEFINED CMAKE_CROSSCOMPILING)
  set(CMAKE_CROSSCOMPILING "TRUE")
endif()

# Set default install directory permissions.
if(NOT DEFINED CMAKE_OBJDUMP)
  set(CMAKE_OBJDUMP "C:/Users/Denys/.espressif/tools/xtensa-esp32s2-elf/esp-12.2.0_20230208/xtensa-esp32s2-elf/bin/xtensa-esp32s2-elf-objdump.exe")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/xtensa/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/newlib/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/soc/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/micro-ecc/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/hal/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/spi_flash/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/esp_app_format/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/bootloader_support/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/efuse/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/esp_system/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/esp_hw_support/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/esp_common/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/esp_rom/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/log/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/esptool_py/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/partition_table/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/bootloader/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/freertos/cmake_install.cmake")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for the subdirectory.
  include("D:/ECLIPSE-IED-4-09/idf-eclipse-plugin/tests/com.espressif.idf.ui.test/ws/NewProjectTestDFU/build/bootloader/esp-idf/main/cmake_install.cmake")
endif()

