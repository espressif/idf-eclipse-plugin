KConfig 编辑器
==============

:link_to_translation:`en:[English]`

KConfig 编辑器为 ESP-IDF 配置文件提供增强的编辑功能，包括语法高亮、内容辅助和智能编辑。该编辑器支持 ESP-IDF 项目中使用的所有 KConfig 文件类型。

支持的文件类型
--------------

KConfig 编辑器会自动识别并为下列文件类型提供增强编辑：

- ``Kconfig`` – 主配置文件
- ``Kconfig.projbuild`` – 项目特定的配置文件

功能
----

语法高亮
~~~~~~~~

该编辑器为所有 KConfig 语言结构提供全面的语法高亮：

- **关键字**：``config``、``menuconfig``、``choice``、``menu``、``endmenu``、``endchoice``。
- **类型**：``bool``、``tristate``、``string``、``hex``、``int``。
- **属性**：``default``、``depends``、``select``、``help``、``prompt``。
- **取值**：``y``、``n``、``m``、字符串字面量、十六进制与整数值。
- **注释**：以 ``#`` 开头的行注释。

内容辅助
~~~~~~~~

编辑 KConfig 文件时提供智能建议：

- **主要关键字**：对 ``config``、``menuconfig``、``choice``、``menu`` 等的建议。
- **类型**：对 ``bool``、``tristate``、``string``、``hex``、``int`` 的建议。
- **属性**：对 ``default``、``depends``、``select``、``help``、``prompt`` 的自动补全。
- **取值**：针对 ``y``、``n``、``m`` 与字符串值的上下文感知建议。

自动补全成对符号
~~~~~~~~~~~~~~~~

编辑器会自动处理括号和引号的成对输入：

- **圆括号**：输入 ``(`` 时自动补全 ``)``。
- **引号**：输入 ``"`` 时自动补全 ``"``。
- **智能光标定位**：光标保持在继续输入的最佳位置。

自动补全成对符号示例：

.. code-block:: kconfig

    config ESP32_WIFI_ENABLED
        bool "Enable WiFi"
        default y
        depends on (IDF_TARGET="esp32" || IDF_TARGET="esp32s2")
        help
            "Enable WiFi support for ESP32"

当输入 ``depends on (`` 时，编辑器会自动补上 ``)``，并将光标置于二者之间。

智能缩进
~~~~~~~~

编辑器为 KConfig 结构提供智能缩进规则：

- **增加缩进**：在 ``menu``、``config``、``menuconfig``、``choice``、``help``、``comment`` 之后。
- **减少缩进**：在 ``endmenu``、``endchoice``、``endif`` 之后。
- **格式一致性**：保持正确的 KConfig 文件结构。

括号匹配
~~~~~~~~

支持高亮匹配成对符号：

- **菜单块**：``menu`` 与 ``endmenu`` 成对。
- **选择块**：``choice`` 与 ``endchoice`` 成对。
- **圆括号**：``(`` 与 ``)`` 成对。
- **引号**：``"`` 与 ``"`` 成对。

代码折叠
~~~~~~~~

支持折叠 KConfig 代码块：

- **菜单区域**：折叠或展开菜单块。
- **选择区域**：折叠或展开选择块。
- **注释区域**：折叠或展开注释段。

用法
----

打开 KConfig 文件
~~~~~~~~~~~~~~~~~

在以下情况下，KConfig 文件会自动使用增强型编辑器打开：

1. 在项目资源管理器中双击任意 ``Kconfig`` 或 ``Kconfig.projbuild`` 文件。
2. 右键点击并选择 ``Open With`` > ``KConfig Editor``。
3. 从 ``File`` 菜单打开文件。

编辑器会自动检测文件类型并应用相应的语法高亮与其他编辑功能。

编辑 KConfig 文件
~~~~~~~~~~~~~~~~~

编辑 KConfig 文件时，可以使用：

- **内容辅助**：按 ``Ctrl+Space`` 触发内容建议。
- **自动补全**：输入部分关键字后按 ``Tab`` 补全内容。
- **括号导航**：使用 ``Ctrl+Shift+P`` 在匹配的括号之间跳转。
- **代码折叠**：点击编辑器边栏中的折叠图标以折叠或展开内容。

KConfig 条目示例
~~~~~~~~~~~~~~~~

下面是一个带有语法高亮的完整 KConfig 条目示例：

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

参考资料
--------

- `ESP-IDF KConfig 语言参考 <https://docs.espressif.com/projects/esp-idf-kconfig/en/latest/kconfiglib/language.html>`_
- `KConfig 语言文档 <https://www.kernel.org/doc/Documentation/kbuild/kconfig-language.txt>`_
- `Eclipse TM4E 文档 <https://github.com/eclipse/tm4e>`_
