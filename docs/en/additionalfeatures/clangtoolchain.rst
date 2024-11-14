Configuring Clang Toolchain
===========================

1. After creating a new project, edit the project configuration.
   
   .. image:: https://user-images.githubusercontent.com/24419842/194882285-9faadb5d-0fe2-4012-bb6e-bc23dedbdbd2.png
      :alt: Project Configuration

2. Go to the ``Build Settings`` tab and select the clang toolchain:
   
   .. image:: https://user-images.githubusercontent.com/24419842/194882462-3c0fd660-b223-4caf-964d-58224d91b518.png
      :alt: Clang Toolchain Selection

.. note:: 
   Clang toolchain is currently an experimental feature, and you may encounter build issues due to incompatibility with ESP-IDF. The following describes how to address the most common build issues on the current ESP-IDF master (ESP-IDF v5.1-dev-992-gaf28c1fa21-dirty). To work around clang build errors, please refer to `this workaround guide <https://github.com/espressif/idf-eclipse-plugin/blob/master/WORKAROUNDS.md#clang-toolchain-buid-errors>`_.
