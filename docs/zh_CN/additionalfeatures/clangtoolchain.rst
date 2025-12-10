配置 Clang 工具链
=================

:link_to_translation:`en:[English]`

1. 创建新项目后，编辑项目配置。
   
   .. image:: https://user-images.githubusercontent.com/24419842/194882285-9faadb5d-0fe2-4012-bb6e-bc23dedbdbd2.png
      :alt: 项目配置

2. 前往 ``Build Settings`` 选项卡并选择 Clang 工具链。
   
   .. image:: https://user-images.githubusercontent.com/24419842/194882462-3c0fd660-b223-4caf-964d-58224d91b518.png
      :alt: 选择 Clang 工具链

.. note:: 

   Clang 工具链目前处于实验阶段，使用时可能会因为与 ESP-IDF 不兼容而遇到构建问题。以下内容说明了如何解决当前 ESP-IDF master 分支 (ESP-IDF v5.1-dev-992-gaf28c1fa21-dirty) 上最常见的构建问题。若遇到 Clang 构建错误，请参考此 `变通方法指南 <https://github.com/espressif/idf-eclipse-plugin/blob/master/WORKAROUNDS_CN.md>`_。
