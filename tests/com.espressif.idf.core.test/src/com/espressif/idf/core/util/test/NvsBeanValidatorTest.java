package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.espressif.idf.core.build.NvsTableBean;
import com.espressif.idf.core.util.Messages;
import com.espressif.idf.core.util.NvsBeanValidator;
import com.espressif.idf.core.util.NvsTableDataService;
import com.espressif.idf.core.util.StringUtil;

class NvsBeanValidatorTest
{

	@Test
	void validate_empty_key_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setKey(StringUtil.EMPTY);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 0);

		assertEquals(Messages.NvsValidation_KeyValidationErr_1, actualResult);
	}

	@Test
	void validate_key_longer_then_15_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setKey("test key longer then 15");

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 0);

		assertEquals(Messages.NvsValidation_KeyValidationErr_2, actualResult);
	}

	@Test
	void validate_valid_key_returns_empty_string()
	{
		NvsTableBean tesTableBean = new NvsTableBean();
		tesTableBean.setKey("valid key");

		String actualResult = new NvsBeanValidator().validateBean(tesTableBean, 0);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@Test
	void validate_empty_type_returns_empty_string()
	{
		NvsTableBean tesTableBean = new NvsTableBean();

		String actualResult = new NvsBeanValidator().validateBean(tesTableBean, 1);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@Test
	void validate_not_supported_encoding_returns_validation_error()
	{
		String encoding = "Non existing encoding";
		String type = "file";
		String expectedResult = String.format(Messages.NvsValidation_EncodingValidationErr_1, type,
				String.join(",", NvsTableDataService.getEncodings(type)), encoding);
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType(type);
		testTableBean.setEncoding(encoding);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 2);

		assertEquals(expectedResult, actualResult);
	}

	@ParameterizedTest
	@CsvSource({ "file, hex2bin", "file, base64", "file, string", "file, binary", "data, u8", "data, i8", "data, u16",
			"data, i16", "data, u32", "data, i32", "data, u64", "data, i64", "data, string", "data, hex2bin",
			"data, base64", "namespace, ''", "'', ''" })
	void validate_supported_encoding_returns_empty_string(String type, String encoding)
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType(type);
		testTableBean.setEncoding(encoding);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 2);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@Test
	void validate_non_empty_value_with_type_namespace_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("namespace");
		testTableBean.setValue("test");

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(Messages.NvsValidation_ValueValidationErr_1, actualResult);
	}

	@Test
	void validate_with_empty_value_with_type_namespace_returns_empty_string()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("namespace");
		testTableBean.setValue(StringUtil.EMPTY);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@ParameterizedTest
	@ValueSource(strings = { "file", "data", "namespace" })
	void validate_empty_value_with_type_other_then_namespace_returns_validation_error(String type)
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("file");
		testTableBean.setValue(StringUtil.EMPTY);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(Messages.NvsValidation_ValueValidationErr_2, actualResult);
	}
	
	@ParameterizedTest
	@ValueSource(strings = { "C:", "test", "file.exe" })
	void validate_value_with_type_file_returns_empty_string(String value)
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("file");
		testTableBean.setValue(value);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@Test
	void validate_out_of_limit_value_with_data_type_and_string_encoding_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setEncoding("string");
		String valueOverLimit = "";
		while (valueOverLimit.getBytes().length < 4001)
		{
			valueOverLimit += "q";
		}
		testTableBean.setValue(valueOverLimit);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(Messages.NvsValidation_ValueValidationErr_3, actualResult);

	}

	@Test
	void validate_valid_value_with_data_type_and_string_encoding_returns_empty_string()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setEncoding("string");
		testTableBean.setValue("test");

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(StringUtil.EMPTY, actualResult);
	}


	@Test
	void validate_out_of_limit_value_with_data_type_and_binary_encoding_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setEncoding("binary");
		String valueOverLimit = "";
		while (valueOverLimit.getBytes().length < 4001)
		{
			valueOverLimit += "q";
		}
		testTableBean.setValue(valueOverLimit);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(Messages.NvsValidation_ValueValidationErr_3, actualResult);

	}

	@Test
	void validate_valid_value_with_data_type_and_binary_encoding_returns_empty_string()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setEncoding("binary");
		testTableBean.setValue("test");

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@ParameterizedTest
	@CsvSource({ "u8, 0", "u8, 120", "u8, 255", "i8, -128", "i8, 0", "i8, 120", "i8, 127", "u16, 0", "u16, 360",
			"u16, 65535", "i16, -32768", "i16, 0", "i16, 120", "i16, 32767", "u32, 0", "u32, 1000", "u32, 4294967295",
			"i32, -2147483648", "i32, 9", "i32, 2147483647", "u64, 0", "u64, 2000", "u64, 18446744073709551615",
			"i64, -9223372036854775808", "i64, 3000", "i64, 9223372036854775807" })
	void validate_valid_integer_in_range_returns_empty_string(String encoding, String value)
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setEncoding(encoding);
		testTableBean.setValue(value);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@ParameterizedTest
	@CsvSource({ "u8, -1", "u8, -20", "u8, 256", "i8, -129", "i8, -150", "i8, 150", "i8, 128", "u16, -1", "u16, -50",
			"u16, 65536", "i16, -32769", "i16, -2147483649", "i16, -38000", "i16, 32768", "u32, -1", "u32, -1000",
			"u32, 4294967296", "i32, -2147483649", "i32, 2147483648", "u64, -1", "u64, 18446744073709551616",
			"i64, -9223372036854775809", "i64, 9223372036854775808" })
	void validate_valid_integer_out_of_range_returns_validation_error(String encoding, String value)
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setEncoding(encoding);
		testTableBean.setValue(value);

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(String.format(Messages.NvsValidation_NumberValueValidationErr_2, value, encoding), actualResult);
	}

	@Test
	void validate_non_integer_number_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		String value = "test";
		String expectedResult = "";
		testTableBean.setType("data");
		testTableBean.setEncoding("i8");
		testTableBean.setValue(value);
		try
		{
			BigInteger number = new BigInteger(value);
		}
		catch (NumberFormatException e)
		{
			expectedResult = String.format(Messages.NvsValidation_NumberValueValidationErr_1, e.getLocalizedMessage());
		}
		
		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(expectedResult, actualResult);
	}

	@Test
	void validate_not_specified_index_returns_empty_string()
	{
		NvsTableBean testTableBean = new NvsTableBean();

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 4);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@Test
	void validate_first_bean_with_type_other_then_namespace_returns_validation_error()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");

		String actualResult = new NvsBeanValidator().validateFirstBean(testTableBean);

		assertEquals(Messages.NvsValidation_FirstBeanValidationErr, actualResult);
	}

	@Test
	void validate_first_with_namespace_type_returns_empty_string()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("namespace");

		String actualResult = new NvsBeanValidator().validateFirstBean(testTableBean);

		assertEquals(StringUtil.EMPTY, actualResult);
	}

	@Test
	void validate_data_value_with_non_existing_encoding_returns_empty_string()
	{
		NvsTableBean testTableBean = new NvsTableBean();
		testTableBean.setType("data");
		testTableBean.setValue("test");
		testTableBean.setEncoding("non-existing");

		String actualResult = new NvsBeanValidator().validateBean(testTableBean, 3);

		assertEquals(StringUtil.EMPTY, actualResult);
	}
}
