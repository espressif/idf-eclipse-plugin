NVS Table Editor
================

The NVS Table Editor helps create a binary file based on key-value pairs provided in a CSV file. The resulting binary file is compatible with the NVS architecture as defined in `ESP-IDF Non Volatile Storage <https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/storage/nvs_flash.html>`_. The expected CSV format is:

.. code-block:: text

   key,type,encoding,value      <-- column header (must be the first line)
   namespace_name,namespace,,   <-- First entry must be of type "namespace"
   key1,data,u8,1
   key2,file,string,/path/to/file

.. note:: This is based on ESP-IDF `NVS Partition Generator Utility <https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-reference/storage/nvs_partition_gen.html>`_.

Steps
-----

1. Right-click on a project in the *Project Explorer*.
2. Click on the *ESP-IDF > NVS Table Editor* menu option:

   .. image:: https://user-images.githubusercontent.com/24419842/216114697-9f231211-f5dd-431b-9432-93ecc656cfec.png
      :alt: NVS Table Editor menu option

3. Make desired changes to the CSV data.
4. Save changes by clicking the *Save* button. If everything is correct, you will see an information message at the top of the dialog:

   .. image:: https://user-images.githubusercontent.com/24419842/216115906-9bb4fe55-293b-4c6b-8d22-0aa3520581ab.png
      :alt: Save confirmation in NVS Table Editor

5. Generate the partition binary (choose *Encrypt* to encrypt the binary, and disable the *Generate Key* option to use your own key if desired). You will see an information message at the top of the dialog about the result of the generated binaries. Hover over the message if it's too long to read fully:

   .. image:: https://user-images.githubusercontent.com/24419842/216117261-9bee798a-3a9e-4be5-9466-fc9d3847834b.png
      :alt: Binary generation result in NVS Table Editor

 .. note:: If there are any errors, they will be highlighted. Hover over the error icon to read more about the error. You will also see an error message at the top of
