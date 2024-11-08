.. _marketplaceupdate:

Update Site Installation Guide
======================================

.. toctree::
    :maxdepth: 2
    :caption: Contents:

    installUpdateSiteURL
    install_idf_eclipse_plugin_marketplace
    install_idf_eclipse_plugin_local_archive
    upgradePlugins


.. _installUpdateSiteURL:

Installing IDF Plugin using update site URL
--------------------------------------------
You can install the IDF Eclipse plugin into an existing IDE using the update site URL. First, add the release repository URL as follows:

1. Go to `Help` > `Install New Software`.
2. Click `Add…`, and in the pop-up window:
    * Enter `Name` as `Espressif IDF Plugin for Eclipse`.
    * Enter `Location` of the repository
        * For the stable release: `<https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/>`_.
    * Click `Add`.
3. Select `Espressif IDF` from the list and proceed with the installation.

For adding beta and nightly builds, you can use the following update site URLs:
   * For Beta version: `<https://dl.espressif.com/dl/idf-eclipse-plugin/updates/beta/>`_.
   * For Nightly build: `<https://dl.espressif.com/dl/idf-eclipse-plugin/updates/nightly/>`_.

> **Note:** While the screenshots are captured on macOS, the installation instructions are applicable to Windows, Linux, and macOS.

.. image:: ../../media/idf_update_site_install.png


.. _install_idf_eclipse_plugin_marketplace:

Installing IDF Eclipse Plugin from Eclipse Marketplace
------------------------------------------------------

To install the ESP-IDF Eclipse Plugin from the Eclipse Marketplace, follow these steps:

1. Open Eclipse and go to `Help` > `Eclipse Marketplace...`.

2. In the search box, enter **ESP-IDF Eclipse Plugin** to locate the plugin.

3. Click **Install** and follow the on-screen instructions to complete the installation.

4. After installation, restart Eclipse to activate the plugin.


.. _install_idf_eclipse_plugin_local_archive:

Installing IDF Eclipse Plugin from Local Archive
------------------------------------------------------

To install the ESP-IDF Eclipse Plugin from a local archive, follow these steps:

1. Download the latest update site archive for the IDF Eclipse Plugin from [here](https://github.com/espressif/idf-eclipse-plugin/releases).

2. In Eclipse, go to `Help` > `Install New Software`.

3. Click the `Add…` button.

4. In the **Add Repository** dialog, select **Archive** and choose the file `com.espressif.idf.update-vxxxxxxx.zip`.

5. Click **Add**.

6. Select **Espressif IDF** from the list and proceed with the installation.

7. Restart Eclipse after the installation is complete.


.. _upgradePlugins:

How do I upgrade my existing IDF Eclipse Plugin?
------------------------------------------------------
If you are installing the IDF Eclipse Plugin for the first time, follow these steps to add the repository for the new release:

1. Go to `Window` > `Preferences` > `Install/Update` > `Available Software Sites`.
2. Click `Add`.
3. Enter the URL of the new repository: `https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/`.
4. Click `Ok`.

If you have already installed the IDF Eclipse Plugin using the update site URL, you can upgrade to the latest version with the following steps:

1. Go to `Help` > `Check for Updates`.
2. If updates are found, select `Espressif IDF Plugins for Eclipse` and deselect all other items.
3. Click `Next` to proceed with the installation.

.. image:: ../../media/Update_plugins.png
