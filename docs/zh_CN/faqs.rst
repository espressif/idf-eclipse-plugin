常见问题
========

:link_to_translation:`en:[English]`

如何查看系统中已安装的 Java 版本？
------------------------------------

可以在终端使用 ``java -version`` 命令进行检查。

如何查看 Eclipse 使用的 Java 版本？
-------------------------------------

1. 前往 ``Help`` > ``About Eclipse`` > ``Installation Details`` > ``Configuration``。
2. 查找 ``-vm`` 参数。

使用 Eclipse IDE 时，如何增大 Java 的堆内存？
----------------------------------------------

1. 找到 ``eclipse.ini`` 或 ``espressif-ide.ini``。
2. 在 ``-vmargs`` 参数部分增大 Xmx 值。 例如，可以设置为 ``-Xmx2048m``。

该插件支持哪些操作系统？
------------------------

- Windows
- macOS
- Linux

如何提供 Eclipse 环境与插件信息？
---------------------------------

``Help`` > ``About Eclipse`` > ``Installation Details`` > ``Configuration`` > ``Copy to Clipboard``

如何查看已安装的 IDF Eclipse 插件版本？
----------------------------------------

1. 前往菜单栏 ``Eclipse`` > ``About Eclipse`` > ``Installation Details`` > ``Installed Software`` 查看。
2. 搜索 Espressif。

如何从 Eclipse 卸载 IDF Eclipse 插件？
--------------------------------------

1. 前往菜单栏 ``Eclipse`` > ``About Eclipse`` > ``Installation Details`` > ``Installed Software``。
2. 搜索 Espressif。
3. 选择 ``Espressif IDF Feature``。
4. 点击 ``Uninstall..``。

为什么在 Eclipse 中无法安装 IDF 插件？
---------------------------------------

请从主菜单检查错误日志:

1. 前往 ``Window`` > ``Show View`` > ``Other``。
2. 选择 ``General`` > ``Error Log``。

为什么在我的 Eclipse CDT 中看不到 Espressif 菜单选项和 Espressif IDF Project 菜单？
---------------------------------------------------------------------------------------

1. 请确保已安装 Java 8 及以上版本，并在 Eclipse 中切换到 C/C++ 布局模式。
2. 前往 ``Window`` > ``Perspective`` > ``Reset Perspective..`` 可重置布局模式。

IDF Eclipse 插件是否支持创建 CMake IDF 项目？
----------------------------------------------------------

可以通过 ``File`` > ``New`` > ``Espressif IDF Project`` 创建 IDF CMake 项目。

是否可以将现有的 IDF 项目导入 Eclipse？
----------------------------------------

可以通过导入菜单进行导入：``Import...`` > ``Espressif`` > ``Existing IDF Project``。

系统中 IDF 安装的工具位于哪里？
----------------------------------

Linux/MacOS 用户的默认目录为 ``$HOME/.espressif``，Windows 用户为 ``%USER_PROFILE%.espressif``。

为什么删除的 C/C++ 构建环境变量仍然显示？
------------------------------------------

你需要取消勾选首选项记录器。步骤如下：

1. 在 Eclipse 中前往 ``Preferences`` > ``Oomph`` > ``Setup Tasks`` > ``Preference Recorder``。
2. 取消勾选 ``Record into``。

如何回退到旧版 ESP-IDF Eclipse 插件？
-----------------------------------------

1. 打开 Eclipse IDE 并卸载 ESP-IDF 插件。
2. 重启 Eclipse IDE。
3. 从发布页面下载旧版 ESP Eclipse 插件。
4. 前往 ``Help`` > ``Install New Software``。
5. 点击 ``Add`` 按钮，会弹出名为 ``Add Repository`` 的窗口。
6. 点击 ``Archive`` 按钮并选择已下载的文件。
7. 继续安装。
8. 重启 Eclipse。

在项目中哪里可以找到 ``compile_commands.json`` 文件？
--------------------------------------------------------------

该文件存储在 ``/projectName/build/compile_commands.json``。

``compile_commands.json`` 以机器可读的格式记录了项目中所有翻译单元的完整编译器调用信息。Eclipse CDT 的索引器会使用该文件来解析代码并定位头文件。

如何访问 CDT 解析器的错误日志？
-----------------------------------

前往 ``Project`` > ``C/C++ Index`` > ``Create Parser Log``。

如何查看错误日志？
-------------------

在主菜单中选择 ``Window`` > ``Show View`` > ``Other``，然后选择 ``General`` > ``Error Log``。

如何报告死锁或 Eclipse 卡死？
-------------------------------------------

详细说明请参阅：https://wiki.eclipse.org/How_to_report_a_deadlock。

在命令行中，使用 ``jps -v`` 查找 Java 进程的 PID；使用 ``jstack <pid>`` 显示 Java 进程的堆栈跟踪。

此处 32308 和 8824 是 Java 进程的 PID。8824 是 jps 本身，与我们无关。根据其命令行中包含 ``org.eclipse.equinox.launcher`` 可以判断 32308 是一个 Eclipse 进程。``jstack`` 命令会将该 Eclipse 进程的堆栈跟踪保存到文件 ``/tmp/jstack.txt``，请将该文件附在问题报告中。

为什么会出现 ``sun.security.validator.ValidatorException: PKIX path building failed`` 报错？
--------------------------------------------------------------------------------------------

这通常是由错误的 Java 版本或缺失的 Java 证书导致。请确保已安装 **Java 11 或更高版本** 以修复该问题。详情请查看以下链接：

