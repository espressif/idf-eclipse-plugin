package com.espressif.idf.ui.telemetry.util;

import java.util.List;

public interface DataParser
{
	List<Double> parseSerialData(byte[] buff);
}
