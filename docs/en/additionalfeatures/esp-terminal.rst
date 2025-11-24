ESP-IDF Terminal
================

:link_to_translation:`zh_CN:[中文]`

This launches a local terminal with all environment variables configured under ``Preferences`` > ``C/C++`` > ``Build`` > ``Environment``. The default working directory would be either the currently selected project or ``IDF_PATH`` if no project selected.

The terminal ``PATH`` is also configured with ``esptool``, ``espcoredump``, ``partition_table``, and ``app_update`` component paths, so it is convenient to access them directly from the ESP-IDF terminal.

To launch the ESP-IDF Terminal:

1. Click on the ``Open a Terminal`` icon from the toolbar.
2. Choose ``ESP-IDF Terminal`` from the terminal drop-down and click ``OK`` to launch a terminal.

.. image:: ../../../media/idf_terminal.png
   :alt: ESP-IDF Terminal
