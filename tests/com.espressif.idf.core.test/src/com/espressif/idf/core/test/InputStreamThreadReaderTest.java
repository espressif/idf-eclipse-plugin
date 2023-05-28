package com.espressif.idf.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.espressif.idf.core.InputStreamThread;

class InputStreamThreadReaderTest
{

	private static final String UTF_16 = "UTF-16";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String UTF_8 = "UTF-8";
	private static final String EMPTY_STRING = "";
	private static final String INPUT_STRING = "Testing\nInput";

	@Test
	void testShouldReadInputStreamAndReturnExpectedResult()
	{
		String inputString = INPUT_STRING;
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

		InputStreamThread inputStreamThread = new InputStreamThread(inputStream, NEW_LINE_SEPARATOR, UTF_8);
		inputStreamThread.run();

		String expected = INPUT_STRING;
		String actual = inputStreamThread.getResult();
		assertEquals(expected, actual);
	}

	@Test
	void testShouldThrowExceptionForNullInputStreamInConstructor()
	{
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> new InputStreamThread(null, NEW_LINE_SEPARATOR, UTF_8));

		assertEquals("The InputStream and the newLineSeparator cannot be null!", thrown.getMessage());
	}

	@Test
	void testShouldThrowExceptionForNullNewLineSeparatorInConstructor()
	{
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
			InputStream inputStream = new ByteArrayInputStream("Input".getBytes());
			String newLineSeparator = null;
			String charsetName = UTF_8;
			new InputStreamThread(inputStream, newLineSeparator, charsetName);
		});

		assertEquals("The InputStream and the newLineSeparator cannot be null!", thrown.getMessage());
	}

	@Test
	void testShouldReturnEmptyResultForEmptyInputStream()
	{
		InputStream inputStream = new ByteArrayInputStream(new byte[0]);
		InputStreamThread inputStreamThread = new InputStreamThread(inputStream, NEW_LINE_SEPARATOR, UTF_8);

		inputStreamThread.run();

		String expected = EMPTY_STRING;
		String actual = inputStreamThread.getResult();
		assertEquals(expected, actual);
	}

	@Test
	void testShouldReadInputStreamWithCustomCharsetName()
	{
		String inputString = INPUT_STRING;
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_16));
		InputStreamThread inputStreamThread = new InputStreamThread(inputStream, NEW_LINE_SEPARATOR, UTF_16);

		inputStreamThread.run();

		String expected = INPUT_STRING;
		String actual = inputStreamThread.getResult();
		assertEquals(expected, actual);
	}

	@Test
	void testShouldReadInputStreamWithDefaultCharsetName()
	{
		String inputString = INPUT_STRING;
		InputStream inputStream = new ByteArrayInputStream(inputString.getBytes());

		InputStreamThread inputStreamThread = new InputStreamThread(inputStream, NEW_LINE_SEPARATOR, null);
		inputStreamThread.run();

		String expected = INPUT_STRING;
		String actual = inputStreamThread.getResult();
		assertEquals(expected, actual);
	}

	@Test
	void testShouldReturnEmptyResultOnIOException()
	{
		InputStream inputStream = new InputStream()
		{
			@Override
			public int read() throws IOException
			{
				throw new IOException("Test IOException");
			}
		};
		InputStreamThread inputStreamThread = new InputStreamThread(inputStream, NEW_LINE_SEPARATOR, UTF_8);
		inputStreamThread.run();

		String expected = EMPTY_STRING;
		String actual = inputStreamThread.getResult();
		assertEquals(expected, actual);
	}
}
