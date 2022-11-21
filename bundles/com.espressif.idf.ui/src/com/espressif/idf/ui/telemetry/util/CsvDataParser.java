package com.espressif.idf.ui.telemetry.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvDataParser implements DataParser
{

	private int columnNumber;
	private int totalColumns;
	private String separator;

	public CsvDataParser(int columnNumber, int totalColumns, String separator)
	{
		this.columnNumber = columnNumber;
		this.totalColumns = totalColumns;
		this.separator = separator;
	}

	@Override
	public List<Double> parseSerialData(byte[] buff)
	{
		List<Double> resultList = new ArrayList<>();
		String parsingString = new String(buff).replaceAll("\u0000.*", "").replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
		Pattern pattern = Pattern.compile(String.format("(-?\\d+(\\.\\d+)?[%s]){%d}$", separator, totalColumns));
		for (String compileString : parsingString.split("\n"))
		{
			Matcher matcher = pattern.matcher(compileString.strip());
			while (matcher.find())
			{
				resultList.add(Double.valueOf(compileString.split(separator)[columnNumber]));
			}
		}

		return resultList;
	}

}
