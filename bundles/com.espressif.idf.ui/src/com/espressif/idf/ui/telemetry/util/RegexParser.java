package com.espressif.idf.ui.telemetry.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParser implements DataParser
{

	private String regex;

	public RegexParser(String regex)
	{
		this.regex = regex;
	}

	@Override
	public List<Double> parseSerialData(byte[] buff)
	{
		List<Double> resultList = new ArrayList<>();
		String parsingString = new String(buff).replaceAll("\u0000.*", "").replaceAll("\u001B\\[[\\d;]*[^\\d;]", "");
		Pattern pattern = Pattern.compile(regex);
		for (String compileString : parsingString.split("\n"))
		{
			Matcher matcher = pattern.matcher(compileString.strip());
			while (matcher.find())
			{
				resultList.add(Double.valueOf(compileString));
			}
		}

		return resultList;

	}

}
