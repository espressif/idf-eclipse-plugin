.. _downloads:

Espressif-IDE Downloads
=========================

You can find the latest Espressif-IDE release notes from `here <https://github.com/espressif/idf-eclipse-plugin/releases>`_. Provided below are the direct download links for various platforms.

+--------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| OS           | Download                                                                                                                                  |
+==============+===========================================================================================================================================+
| Windows      | `Espressif-IDE-win32.win32.x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-win32.win32.x86_64/latest>`_          |
+--------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| macOS x86_64 | `Espressif-IDE-macosx-cocoa-x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-x86_64/latest>`_        |
+--------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| macOS aarch64| `Espressif-IDE-macosx-cocoa-aarch64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-aarch64/latest>`_      |
+--------------+-------------------------------------------------------------------------------------------------------------------------------------------+
| Linux        | `Espressif-IDE-linux.gtk.x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-linux.gtk.x86_64/latest>`_              |
+--------------+-------------------------------------------------------------------------------------------------------------------------------------------+


macOS security notice (Applicable only for Nightly Builds)
------------------------------------------------------------
On macOS, if you download the archive with the browser, the strict security checks on recent macOS will prevent it to run, and complain that the program is damaged. That’s obviously not true, and the fix is simple, you need to remove the `com.apple.quarantine` extended attribute.

.. code-block:: shell

    $ xattr -d com.apple.quarantine ~/Downloads/Espressif-IDE-x.x.x-macosx.cocoa.x86_64.tar.gz

After un-archiving, if the application still complains, check/remove the attribute from the Espressif-IDE.app folder too:

.. code-block:: shell

    $ xattr -dr com.apple.quarantine ~/Downloads/Espressif-IDE.app