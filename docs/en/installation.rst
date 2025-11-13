Installation
============

:link_to_translation:`zh_CN:[中文]`

Configuring and installing the Espressif-IDE involves three main steps:

1. :ref:`Download and Installing the Espressif-IDE <esp-idf-installation>`
2. :ref:`Install the ESP-IDF and related tools within the Espressif-IDE <esp-idf-tools-installation>`
3. :ref:`Migrate old workspace to new <esp-idf-tools-migration>`

.. note::

    For Eclipse CDT users who prefer installing the ESP-IDF Eclipse Plugin via the `update site <https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/>`_, please refer to the :ref:`Update Site Installation Guide <marketplaceupdate>`. 

.. _esp-idf-installation:

Espressif-IDE Installation
--------------------------

To begin, navigate to the section below corresponding to your operating system, and follow the provided instructions to install the Espressif-IDE.

Please note that the Espressif-IDE requires that you install ESP-IDF via Espressif Installation Manager (EIM) to manage the ESP-IDF versions and tools. For detailed documentation on using the EIM, please refer to the `official guide <https://docs.espressif.com/projects/idf-im-ui/en/latest/>`_.

Windows
~~~~~~~~

.. note::

    For Windows users, Espressif-IDE is available with ESP-IDF as an offline installer.

macOS/Linux
~~~~~~~~~~~~

Download the Espressif-IDE for your respective operating system from the :ref:`download section <downloads>`. After downloading, launch the IDE and proceed to :ref:`install the necessary ESP-IDF and related tools <esp-idf-tools-installation>`. Please ensure that Java, Python, and Git are installed as prerequisites and are available in the system path before launching the IDE.

.. _esp-idf-tools-installation:

ESP-IDF and Tools Installation (New Workspace)
----------------------------------------------

The Espressif-IDE provides a tool manager view that allows you to activate available ESP-IDF versions and manage related tools. To install the ESP-IDF and its tools, first download the EIM from the following `link <https://dl.espressif.com/dl/eim/>`__.

After downloading and launching EIM, follow the on-screen instructions to install the required ESP-IDF version and associated tools. Once the installation is complete, you can use the ESP-IDF Manager within the IDE to activate the installed ESP-IDF version in the workspace. The Espressif-IDE allows you to download and launch the EIM directly from the IDE, making it easier to manage your ESP-IDF versions and tools. When you open a workspace created with an old version of Espressif-IDE, the IDE will prompt you to convert its configuration to be compatible with EIM.

Follow these steps inside the Espressif-IDE to install the ESP-IDF and related tools:

1.  Go to ``Espressif`` > ``ESP-IDF Manager``. The following editor will open:

    .. image:: ../../media/ToolsManager/ESP-IDF_Manager_Editor_Screen.png

2.  Click the ``Launch EIM`` or ``Download & Launch EIM`` button if you have not downloaded and installed EIM.

    You will see the progress of download and installation in the console. Once the EIM is launched, you can use it to install the ESP-IDF and related tools.

3.  Once you close the EIM, your ESP-IDF Manager editor will be updated with the latest information about the ESP-IDF versions and tools available.

    .. image:: ../../media/ToolsManager/Tool_installed_and_activated.png

.. note::

    Please note that the EIM will be the only tool going forward to manage the ESP-IDF versions and tools. Previous versions of the Espressif-IDE managed tools internally, but now all access to ESP-IDF is handled through the EIM.

You can add as many versions of ESP-IDF as you want via EIM, but only one version can be set as active, and that version will be used to compile and index projects in your workspace. This feature helps you switch between versions in the workspace easily.

To activate any specific version, simply click the radio button next to it in the ``Active`` column.

The refresh button in the last column for the active ESP-IDF version can be used to reload any changes in the ESP-IDF directory you made.

.. note::

    Any manual changes made to the ESP-IDF directory will not be reflected in the IDE until you refresh the active ESP-IDF version using the refresh button. However, if you made any changes to the installation of ESP-IDF using the EIM, those changes will be detected and a popup message will be shown to you to update the ESP-IDF version in the IDE.

.. image:: ../../media/ToolsManager/ESP-IDF_Manager_Changed_Installation_Message.png

.. _esp-idf-tools-migration:

Old-to-New Workspace Migration
------------------------------

If you are migrating from an old version of the Espressif-IDE, you will need to convert your existing workspace to be compatible with the new ESP-IDF Manager. You will need to download and install the EIM from the following `link <https://dl.espressif.com/dl/eim/>`__. After installation of the EIM, please follow the steps below:

1.  Place the downloaded EIM executable in the appropriate location according to your operating system:

    - **Windows**: `C:\Users\<username>\.espressif\eim_gui\eim.exe`
    - **Linux**: `~/.espressif/eim_gui/eim`
    - **macOS**: Copy the ``eim.app`` bundle to your ``Applications`` folder, for example `/Applications/eim.app`.

2.  Open the Espressif-IDE. If it is already running, restart the IDE.
3.  After the IDE detects the EIM executable and a valid old workspace, it will prompt you to convert the old workspace to the new format.

    .. image:: ../../media/ToolsManager/ESP-IDF_Manager_Conversion.png

4.  Click the ``Yes`` button to convert the old configuration.
5.  Now in Espressif-IDE, go to ``Espressif`` > ``ESP-IDF Manager``. The ESP-IDF Manager editor will open, and you can proceed to select and activate the ESP-IDF version you want.

References
----------

.. toctree::
    :maxdepth: 1

    Update Site Installation Guide <marketplaceupdate>
    Configure CDT Build Environment Variables <additionalfeatures/configureenvvariables>