- https://esp32.com/viewtopic.php?f=13&t=12327&start=10#p50137
- https://stackoverflow.com/questions/6908948/java-sun-security-provider-certpath-suncertpathbuilderexception-unable-to-find

为何建议在 IDF Eclipse 插件中使用 Java 11？
---------------------------------------------

我们建议在使用 IDF Eclipse 插件时采用 Java 11 及以上版本（这是 Oracle 最新的 LTS 版本），因为 Eclipse 2020-06 要求使用 Java 11 才能运行 CDT。以下是来自 Eclipse 的一些重要提示：

- `在 Eclipse 2020-06 及之后版本上使用 Java 8 安装 CDT 9.11 时需要变通办法 <https://wiki.eclipse.org/CDT/User/NewIn911#Release>`_。

  运行 CDT 9.11 仅需 Java 8。不过，Eclipse 2020-06 及之后版本中的一个新功能可能会阻止安装向导执行安装操作。变通办法是在 ``Windows`` > ``Preferences`` > ``Install/Update`` 中关闭 "Verify provisioning operation is compatible with the currently running JRE"。参见 https://bugs.eclipse.org/bugs/show_bug.cgi?id=564407#c1。

- `CDT 10.0 需要 Java 11 或更高版本 <https://wiki.eclipse.org/CDT/User/NewIn100>`_。

  从 CDT 10.0 开始，运行 CDT 需要 Java 11 或更高版本。这与 Eclipse IDE 的要求一致，后者自 2020-09 起也需要 Java 11 才能运行。

如何在 Eclipse 中删除运行目标？
--------------------------------

Eclipse 中没有直接删除运行目标 (launch target) 的 UI 选项，不过可以参照以下步骤实现：

1. 前往 Eclipse 工作区目录。 例如，我的路径是 ``/Users/myName/myTesteclipseWorkspace``。
2. 在工作区目录中，进入 ``.metadata/.plugins/org.eclipse.core.runtime/.settings`` 文件夹。
3. 找到 ``org.eclipse.launchbar.core.prefs`` 文件并在编辑器中打开。
4. 搜索要删除的运行目标名称，并从文件中删除其对应的所有条目。
5. 保存文件。
6. 重启 Eclipse。

如何获取项目构建日志？
----------------------

1. 若想启用日志记录，请前往 ``Preferences`` > ``Project`` > ``C/C++`` > ``Build`` > ``Logging``。
2. 勾选 ``Enable global build logging``。
3. 构建项目。
4. 导出 ``global-build.log``。该日志与 CDT 构建控制台中显示的构建控制台日志相同，但构建控制台通常有 buffer 大小限制，因此不会显示全部内容。

如何为项目构建启用详细调试输出？
--------------------------------

IDF Eclipse 插件使用 CMake 命令来构建项目，因此可以通过构建配置向导传递 CMake 参数。配置方法如下：

1. 点击编辑器配置向导。
2. 切换到 ``Build Settings`` 选项卡。
3. 添加 ``--debug-output`` 或其他参数。

如何在 Espressif-IDE 中构建多个配置？
----------------------------------------

1. 创建一个新项目。
2. 打开 ``Launch Configuration`` 对话框。
3. 进入 ``Build Settings`` 选项卡，在 ``Additional CMake Arguments`` 中输入 ``-B build_release``。其中，``build_release`` 是构建文件夹的名称。
4. 点击 ``OK`` 按钮保存该配置。
5. 重新打开 ``Launch Configuration`` 对话框。
6. 点击左下角的 ``Duplicate`` 按钮。
7. 进入 ``Build Settings`` 选项卡，将 ``Additional CMake Arguments`` 更新为 ``-B build_dev``。其中，``build_dev`` 是构建文件夹的名称。
8. 点击 ``OK`` 按钮保存该配置。
9. 在工具栏中点击所选配置的 ``Build`` 图标（最左侧图标），这将为该配置构建项目并创建一个构建文件夹。然后在下拉菜单中选择另一配置，重复相同的步骤。

可以将我之前的 C/C++ 编辑器格式化文件 (.xml) 用作 ``.clang-format`` 文件吗？
----------------------------------------------------------------------------

不可以。不能在 ESP-IDF 项目中直接使用旧的 ``.xml`` （CDT 格式化工具）文件，因为目前使用的是 CDT LSP 编辑器，其代码格式化依赖 Clangd。Clangd 需要 ``.clang-format`` 文件，而且没有官方工具可以将 ``.xml`` 格式化文件转换为 Clang 格式。

不过，你可以参考 Clang 提供的若干默认的格式化风格（例如 LLVM、Google、Mozilla 等）。使用以下命令可以生成默认的 ``.clang-format`` 文件：

.. code-block:: none

    clang-format -style=llvm -dump-config > .clang-format

新建 ESP-IDF 项目时，根目录会自动创建带默认设置的 ``.clang-format`` 文件。Clangd 会自动识别该文件，无需额外配置。

对于现有项目，可以右键点击项目并选择 ``ESP-IDF`` > ``Create Clangd File``，手动创建 ``.clang-format`` 文件。

如果想复现旧的格式化设置，可以：

- 手动依据 Clang 的格式化指南，将 ``.xml`` 设置映射到 Clang 格式。
- 或者使用 AI 工具（例如 ChatGPT）将旧配置转换为新配置，然后再手动调整差异。

更多有关 Clang 格式的信息请参阅 `ClangFormat 文档 <https://clang.llvm.org/docs/ClangFormat.html>`_。

关于格式化风格，请参阅 `Clang-Format 风格选项 <https://clang.llvm.org/docs/ClangFormatStyleOptions.html>`_。
