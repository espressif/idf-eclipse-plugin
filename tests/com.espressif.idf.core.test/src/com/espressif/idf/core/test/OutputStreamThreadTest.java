package com.espressif.idf.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.espressif.idf.core.OutputStreamThread;

class OutputStreamThreadTest
{

	private static final String UTF_8 = "UTF-8";
	private static final String TEST_INPUT = "Test, Input";

	@Test
	public void testOutputStreamThreadShouldWriteContentToOutputStream() throws IOException, InterruptedException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OutputStreamThread thread = new OutputStreamThread(outputStream, TEST_INPUT, UTF_8);
		thread.run();

		String actualOutput = new String(outputStream.toByteArray(), UTF_8);
		assertEquals(TEST_INPUT, actualOutput);
	}

	@Test
	void testOutputStreamThreadWithDefaultCharsetShouldWriteContentToOutputStream()
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		OutputStreamThread thread = new OutputStreamThread(outputStream, TEST_INPUT, null);
		thread.run();

		String actualOutput = new String(outputStream.toByteArray());
		assertEquals(TEST_INPUT, actualOutput);
	}

	@Test
	void testOutputStreamThreadWithNullOutputStreamShouldThrowException()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new OutputStreamThread(null, TEST_INPUT, UTF_8);
		});
	}

	@Test
	void testOutputStreamThreadWithNullContentStreamShouldThrowException()
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		assertThrows(IllegalArgumentException.class, () -> {
			new OutputStreamThread(outputStream, null, UTF_8);
		});
	}

	@Test
	void testOutputStreamShouldBeEmptyIfIncorrectCharsetSent() throws IOException, InterruptedException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		OutputStreamThread thread = new OutputStreamThread(outputStream, TEST_INPUT, "incorrectCharset");

		thread.run();

		String actualOutput = new String(outputStream.toByteArray(), UTF_8);
		assertEquals("", actualOutput);

	}
}
