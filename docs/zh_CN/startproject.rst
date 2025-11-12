.. _startproject:

开始项目
========

:link_to_translation:`en:[English]`

要开始使用 Espressif-IDE，可以创建一个新项目或导入现有项目。

- :ref:`创建新项目 <newproject>`
- :ref:`使用 ESP-IDF 模板创建新项目 <newprojecttemplate>`
- :ref:`导入现有项目 <importproject>`

..  _newproject: 

创建新项目
----------

要在 Espressif-IDE 中创建新项目，请按以下步骤操作：

#.  依此选择 ``File`` > ``New`` > ``Espressif IDF Project``。

    .. image:: ../../media/newproject_menu.png
       :alt: 新项目菜单栏

#.  填写 ``Project name``。
#.  点击 ``Finish``。

.. Note::

    ESP-IDF 构建系统不支持在 ESP-IDF 或项目的路径中使用空格。


.. _newprojecttemplate:

使用 ESP-IDF 模板创建新项目
-----------------------------

Espressif-IDE 还提供使用 ESP-IDF 模板创建项目的功能，按以下步骤操作：

#. 依次选择 ``File`` > ``New`` > ``Espressif IDF Project``。
#. 在 ``Select Project Target`` 下拉列表中选择目标开发板。
#. 在 ``Template Selection`` 部分勾选 ``Create a project using one of the templates``。
#. 选择要使用的模板，项目名称会根据所选模板自动填写。
#. 点击 ``Finish``。

.. image:: ../../media/3_new_project_default.png

.. note::

    你可能会在编辑器中看到大量未解析的头文件或符号错误，这些错误会在构建完成后被解析。


.. _importproject:

导入现有项目
------------

要将现有项目导入到 Espressif-IDE，请确保它是一个 CMake 项目。按以下步骤操作：

#. 右键点击 ``Project Explorer``。
#. 选择 ``Import..`` 菜单。
#. 从 ``Espressif`` 导入向导菜单列表中选择 ``Existing IDF Project``。
#. 点击 ``Next``。
#. 点击 ``Browse...`` 选择现有项目所在目录。
#. 如果希望使用不同的名称，请填写 ``Project name``。
#. 点击 ``Finish``，将所选项目作为 CMake 项目导入到 Eclipse 工作区。

.. image:: ../../media/5_import_project.png
