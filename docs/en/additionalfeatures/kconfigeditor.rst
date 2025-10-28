KConfig Editor
==============

The KConfig Editor provides enhanced editing capabilities for ESP-IDF configuration files, including syntax highlighting, content assist, and intelligent editing features. It supports all KConfig file types used in ESP-IDF projects.

Supported File Types
--------------------

The KConfig Editor automatically recognizes and provides enhanced editing for the following file types:

- ``Kconfig`` - Main configuration files
- ``Kconfig.projbuild`` - Project-specific configuration files  

Features
--------

Syntax Highlighting
~~~~~~~~~~~~~~~~~~~

The editor provides comprehensive syntax highlighting for all KConfig language constructs:

- **Keywords**: ``config``, ``menuconfig``, ``choice``, ``menu``, ``endmenu``, ``endchoice``
- **Types**: ``bool``, ``tristate``, ``string``, ``hex``, ``int``
- **Properties**: ``default``, ``depends``, ``select``, ``help``, ``prompt``
- **Values**: ``y``, ``n``, ``m``, string literals, hexadecimal and integer values
- **Comments**: Line comments starting with ``#``

Content Assist
~~~~~~~~~~~~~~

Intelligent content proposals are available when editing KConfig files:

- **Main Keywords**: Proposals for ``config``, ``menuconfig``, ``choice``, ``menu``, etc.
- **Types**: Suggestions for ``bool``, ``tristate``, ``string``, ``hex``, ``int``
- **Properties**: Auto-completion for ``default``, ``depends``, ``select``, ``help``, ``prompt``
- **Values**: Context-aware suggestions for ``y``, ``n``, ``m``, and string values

Auto-closing Pairs
~~~~~~~~~~~~~~~~~~

The editor automatically handles bracket and quote pairing:

- **Parentheses**: Automatically closes ``(`` with ``)``
- **Quotes**: Automatically closes ``"`` with ``"``
- **Smart Cursor Positioning**: Cursor stays in the optimal position for continued typing

Example of auto-closing pairs:

.. code-block:: kconfig

    config ESP32_WIFI_ENABLED
        bool "Enable WiFi"
        default y
        depends on (IDF_TARGET="esp32" || IDF_TARGET="esp32s2")
        help
            "Enable WiFi support for ESP32"

When you type ``depends on (``, the editor automatically adds ``)`` and positions the cursor between them.

Smart Indentation
~~~~~~~~~~~~~~~~~

The editor provides intelligent indentation rules for KConfig structure:

- **Increase Indent**: After ``menu``, ``config``, ``menuconfig``, ``choice``, ``help``, ``comment``
- **Decrease Indent**: After ``endmenu``, ``endchoice``, ``endif``
- **Consistent Formatting**: Maintains proper KConfig file structure

Bracket Matching
~~~~~~~~~~~~~~~~

Visual highlighting of matching bracket pairs:

- **Menu Blocks**: ``menu`` and ``endmenu`` pairs
- **Choice Blocks**: ``choice`` and ``endchoice`` pairs
- **Parentheses**: ``(`` and ``)`` pairs
- **Quotes**: ``"`` and ``"`` pairs

Code Folding
~~~~~~~~~~~~

Support for folding KConfig blocks:

- **Menu Sections**: Fold/unfold menu blocks
- **Choice Sections**: Fold/unfold choice blocks
- **Comment Regions**: Fold/unfold comment sections

Usage
-----

Opening KConfig Files
~~~~~~~~~~~~~~~~~~~~~

KConfig files are automatically opened with the enhanced editor when:

1. Double-clicking on any ``Kconfig`` or ``Kconfig.projbuild`` file in the Project Explorer
2. Right-clicking and selecting "Open With > KConfig Editor"
3. Opening files from the File menu

The editor will automatically detect the file type and apply the appropriate syntax highlighting and features.

Editing KConfig Files
~~~~~~~~~~~~~~~~~~~~~

When editing KConfig files, you can take advantage of:

- **Content Assist**: Press ``Ctrl+Space`` to trigger content proposals
- **Auto-completion**: Type partial keywords and press ``Tab`` to complete
- **Bracket Navigation**: Use ``Ctrl+Shift+P`` to jump between matching brackets
- **Code Folding**: Click the fold icons in the editor gutter to collapse/expand sections

Example KConfig Entry
~~~~~~~~~~~~~~~~~~~~~

Here's an example of a complete KConfig entry with syntax highlighting:

.. code-block:: kconfig

    config ESP32_WIFI_ENABLED
        bool "Enable WiFi Support"
        default y
        depends on IDF_TARGET_ESP32 || IDF_TARGET_ESP32S2
        select ESP32_WIFI
        help
            Enable WiFi support for ESP32 and ESP32-S2 chips.
            
            This option enables the WiFi driver and related functionality.
            It requires the ESP32 or ESP32-S2 target to be selected.

    config ESP32_WIFI_MAX_CONN_NUM
        int "Maximum number of WiFi connections"
        range 1 10
        default 4
        depends on ESP32_WIFI_ENABLED
        help
            Maximum number of concurrent WiFi connections supported.

References
----------

- `ESP-IDF KConfig Language Reference <https://docs.espressif.com/projects/esp-idf-kconfig/en/latest/kconfiglib/language.html>`_
- `KConfig Language Documentation <https://www.kernel.org/doc/Documentation/kbuild/kconfig-language.txt>`_
- `Eclipse TM4E Documentation <https://github.com/eclipse/tm4e>`_

