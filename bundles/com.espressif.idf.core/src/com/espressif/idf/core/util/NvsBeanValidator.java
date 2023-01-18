/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/

package com.espressif.idf.core.util;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.espressif.idf.core.build.NvsTableBean;

public class NvsBeanValidator
{
	private static final String NAMESPACE = "namespace"; //$NON-NLS-1$
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
		if (!bean.getType().contentEquals(NAMESPACE))
		{
			return Messages.NvsValidation_FirstBeanValidationErr;
		}

		
		return StringUtil.EMPTY;
	}

	private String validateValue(String value, String type, String encoding)
	{
		if (type.contentEquals(NAMESPACE) && !value.isBlank())
		{
			return Messages.NvsValidation_ValueValidationErr_1;
		}

		if (type.contentEquals(NAMESPACE) && value.isBlank())
		{
			return StringUtil.EMPTY;
		}

		if (value.isBlank())
		{
			return Messages.NameValidationError_2;
		}

		if (type.contentEquals("file")) //$NON-NLS-1$
		{
			return StringUtil.EMPTY;
		}

		if (encoding.contentEquals("string") || encoding.contentEquals("binary")) //$NON-NLS-1$ //$NON-NLS-2$
		{
			if (value.length() > 4000)
			{
				return Messages.NvsValidation_ValueValidationErr_3;
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
			return String.format(Messages.NvsValidation_NumberValueValidationErr_1, e.getLocalizedMessage());
		}

		if (bigIntegerValue.compareTo(minValuesMap.get(encoding)) >= 0
				&& bigIntegerValue.compareTo(maxValuesMap.get(encoding)) <= 0)
		{
			return StringUtil.EMPTY;
		}
		else
		{
			return String.format(Messages.NvsValidation_NumberValueValidationErr_2, value, encoding);
		}
	}

	private String validateEncoding(String encoding, String type)
	{

		if (!List.of(NvsTableDataService.getEncodings(type)).contains(encoding))
		{
			return String.format(Messages.NvsValidation_EncodingValidationErr_1, type,
					NvsTableDataService.getEncodings(type), encoding);
		}
		return StringUtil.EMPTY;
	}

	// Always true for now
	private String validateType(String type)
	{
		return StringUtil.EMPTY;
	}

	private String validateKey(String key)
	{
		if (key.isBlank())
		{
			return Messages.NvsValidation_KeyValidationErr_1;
		}

		if (key.length() > 15)
		{
			return Messages.NvsValidation_KeyValidationErr_2;
		}

		return StringUtil.EMPTY;
	}
}
