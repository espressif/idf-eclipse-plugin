.. _downloads:

下载 Espressif-IDE 
===================

:link_to_translation:`en:[English]`

点击 `此处 <https://github.com/espressif/idf-eclipse-plugin/releases>`_ 可查看最新的 Espressif-IDE 发行说明。下面提供了各平台的直接下载链接。

.. list-table::
   :header-rows: 1
   :widths: 20 80

   * - 操作系统
     - 下载链接
   * - Windows
     - `Espressif-IDE-win32.win32.x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-win32.win32.x86_64/latest>`_
   * - macOS x86_64
     - `Espressif-IDE-macosx-cocoa-x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-x86_64/latest>`_
   * - macOS aarch64
     - `Espressif-IDE-macosx-cocoa-aarch64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-macosx-cocoa-aarch64/latest>`_
   * - Linux
     - `Espressif-IDE-linux.gtk.x86_64 <https://dl.espressif.com/dl/idf-eclipse-plugin/ide/Espressif-IDE-linux.gtk.x86_64/latest>`_


macOS 安全注意事项（仅适用于每日自动构建的测试版本）
----------------------------------------------------

在 macOS 上，如果通过浏览器下载压缩包，则新版的 macOS 会严格进行安全检查，阻止其运行并提示程序已损坏。该提示并不属实，解决方法很简单：只需移除 ``com.apple.quarantine`` 扩展属性即可。

.. code-block:: shell

    $ xattr -d com.apple.quarantine ~/Downloads/Espressif-IDE-x.x.x-macosx.cocoa.x86_64.tar.gz

解压缩文件后，如果应用程序仍然出现相同的警告，请检查 ``Espressif-IDE.app`` 文件夹并删除该属性：

.. code-block:: shell

    $ xattr -dr com.apple.quarantine ~/Downloads/Espressif-IDE.app
