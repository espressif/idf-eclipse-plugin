安装
====

:link_to_translation:`en:[English]`

配置和安装 Espressif-IDE 包括三个主要步骤：

1. :ref:`下载并安装 Espressif-IDE <esp-idf-installation>`
2. :ref:`在 Espressif-IDE 中安装 ESP-IDF 及相关工具 <esp-idf-tools-installation>`
3. :ref:`将旧工作区迁移到新工作区 <esp-idf-tools-migration>`

.. note::

    对于喜欢通过 `更新站点 <https://dl.espressif.com/dl/idf-eclipse-plugin/updates/latest/>`_ 安装 ESP-IDF Eclipse 插件的 Eclipse CDT 用户，请参考 :ref:`更新站点安装指南 <marketplaceupdate>`。

.. _esp-idf-installation:

安装 Espressif-IDE 
------------------

首先，请根据你的操作系统查看下方对应的章节，并参照说明安装 Espressif-IDE。

请注意，Espressif-IDE 要求通过乐鑫安装管理器 (Espressif Installation Manager, EIM) 安装 ESP-IDF，从而管理 ESP-IDF 版本和所需工具。有关使用 EIM 的详细文档，请参考 `官方指南 <https://docs.espressif.com/projects/idf-im-ui/en/latest/>`_。

Windows
~~~~~~~

.. note::

    对于 Windows 用户，Espressif-IDE 提供带有 ESP-IDF 的离线安装包。

macOS/Linux
~~~~~~~~~~~

找到 :ref:`下载章节 <downloads>` 中适用于你操作系统的 Espressif-IDE。下载完成后，启动 IDE 并继续 :ref:`安装所需的 ESP-IDF 及相关工具 <esp-idf-tools-installation>`。请确保在启动 IDE 之前已安装 Java、Python 和 Git，并且其在系统路径中可用。

.. _esp-idf-tools-installation:

安装 ESP-IDF 及相关工具（新工作区）
-----------------------------------

Espressif-IDE 提供了工具管理视图，可用来激活可用的 ESP-IDF 版本并管理相关工具。若要安装 ESP-IDF 及其工具，首先点击 `此链接 <https://dl.espressif.com/dl/eim/>`__ 下载 EIM。

下载并启动 EIM 后，按照屏幕提示安装所需的 ESP-IDF 版本及相关工具。安装完成后，你可以在 IDE 内使用 ESP-IDF 管理器激活工作区中的 ESP-IDF 版本。你可以直接从 IDE 下载并启动 EIM，便于管理 ESP-IDF 版本和工具。如果你在旧版本的 Espressif-IDE 中打开工作区，系统会提示你将旧版本的配置转换为与 EIM 兼容的格式。

参照下列步骤，在 Espressif-IDE 中安装 ESP-IDF 及相关工具：

1.  前往 ``Espressif`` > ``ESP-IDF Manager``，将打开如下编辑器：

    .. image:: ../../media/ToolsManager/ESP-IDF_Manager_Editor_Screen.png

2.  如果尚未下载和安装 EIM，请点击 ``Launch EIM`` 或 ``Download & Launch EIM`` 按钮。

    你可以在控制台查看下载和安装进度。EIM 启动后，可以用来安装 ESP-IDF 及相关工具。

3.  关闭 EIM 后，ESP-IDF 管理器编辑器将更新可用的 ESP-IDF 版本及工具的最新信息。

    .. image:: ../../media/ToolsManager/Tool_installed_and_activated.png

.. note::

    注意，早期版本的 Espressif-IDE 在内部管理工具，但现在对 ESP-IDF 的所有访问都由 EIM 统一处理。

你可以通过 EIM 添加任意数量的 ESP-IDF 版本，但只能激活一个版本，并将其用于编译和索引工作区中的项目。使用此功能，你可以在工作区中轻松切换 IDF 版本。

只需点击 ``Active`` 列旁的单选按钮即可激活特定版本。

最后一列的刷新按钮可用于重新加载 ESP-IDF 目录中的更改。

.. note::

    对 ESP-IDF 目录所做的任何手动更改，只有点击刷新按钮后，IDE 才会更新激活版本并显示最新内容。但如果是通过 EIM 修改了 ESP-IDF 的安装，IDE 会自动检测到，并弹出提示要求更新 ESP-IDF 版本。

.. image:: ../../media/ToolsManager/ESP-IDF_Manager_Changed_Installation_Message.png

.. _esp-idf-tools-migration:

迁移旧工作区到新工作区
----------------------

如果你从旧版本的 Espressif-IDE 进行迁移，需要转换现有工作区，使其兼容新的 ESP-IDF 管理器。点击 `此链接 <https://dl.espressif.com/dl/eim/>`__，下载并安装 EIM。安装 EIM 后，请按照以下步骤操作：

1.  将下载的 EIM 可执行文件放置到适合你操作系统的位置：

    - **Windows**: `C:\Users\<username>\.espressif\eim_gui\eim.exe`
    - **Linux**: `~/.espressif/eim_gui/eim`
    - **macOS**: 将 ``eim.app`` 应用包复制到 ``Applications`` 文件夹，例如 `/Applications/eim.app`.

2.  打开 Espressif-IDE。如果 IDE 已运行，请重启。
3.  IDE 检测到 EIM 可执行文件和有效的旧工作区后，会提示你将旧工作区转换为新格式：

    .. image:: ../../media/ToolsManager/ESP-IDF_Manager_Conversion.png

4.  点击 ``Yes`` 按钮转换旧配置。
5.  在 Espressif-IDE 中，前往 ``Espressif`` > ``ESP-IDF Manager``。在打开的 ESP-IDF 管理器编辑器中选择并激活所需版本。

参考文档
--------

.. toctree::
    :maxdepth: 1

    更新站点安装指南 <marketplaceupdate>
    配置 CDT 构建环境变量 <additionalfeatures/configureenvvariables>
