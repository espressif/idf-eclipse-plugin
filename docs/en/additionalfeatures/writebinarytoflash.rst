Write Binary to Flash
======================

Binary data can be written to the ESP’s flash chip via the *ESP-IDF > Write Binary Data to Flash* command. To access this command, right-click on the project in the Project Explorer:

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/186c8498-d779-4771-af53-e5bf09e29502
   :alt: Write Binary Data to Flash command

After clicking this command, the *Write Binary Data to Flash* dialog box will open. Editable default values are provided for the binary path and offset. You can check the correct offset by viewing the partition table via *ESP-IDF > Partition Table Editor* or by manually opening the `partitions.csv` file.

.. image:: https://github.com/espressif/idf-eclipse-plugin/assets/24419842/46e24e89-a1ed-4169-8c92-1ba0b0089ea7
   :alt: Write Binary Data to Flash dialog

Clicking the *Flash* button will execute the flash command, and the result will be displayed within this dialog.
