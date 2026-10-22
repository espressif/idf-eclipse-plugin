Final migration checklist
=========================

:link_to_translation:`zh_CN:[中文]`

Before considering the migration complete:

- Project opens and builds with the expected ESP-IDF version and target.
- Menuconfig opens and existing configuration values are preserved.
- Board/OpenOCD, flash method, and serial port are correct for the workflows you use.
- Flash / Monitor work if they are part of your workflow.
- Custom build directory is restored if required.
- Debug starts successfully and required custom Eclipse debug settings have been reviewed.

Related resources
-----------------

- :doc:`migrate-to-vscode`
- :doc:`support-policy`
- `ESP-IDF VS Code Extension <https://docs.espressif.com/projects/vscode-esp-idf-extension/en/latest/>`_
- `ESP-IDF Programming Guide <https://docs.espressif.com/projects/esp-idf/en/latest/>`_
