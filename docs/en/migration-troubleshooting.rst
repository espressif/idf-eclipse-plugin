Migration Troubleshooting
=========================

:link_to_translation:`zh_CN:[中文]`

Use this table when a step in the :doc:`migration-walkthrough` does not behave the same way it did in Espressif-IDE. IDE build-error hints remain in :doc:`troubleshooting`.

.. list-table::
   :header-rows: 1
   :widths: 32 34 34

   * - Symptom
     - Likely cause
     - Recommended action
   * - SDK Configuration Editor asks for ``fullclean``
     - Existing CMake cache references another Python/environment
     - Run **ESP-IDF: Full Clean**, then reopen the editor.
   * - JTAG Flash reports an FTDI/OpenOCD device error
     - Board/OpenOCD setup was not restored
     - Re-select target + exact board/device configuration.
   * - Target appears correct but JTAG still fails
     - Chip target and board/OpenOCD state are separate
     - Explicitly select the board/device again.
   * - Debug reports ``.vscode/launch.json`` is missing
     - Existing project has no VS Code debug config
     - Run **ESP-IDF: Add VS Code Configuration Folder**.
   * - Default Debug works but behavior differs from Eclipse
     - Custom Eclipse ``.launch`` values were not converted
     - Review and migrate/recreate the required custom debug settings.
   * - VS Code creates ``project/build`` instead of the old build path
     - Eclipse custom build path was not imported
     - Configure the custom build directory manually. See :doc:`migration-advanced`.
   * - Doctor reports unrelated configuration errors
     - Stale user/global extension settings
     - Check whether the reported setting is relevant to the current project.
