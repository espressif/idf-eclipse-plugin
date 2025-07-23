package com.espressif.idf.core.configparser.vo;

import java.util.List;

public record EspConfig(List<Target> targets, List<Option> options, List<Board> boards)
{
}
