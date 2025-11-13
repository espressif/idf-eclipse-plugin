.. _windowsofflineinstaller:

Installing Espressif-IDE by Windows Offline Installer
======================================================

:link_to_translation:`zh_CN:[中文]`

`Espressif-IDE with ESP-IDF Windows Offline Installer` is an offline installer that includes all the required components for ESP-IDF application development.

The installer deploys the following components:

- Embedded Python
- Cross-compilers
- OpenOCD
- CMake and Ninja build tools
- ESP-IDF
- Espressif-IDE
- Amazon Corretto OpenJDK

The installer bundles all the required components and tools, including a stable version of ESP-IDF, so that people working behind corporate firewalls can use the entire solution out of the box. This also configures all the required build environment variables and tool paths as you launch the IDE. All you need to do is start working on your project directly, without manually configuring anything. This will greatly boost your productivity!

Download
--------

You can download the latest version of the installer from `this link <https://dl.espressif.com/dl/esp-idf/>`_ and run the installer. The installer name may look like ``Espressif-IDE-3.1.1 with ESP-IDF v5.3.1`` or something similar, depending on the version of the IDE and ESP-IDF.

Choose the installer as shown below.

.. image:: ../../media/windows-installation/ide_windows_installer_0.png

Installation
------------

The installer is an executable file with ``.exe`` extension. You can run the installer by double-clicking on it.

The installer will guide you through the installation process. Please find the step-by-step guide below.

Step 1: Choose Language
~~~~~~~~~~~~~~~~~~~~~~~

Select language for the installer and click ``OK``.

.. image:: ../../media/windows-installation/ide_windows_installer_1.png
   :alt: drawing

Step 2: Accept the Product License Agreement
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Read through the product license agreement, and then select ``I accept the agreement``. You must accept the product license in order to continue with the installation. Continue by clicking ``Next``.

.. image:: ../../media/windows-installation/ide_windows_installer_2.png
   :alt: drawing

Step 3: Pre-Installation Checks
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. image:: ../../media/windows-installation/ide_windows_installer_3.png
   :alt: drawing

Step 4: Choose the Installation Directory
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. image:: ../../media/windows-installation/ide_windows_installer_4.png
   :alt: drawing

Step 5: Select Components to Install
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

By default, all components are selected. You can deselect any component that you don’t want to install.

.. image:: ../../media/windows-installation/ide_windows_installer_5.png
   :alt: drawing

Step 6: Review the Installation Summary
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

A summary for review is displayed before Espressif-IDE and other components are installed.

.. image:: ../../media/windows-installation/ide_windows_installer_6.png
   :alt: drawing
.. image:: ../../media/windows-installation/ide_windows_installer_7.png
   :alt: drawing

Step 7: Finalize the Installation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

.. image:: ../../media/windows-installation/ide_windows_installer_8.png
   :alt: drawing
.. image:: ../../media/windows-installation/ide_windows_installer_9.png
   :alt: drawing

Step 8: Launch Espressif-IDE
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Launch Espressif-IDE by double-clicking on the icon.

.. image:: ../../media/windows-installation/ide_windows_installer_10.png
   :alt: drawing

Step 9: Choose Espressif-IDE Workspace
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

It's advised to select a workspace directory outside the Espressif-IDE installer folder.

.. image:: ../../media/windows-installation/ide_windows_installer_11.png
   :alt: drawing

Step 10: Espressif-IDE Workbench
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

As soon as you launch the Espressif-IDE, it will automatically configure the required environment variables and launch the Welcome page. You can close the Welcome page; there is no need to run any additional installation tools from the IDE.

.. image:: ../../media/windows-installation/ide_windows_installer_12.png
   :alt: drawing

You can verify the CDT Build environment variables from the Eclipse ``Preferences``.

.. image:: ../../media/windows-installation/ide_windows_installer_13.png
   :alt: drawing

Step 11: Build Your First Project
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Since the IDE is already configured with all the required environment variables, you can start your project.

You can find more details for creating a project in the :ref:`Start a Project <startproject>` section of the documentation.

.. image:: ../../media/windows-installation/ide_windows_installer_14.png
   :alt: drawing
   :width: 400
