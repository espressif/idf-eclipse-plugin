package com.espressif.idf.core.configparser.vo;

import java.util.List;

public record Option(String name, List<String> values)
{
}