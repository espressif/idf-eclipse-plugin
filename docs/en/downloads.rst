.. _downloads:

Espressif-IDE Downloads
=======================

:link_to_translation:`zh_CN:[中文]`

You can find the latest Espressif-IDE release notes at `here <https://github.com/espressif/idf-eclipse-plugin/releases>`_. Direct download links for various platforms are provided below.

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - OS
     - Download
   * - Windows
     - `Espressif-IDE-win32.win32.x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-win32.win32.x86_64/latest>`_
   * - macOS x86_64
     - `Espressif-IDE-macosx-cocoa-x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-x86_64/latest>`_
   * - macOS aarch64
     - `Espressif-IDE-macosx-cocoa-aarch64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-aarch64/latest>`_
   * - Linux
     - `Espressif-IDE-linux.gtk.x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-linux.gtk.x86_64/latest>`_


macOS Security Notice (Applicable Only for Nightly Builds)
----------------------------------------------------------

On macOS, if you download the archive using a web browser, the strict security checks in recent macOS versions will prevent it from running and display a message that the program is damaged. This is not actually true, and the fix is simple: you just need to remove the ``com.apple.quarantine`` extended attribute.

.. code-block:: shell

    $ xattr -d com.apple.quarantine ~/Downloads/Espressif-IDE-x.x.x-macosx.cocoa.x86_64.tar.gz

After unpacking the archive, if the application still shows the same warning, check and remove the attribute from the ``Espressif-IDE.app`` folder as well:

.. code-block:: shell

    $ xattr -dr com.apple.quarantine ~/Downloads/Espressif-IDE.app
