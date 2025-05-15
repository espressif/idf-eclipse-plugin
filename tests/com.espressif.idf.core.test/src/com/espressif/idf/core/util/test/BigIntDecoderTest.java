/*******************************************************************************
 * Copyright 2023 Espressif Systems (Shanghai) PTE LTD. All rights reserved.
 * Use is subject to license terms.
 *******************************************************************************/
package com.espressif.idf.core.util.test;

import java.math.BigInteger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.espressif.idf.core.util.BigIntDecoder;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class BigIntDecoderTest
{

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "12345, 12345", "234, 234", "564, 564" })
	void test_decode_positive_dec_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "+12345, 12345", "+234, 234", "+564, 564" })
	void test_decode_positive_dec_with_sign_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "-12345, -12345", "-234, -234", "-564, -564" })
	void test_decode_negative_dec_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "0xA3F, 2623", "0XA3F, 2623", "0xAA, 170", "0XFF, 255" })
	void test_decode_positive_hexadecimal_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "#A3F, 2623", "#A3F, 2623", "#AA, 170", "#FF, 255" })
	void test_decode_positive_hexadecimal_hash_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "+0xA3F, 2623", "+0XA3F, 2623", "+0xAA, 170", "+0XFF, 255" })
	void test_decode_positive_with_sign_hexadecimal_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "-0xA3F, -2623", "-0XA3F, -2623", "-0xAA, -170", "-0XFF, -255" })
	void test_decode_negative_hexadecimal_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "017, 15", "023, 19", "075, 61", "0127, 87", "0456, 302" })
	void test_decode_positive_octal_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "+017, 15", "+023, 19", "+075, 61", "+0127, 87", "+0456, 302" })
	void test_decode_positive_with_sign_octal_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}

	@ParameterizedTest(name = "value ''{0}'' decoded to {1}")
	@CsvSource({ "-017, -15", "-023, -19", "-075, -61", "-0127, -87", "-0456, -302" })
	void test_decode_negative_octal_number(String stringToDecode, int expectedResult)
	{
		BigInteger decodedNumber = BigIntDecoder.decode(stringToDecode);

		Assertions.assertEquals(expectedResult, decodedNumber.intValue());
	}


	@Test
	void test_decode_invalid_number()
	{
		String stringToDecode = "12AB";

		Assertions.assertThrows(NumberFormatException.class, () -> BigIntDecoder.decode(stringToDecode));
	}
}
