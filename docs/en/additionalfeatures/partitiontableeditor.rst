Partition Table Editor UI for ESP-IDF
=====================================

The Partition Table Editor command allows you to edit your `partition table <https://docs.espressif.com/projects/esp-idf/en/latest/esp32/api-guides/partition-tables.html>`_ in a more convenient way, where you can see the supported types and subtypes and monitor the correctness of the entered data.

Steps
-----

1. Go to *Project Explorer* and open any IDF Project where you want to create a customized partition table.
2. In *Project Explorer*, right-click on the project and select *ESP-IDF > Partition Table Editor*:

   .. image:: https://user-images.githubusercontent.com/24419842/216105408-ca2e73ce-5df3-4bdd-ac61-b7265deb9b44.png
      :alt: Partition Table Editor menu option

   When you open the partition table editor for the selected project, you will see the standard editable content. Errors, if any, will be highlighted. You can hover your mouse over them to get hints about the issues:

   .. image:: https://user-images.githubusercontent.com/24419842/216106804-703b2eb4-b141-48de-8559-0599f072219f.png
      :alt: Error hints in Partition Table Editor

3. Click *Save* or *Save and Quit* to save your changes.

To Use a Customized Partition Table
-----------------------------------

1. Go to *sdkconfig* and set *Custom partition table CSV* as shown below:

   .. image:: https://user-images.githubusercontent.com/24419842/216104107-2844068b-8412-468b-931f-b4778af4417c.png
      :alt: Setting custom partition table in sdkconfig
