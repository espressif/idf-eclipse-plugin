package com.espressif.idf.core.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.espressif.idf.core.ProcessBuilderFactory;

class ProcessBuilderFactoryTest
{
	private static final List<String> cmd = Arrays.asList("cmd", "/c");
	private static final List<String> bash = Arrays.asList("bash", "-c", "true");
	private static List<String> dummyCommand;

	private static boolean isWindows()
	{
		return System.getProperty("os.name").startsWith("Windows");
	}

	@BeforeAll
	static void setUp()
	{
		dummyCommand = isWindows() ? cmd : bash;
	}

	@Test
	void testRunInBackgroundShouldReturnOkStatusWithOnlyDummyCommand() throws IOException
	{
		IStatus status = new ProcessBuilderFactory().runInBackground(dummyCommand, null, null);

		assertEquals(IStatus.OK, status.getCode());
	}

	@Test
	void testRunInBackgroundShouldReturnOkStatusWithAllCorrectArguments() throws IOException
	{
		IStatus status = new ProcessBuilderFactory().runInBackground(dummyCommand, Path.ROOT, null);

		assertEquals(IStatus.OK, status.getCode());
	}

	@Test
	void testRunShouldThrowExceptionWithIncorrectWorkingDir()
	{
		Map<String, String> emptyEnvironment = new HashMap<>();

		assertThrows(IOException.class, () ->
		new ProcessBuilderFactory().run(dummyCommand, Path.EMPTY, emptyEnvironment));
	}

	@Test
	void testRunShouldReturnEmptyResultWithDummyCommandAndEmptyEnviromentMap() throws IOException
	{
		Map<String, String> emptyEnvironment = new HashMap<>();

		Process process = new ProcessBuilderFactory().run(dummyCommand, Path.ROOT, emptyEnvironment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithDummyCommandWithNullEnviromentMap() throws IOException
	{
		Process process = new ProcessBuilderFactory().run(dummyCommand, Path.ROOT, null);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithDummyCommandAndNullWorkingDir() throws IOException
	{
		Map<String, String> environment = new HashMap<>(System.getenv());

		Process process = new ProcessBuilderFactory().run(dummyCommand, null, environment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldReturnEmptyResultWithDummyCommandAndWithAllArguments() throws IOException
	{
		Map<String, String> environment = new HashMap<>(System.getenv());

		Process process = new ProcessBuilderFactory().run(dummyCommand, Path.ROOT, environment);

		assertArrayEquals(new byte[0], process.getInputStream().readAllBytes());
	}

	@Test
	void testRunShouldThrowExceptionWithNonExistingFileCommand()
	{
		List<String> command = new ArrayList<>();
		command.add("NotExistingFile");
		Map<String, String> environment = new HashMap<>(System.getenv());

		assertThrows(IOException.class, () -> new ProcessBuilderFactory().run(command, Path.EMPTY, environment));
	}

}
