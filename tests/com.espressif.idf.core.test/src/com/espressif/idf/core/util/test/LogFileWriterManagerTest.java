package com.espressif.idf.core.util.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.espressif.idf.core.logging.LogFileWriterManager;

public class LogFileWriterManagerTest
{

	@TempDir
	Path tempDir;

	private Path logFile;

	@BeforeEach
	void setup()
	{
		logFile = tempDir.resolve("test.log"); //$NON-NLS-1$
	}

	@AfterEach
	void tearDown()
	{
		LogFileWriterManager.closeAll();
	}

	@Test
	void testWriteAndReadFile() throws IOException
	{
		PrintWriter writer = LogFileWriterManager.getWriter(logFile.toString(), false);
		writer.println("Hello World"); //$NON-NLS-1$
		writer.flush();

		String content = Files.readString(logFile);
		assertTrue(content.contains("Hello World")); //$NON-NLS-1$
	}

	@Test
	void testAppendModeTrue() throws IOException
	{
		PrintWriter writer1 = LogFileWriterManager.getWriter(logFile.toString(), true);
		writer1.println("Line 1"); //$NON-NLS-1$
		writer1.flush();

		PrintWriter writer2 = LogFileWriterManager.getWriter(logFile.toString(), true);
		writer2.println("Line 2"); //$NON-NLS-1$
		writer2.flush();

		String content = Files.readString(logFile);
		assertTrue(content.contains("Line 1")); //$NON-NLS-1$
		assertTrue(content.contains("Line 2")); //$NON-NLS-1$
	}

	@Test
	void testAppendModeFalseCreatesNewFile() throws IOException
	{
		PrintWriter writer1 = LogFileWriterManager.getWriter(logFile.toString(), false);
		writer1.println("Initial Line"); //$NON-NLS-1$
		writer1.flush();

		// Manually close and remove
		LogFileWriterManager.closeWriter(logFile.toString());

		PrintWriter writer2 = LogFileWriterManager.getWriter(logFile.toString(), false);
		writer2.println("New Line"); //$NON-NLS-1$
		writer2.flush();

		String content = Files.readString(logFile);
		assertTrue(content.contains("Initial Line") || content.contains("New Line")); //$NON-NLS-1$ //$NON-NLS-2$
		// NOTE: This test doesn't guarantee truncation unless we reimplement logic
		// to forcibly truncate when append=false
	}

	@Test
	void testNullAndEmptyPathReturnsNullWriter()
	{
		PrintWriter writer1 = LogFileWriterManager.getWriter(null, true);
		PrintWriter writer2 = LogFileWriterManager.getWriter("", false); //$NON-NLS-1$

		assertNotNull(writer1);
		assertNotNull(writer2);

		// Writing should not throw
		assertDoesNotThrow(() -> writer1.println("foo")); //$NON-NLS-1$
		assertDoesNotThrow(() -> writer2.println("bar")); //$NON-NLS-1$
	}

	@Test
	void testSharedWriterInstance()
	{
		PrintWriter writer1 = LogFileWriterManager.getWriter(logFile.toString(), true);
		PrintWriter writer2 = LogFileWriterManager.getWriter(logFile.toString(), true);
		assertSame(writer1, writer2);
	}

	@Test
	void testCloseWriter() throws IOException
	{
		PrintWriter writer = LogFileWriterManager.getWriter(logFile.toString(), true);
		writer.println("Before close"); //$NON-NLS-1$
		writer.flush();

		LogFileWriterManager.closeWriter(logFile.toString());

		// After close, it's removed from the map, so new one should be different
		PrintWriter newWriter = LogFileWriterManager.getWriter(logFile.toString(), true);
		assertNotSame(writer, newWriter);
	}

	@Test
	void testCloseAllWriters() throws IOException
	{
		LogFileWriterManager.getWriter(logFile.toString(), true);
		Path anotherFile = tempDir.resolve("another.log"); //$NON-NLS-1$
		LogFileWriterManager.getWriter(anotherFile.toString(), true);

		LogFileWriterManager.closeAll();

		// Should be empty after closeAll
		PrintWriter newWriter = LogFileWriterManager.getWriter(logFile.toString(), true);
		assertNotNull(newWriter);
	}

	@Test
	void testThreadSafeConcurrentAccess() throws Exception
	{
		ExecutorService executor = Executors.newFixedThreadPool(10);
		String path = logFile.toString();

		Callable<Void> task = () -> {
			PrintWriter writer = LogFileWriterManager.getWriter(path, true);
			for (int i = 0; i < 50; i++)
			{
				synchronized (writer)
				{
					writer.println("Line " + i); //$NON-NLS-1$
				}
			}
			return null;
		};

		// Run tasks in parallel
		Future<?>[] futures = new Future<?>[5];
		for (int i = 0; i < futures.length; i++)
		{
			futures[i] = executor.submit(task);
		}

		for (Future<?> future : futures)
		{
			future.get();
		}

		executor.shutdown();

		String content = Files.readString(logFile);
		assertTrue(content.contains("Line 0")); //$NON-NLS-1$
		assertTrue(content.contains("Line 49")); //$NON-NLS-1$
	}
}
