package com.espressif.idf.core.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.espressif.idf.core.build.NvsTableBean;

public class NvsBeanValidator
{
	private static final Map<String, BigInteger> minValuesMap = initMinValuesMap();
	private static final Map<String, BigInteger> maxValuesMap = initMaxValuesMap();

	public String validateBean(NvsTableBean bean, int columnIndex)
	{
		switch (columnIndex)
		{
		case 0:
			return validateKey(bean.getKey());
		case 1:
			return validateType(bean.getType());
		case 2:
			return validateEncoding(bean.getEncoding(), bean.getType());
		case 3:
			return validateValue(bean.getValue(), bean.getType(), bean.getEncoding());
		default:
			break;
		}
		return StringUtil.EMPTY;
	}

	private static Map<String, BigInteger> initMinValuesMap()
	{
		Map<String, BigInteger> minValuesMap = new HashMap<>();
		minValuesMap.put("u8", BigInteger.valueOf(0)); //$NON-NLS-1$
		minValuesMap.put("i8", BigInteger.valueOf(-128)); //$NON-NLS-1$
		minValuesMap.put("u16", BigInteger.valueOf(0)); //$NON-NLS-1$
		minValuesMap.put("i16", BigInteger.valueOf(-32768)); //$NON-NLS-1$
		minValuesMap.put("u32", BigInteger.valueOf(0)); //$NON-NLS-1$
		minValuesMap.put("i32", BigInteger.valueOf(Integer.MIN_VALUE)); //$NON-NLS-1$
		minValuesMap.put("u64", BigInteger.valueOf(0)); //$NON-NLS-1$
		minValuesMap.put("i64", BigInteger.valueOf(Long.MIN_VALUE)); //$NON-NLS-1$
		return minValuesMap;
	}

	private static Map<String, BigInteger> initMaxValuesMap()
	{
		Map<String, BigInteger> maxValuesMap = new HashMap<>();
		maxValuesMap.put("u8", BigInteger.valueOf(255)); //$NON-NLS-1$
		maxValuesMap.put("i8", BigInteger.valueOf(127)); //$NON-NLS-1$
		maxValuesMap.put("u16", BigInteger.valueOf(65535)); //$NON-NLS-1$
		maxValuesMap.put("i16", BigInteger.valueOf(32767)); //$NON-NLS-1$
		maxValuesMap.put("u32", BigInteger.valueOf(Integer.toUnsignedLong(-1))); //$NON-NLS-1$
		maxValuesMap.put("i32", BigInteger.valueOf(Integer.MAX_VALUE)); //$NON-NLS-1$
		maxValuesMap.put("u64", new BigInteger("18446744073709551615")); //$NON-NLS-1$ //$NON-NLS-2$
		maxValuesMap.put("i64", BigInteger.valueOf(Long.MAX_VALUE)); //$NON-NLS-1$
		return maxValuesMap;
	}

	public String validateFirstBean(NvsTableBean bean)
	{
		if (!bean.getType().contentEquals("namespace")) //$NON-NLS-1$
		{
			return "First entry should be of type \"namespace\""; //$NON-NLS-1$
		}

		
		return StringUtil.EMPTY;
	}

	private String validateValue(String value, String type, String encoding)
	{
		if (type.contentEquals("namespace") && !value.isBlank()) //$NON-NLS-1$
		{
			return "value must be empty for namespace type"; //$NON-NLS-1$
		}

		if (type.contentEquals("namespace") && value.isBlank()) //$NON-NLS-1$
		{
			return StringUtil.EMPTY;
		}

		if (value.isBlank())
		{
			return "value is required"; //$NON-NLS-1$
		}

		if (type.contentEquals("file")) //$NON-NLS-1$
		{
			return StringUtil.EMPTY;
		}

		if (encoding.contentEquals("string") || encoding.contentEquals("binary")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			if (value.length() > 4000)
			{
				return "String value is limited to 4000 bytes"; //$NON-NLS-1$
			}
			return StringUtil.EMPTY;
		}

		if (minValuesMap.containsKey(encoding))
		{
			return validateNumber(value, encoding);
		}

		return StringUtil.EMPTY;

	}

	private String validateNumber(String value, String encoding)
	{
		BigInteger bigIntegerValue = null;
		try
		{
			bigIntegerValue = BigIntDecoder.decode(value);
		}
		catch (NumberFormatException e)
		{
			return "Incorrect number format " + e.getLocalizedMessage(); //$NON-NLS-1$
		}

		if (bigIntegerValue.compareTo(minValuesMap.get(encoding)) >= 0
				&& bigIntegerValue.compareTo(maxValuesMap.get(encoding)) <= 0)
		{
			return StringUtil.EMPTY;
		}
		else
		{
			return value + " is out of range for " + encoding; //$NON-NLS-1$
		}
	}

	private String validateEncoding(String encoding, String type)
	{

		if (!List.of(NvsTableDataService.getEncodings(type)).contains(encoding))
		{
			return String.format("Unsupported coding for type %s, expected on of %s. Instead got %s", type, //$NON-NLS-1$
					NvsTableDataService.getEncodings(type), encoding);
		}
		return StringUtil.EMPTY;
	}

	private String validateType(String type)
	{
		return StringUtil.EMPTY;
	}

	private String validateKey(String key)
	{
		if (key.isBlank())
		{
			return "Key is required"; //$NON-NLS-1$
		}

		if (key.length() > 15)
		{
			return "Maximum key length is 15 characters"; //$NON-NLS-1$
		}

		return StringUtil.EMPTY;
	}
}
