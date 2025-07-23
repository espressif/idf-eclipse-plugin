package com.espressif.idf.core.configparser.vo;

import java.util.List;

public record Board(String name, String target, List<String> config_files)
{
}
