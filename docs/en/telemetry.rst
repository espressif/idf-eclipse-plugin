.. _telemetry:

Telemetry
=========

:link_to_translation:`zh_CN:[中文]`

Espressif-IDE collects pseudonymous usage statistics to understand how many machines are active and how quickly new
releases are adopted. These numbers help prioritize features, plan releases and decide which platforms to support.

What is collected
-----------------

At most once a day, the IDE sends a small event containing:

- a persistent machine identifier: SHA-256 of the first valid network adapter MAC address when one is available,
  otherwise a random UUID stored under the current user's home directory. The original MAC address is never sent or stored
- the Espressif-IDE and IDF plugin versions
- the Eclipse platform version and the identifier of the running product
- whether the plugin runs inside Espressif-IDE or in a plain Eclipse installation
- the operating system name, version and architecture
- the Java version

If that fallback identifier cannot be persisted, telemetry is not sent. Development and automated test runtimes do not
send telemetry.

An additional event is sent when an installation reports for the first time and when the plugin version changes, so that installations and updates can be counted.

What is not collected
---------------------

Project names, file paths, source code, serial ports, chip serial numbers, credentials and raw hardware addresses are
never reported. The machine identifier is pseudonymous rather than a user account or personal identity. Your IP
address is not part of the report, although the receiving service sees the address the report is sent from and derives
an approximate location from it.

Disable telemetry reporting
---------------------------

Usage statistics are enabled by default. The first start shows a notification that explains the reporting and offers to switch it off. Reporting can be turned off at any later time as well, and the setting applies to the whole installation rather than to a single workspace:

- Go to ``Window`` > ``Preferences`` > ``Espressif``.
- In the ``Privacy`` group, deselect ``Send usage statistics to Espressif``.
- Click ``Apply and Close``.

Reporting can also be disabled without opening the IDE, which is useful for shared or automated installations:

- Start the IDE with the ``-Didf.telemetry=false`` virtual machine argument, for example by adding it to ``eclipse.ini`` after the ``-vmargs`` line.
- Or set the environment variable ``IDF_TELEMETRY=0`` before starting the IDE.
