package com.espressif.idf.core.util;

import java.math.BigInteger;

public class BigIntDecoder
{
	private BigIntDecoder()
	{
	}

	public static BigInteger decode(String stringToDecode) throws NumberFormatException
	{

		int radix = 10;
		int index = 0;
		boolean hasSign = false;

		char firstChar = stringToDecode.charAt(0);
		if (firstChar == '-' || firstChar == '+')
		{
			hasSign = true;
			index++;
		}

		// Handle radix specifier, if present
		if (stringToDecode.startsWith("0x", index) || stringToDecode.startsWith("0X", index)) //$NON-NLS-1$ //$NON-NLS-2$
		{
			index += 2;
			radix = 16;
		}
		else if (stringToDecode.startsWith("#", index)) //$NON-NLS-1$
		{
			index++;
			radix = 16;
		}
		else if (stringToDecode.startsWith("0", index) && stringToDecode.length() > 1 + index) //$NON-NLS-1$
		{
			index++;
			radix = 8;
		}

		return hasSign ? new BigInteger(firstChar + stringToDecode.substring(index), radix)
				: new BigInteger(stringToDecode.substring(index));
	}

}
